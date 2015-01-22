package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.upenn.cis455.concurrency.BlockingQueue;
import edu.upenn.cis455.mapreduce.MapReduceBase;
import edu.upenn.cis455.mapreduce.Reducer;
import edu.upenn.cis455.util.FileUtil;

public class ReduceWorker {

	public static final String TAG = ReduceWorker.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private String mMasterHost = null;
	private int mMasterPort = -1;
	private String mStorageDir;
	private Class<?> mReducerClass = null;
	private Map<String, String> mParameters = null;
	private String mOutputDir = null;
	private int mThreadsCount = 1;
	private ReducerThread[] mReducers = null;
	private BlockingQueue<KeyValueCollections> mQueue =
			new BlockingQueue<KeyValueCollections>();
	private String mSortCommand;
	
//	private volatile boolean mReading = true;
	private int mFinished = 0;
	private ReducerContext mContext = null;
	
	public interface ReducerListener {
		void onReduceKeyRead();
		void onKeyWritten();
		void onKeyReduced();
		void onReduceFinished();
	}
	private ReducerListener mReducerListener = null;

	public ReduceWorker(String storageDir) {
		this.mStorageDir = storageDir;
		String os = System.getProperty("os.name");
		if(os.startsWith("Windows")) { // Windows
			String pwd = System.getProperty("user.dir");
			File sort = new File(pwd, "exec\\sort.exe");
			mSortCommand = sort.getAbsolutePath() + " -k1 ";
		} else { // Linux
			mSortCommand = "sort -k1 ";
		}
	}

	public void setReducerClass(Class<?> reducerClass) {
		this.mReducerClass = reducerClass;
	}

	public void setParameters(Map<String, String> parameters) {
		this.mParameters = parameters;
	}

	public void setOutputDir(String outputDir) {
		this.mOutputDir = outputDir;
	}

	public void setThreadsCount(int threadsCount) {
		this.mThreadsCount = threadsCount;
	}
	
	public void setMaster(String host, int port) {
		mMasterHost = host;
		mMasterPort = port;
//		mContext.setMaster(mMasterHost, mMasterPort);
	}
	
	public void setReducerListener(ReducerListener l) {
		this.mReducerListener = l;
	}

	/**
	 * Start the reduce worker
	 * @return
	 */
	public boolean start() {
		if(mReducerClass == null || mOutputDir == null || mThreadsCount < 1
				|| mMasterHost == null || mMasterPort < 0) {
			return false;
		}
		Reducer reducer = createReducer();
		if(reducer == null) {
			return false;
		}
		
		// Clear output dir
		File outputDir = new File(mStorageDir, mOutputDir);
		if(outputDir.exists()) {
			FileUtil.clearDir(outputDir);
		} else {
			outputDir.mkdirs();
		}
		mContext = new ReducerContext(outputDir);
		mContext.setMaster(mMasterHost, mMasterPort);
//		mReading = true;
		mFinished = 0;
		mContext.setup();
		
		// Create reducer threads
		mReducers = new ReducerThread[mThreadsCount];
		for(int i = 0; i < mThreadsCount; i++) {
			mReducers[i] = new ReducerThread(reducer);
			if(i != mThreadsCount - 1) {
				reducer = createReducer();
			}
		}
		// Start the reader thread
		new ReaderThread().start();
		// Start reducer threads
		for(ReducerThread t : mReducers) {
			t.start();
		}
		return true;
	}
	
	private synchronized void reportFinished() {
		mFinished++;
		if(mFinished >= mThreadsCount) {
			mContext.close();
			if(mReducerListener != null) {
				mReducerListener.onReduceFinished();
			}
		}
	}
	
	private Reducer createReducer() {
		Reducer reducer = null;
		if(mReducerClass != null) {
			try {
				reducer = (Reducer) mReducerClass.newInstance();
			} catch (ClassCastException e) {
				logger.error(mReducerClass.getCanonicalName() + "does not implement Reducer");
			} catch (Exception e) {
				logger.error("Cannot instantiate class " + mReducerClass.getCanonicalName());
			}
		}
		if(reducer != null && reducer instanceof MapReduceBase
				&& mParameters != null) {
			MapReduceBase base = (MapReduceBase) reducer;
			base.setStorageDir(mStorageDir);
			base.setup(mParameters);
		}
		return reducer;
	}
	
	class ReaderThread extends Thread {
		
		@Override
		public void run() {
			File spoolInDir = new File(mStorageDir, WorkerServlet.DIR_SPOOL_IN);
			if(!spoolInDir.exists() || !spoolInDir.isDirectory()) {
				logger.error("spool-in directory does not exist");
				notifyComplete();
				return;
			}
			File spoolIn = new File(spoolInDir, WorkerServlet.SPOOL_IN_FILE);
			if(!spoolIn.exists() || !spoolIn.isFile()) {
				logger.error("spool-in/in file does not exist");
				notifyComplete();
				return;
			}
			String command = mSortCommand + spoolIn.getAbsolutePath();
			BufferedReader reader = null;
			try {
				Process p = Runtime.getRuntime().exec(command);
				reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				String lastKey = null;
				List<String> valueList = new ArrayList<String>();
				while((line = reader.readLine()) != null) {
//					logger.debug("Read line: " + line);
					int tab = line.indexOf('\t');
					if(tab >= 0) {
						String key = line.substring(0, tab);
						String value = line.substring(tab + 1);
						if(lastKey != null && !key.equals(lastKey)) {
							addToQueue(lastKey, valueList);
							valueList.clear();
						}
						lastKey = key;
						valueList.add(value);
						// Clear reference
						key = null;
						value = null;
					}
				}
				if(lastKey != null) {
					addToQueue(lastKey, valueList);
				}
//				mReading = false;
//				logger.debug("mReading set to false 3");
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} finally {
				if(reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
//				logger.debug("Exit at 3");
				notifyComplete();
			}
		}
		
		private void notifyComplete() {
			for(int i = 0; i < mThreadsCount; i++) {
//				t.notifyComplete();
				KeyValueCollections exitObj = new KeyValueCollections(true);
				try {
					mQueue.put(exitObj);
				} catch (InterruptedException e) {
				}
			}
		}
		
		private void addToQueue(String key, List<String> valueList) {
			if(mReducerListener != null) {
				mReducerListener.onReduceKeyRead();
			}
			String[] values = new String[valueList.size()];
			valueList.toArray(values);
			KeyValueCollections kvc = new KeyValueCollections(key, values);
			try {
				mQueue.put(kvc);
			} catch (InterruptedException e) {
			}
		}
	}
	
	class ReducerThread extends Thread {

		private Reducer reducer;
		
		public ReducerThread(Reducer reducer) {
			this.reducer = reducer;
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					KeyValueCollections pair = mQueue.take();
					if(pair.isExit()) {
//						logger.debug("Reducer knows it's time to exit");
						break;
					}
					reducer.reduce(pair.getKey(), pair.getValues(), mContext);
					if(mReducerListener != null) {
						mReducerListener.onKeyReduced();
					}
					// Clear reference
					pair = null;
				} catch (Throwable t) {
					logger.error(t.getMessage(), t);
				}
			}
			if(reducer instanceof MapReduceBase) {
				((MapReduceBase) reducer).cleanUp();
			}
			reportFinished();
		}
	}
	
	class ReducerContext extends BaseContext {

		public ReducerContext(File outputDir) {
			super(outputDir);
		}

		private PrintWriter writer = null;
		
		public void setup() {
//			File outputDir = new File(mStorageDir, mOutputDir);
//			WorkerServlet.clearDir(outputDir);
			File outputFile = new File(mOutputDir, WorkerServlet.OUTPUT_FILE);
			if(outputFile.exists()) {
				outputFile.delete();
			}
			try {
//				outputFile.createNewFile();
				writer = new PrintWriter(new FileWriter(outputFile, true), true);
			} catch (IOException e) {
				logger.error("Output file: " + outputFile.getAbsolutePath());
				logger.error(e.getMessage(), e);
			}
		}
		
		public void close() {
			super.close();
			if(writer != null) {
				writer.close();
			}
		}
		
		@Override
		public void write(String key, String value) {
			synchronized (writer) {
//				System.out.println("Writing key " + key);
				if(key != null) {
					writer.print(key);
				}
				if(key != null && value != null) {
					writer.print('\t');
				}
				if(value != null) {
					writer.print(value);
				}
				writer.println();
				if(mReducerListener != null) {
					mReducerListener.onKeyWritten();
				}
			}
		}
	}
	
	static class KeyValueCollections {
		
		private String key;
		private String[] values;
		private boolean exit;
		
		public KeyValueCollections(String key, String[] values) {
			this.key = key;
			this.values = values;
			this.exit = false;
		}
		
		public KeyValueCollections(boolean exit) {
			this.key = null;
			this.values = null;
			this.exit = exit;
		}

		public String getKey() {
			return key;
		}
		public String[] getValues() {
			return values;
		}
		public boolean isExit() {
			return exit;
		}
	}
}
