package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.upenn.cis455.concurrency.BlockingQueue;
import edu.upenn.cis455.mapreduce.MapReduceBase;
import edu.upenn.cis455.mapreduce.Mapper;

public class MapWorker {
	
	public static final String TAG = MapWorker.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

	private String mMasterHost = null;
	private int mMasterPort = -1;
	private String mStorageDir;
	private Class<?> mMapperClass = null;
	private Map<String, String> mParameters = null;
	private String mInputDir = null;
	private int mThreadsCount = 1;
	private MapperThread[] mMappers = null;
	private String[] mWorkers = null;
	
	private int mFinished = 0; // The number of threads that finished mapping
	private MapperContext mContext = null;
	private BlockingQueue<KeyValuePair> mQueue = new BlockingQueue<KeyValuePair>();
	
	public interface MapperListener {
		void onKeyRead();
		void onKeyMapped();
		void onMapFinished();
	}
	private MapperListener mMapperListener = null;
	
	public MapWorker(String storageDir) {
		this.mStorageDir = storageDir;
	}

	public void setMapperClass(Class<?> mapperClass) {
		mMapperClass = mapperClass;
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.mParameters = parameters;
	}

	public void setInputDir(String inputDir) {
		this.mInputDir = inputDir;
	}
	
	public void setThreadsCount(int threadCount) {
		this.mThreadsCount = threadCount;
	}
	
	public void setWorkers(String[] workers) {
		this.mWorkers = workers;
	}
	
	public void setMaster(String host, int port) {
		mMasterHost = host;
		mMasterPort = port;
	}
	
	public void setMapperListener(MapperListener l) {
		this.mMapperListener = l;
	}

	/**
	 * Start the map worker
	 * @return Whether the worker is successfully started
	 */
	public boolean start() {
		if(mMapperClass == null || mInputDir == null
		|| mThreadsCount < 1 || mWorkers == null
		|| mMasterHost == null || mMasterPort < 0) {
			return false;
		}
		File inputDir = new File(mStorageDir, mInputDir);
//		if(!inputDir.exists() || !inputDir.isDirectory()) {
//			return false;
//		}
		Mapper mapper = createMapper();
		if(mapper == null) { // Cannot load mapper
			return false;
		}
		
		mFinished = 0;
		File spoolOutDir = new File(mStorageDir, WorkerServlet.DIR_SPOOL_OUT);
		if(!spoolOutDir.exists()) {
			spoolOutDir.mkdirs();
		}
		mContext = new MapperContext(spoolOutDir);
		mContext.setMaster(mMasterHost, mMasterPort);
		mContext.setup(mWorkers);
		
		// Create mapper threads
		mMappers = new MapperThread[mThreadsCount];
		for(int i = 0; i < mThreadsCount; i++) {
			mMappers[i] = new MapperThread(mapper);
			if(i != mThreadsCount - 1) {
				mapper = createMapper();
			}
		}
		
		// Start the reader thread
		new ReaderThread(inputDir).start();
		// Start the mapper thread
		for(MapperThread t : mMappers) {
			t.start();
		}
		return true;
	}
	
	/**
	 * Report the thread has finished mapping
	 */
	private void reportFinished() {
		logger.info("Map reading finished");
		mFinished++;
		if(mFinished >= mThreadsCount) { // All threads finished
//			logger.info("threads finished");
			mContext.close();
			if(mMapperListener != null) {
//				logger.info("nMapperListener !=null");
				mMapperListener.onMapFinished();
			}
		}
	}
	
	private Mapper createMapper() {
		Mapper mapper = null;
		if(mMapperClass != null) {
			try {
				mapper = (Mapper) mMapperClass.newInstance();
			} catch (ClassCastException e) {
				logger.error(mMapperClass.getCanonicalName() + "does not implement Mapper");
			} catch (Exception e) {
				logger.error("Cannot instantiate class " + mMapperClass.getCanonicalName());
			}
		}
		if(mapper != null && mapper instanceof MapReduceBase
				&& mParameters != null) {
			MapReduceBase base = (MapReduceBase) mapper;
			base.setStorageDir(mStorageDir);
			base.setup(mParameters);
		}
		return mapper;
	}
	
	public static String getSpoolOutFileName(String ipPort) {
		if(ipPort == null) {
			return null;
		}
		return ipPort.replace('.', '-').replace(':', '_');
	}
	
	public static String getIpPort(String spoolOut) {
		if(spoolOut == null) {
			return null;
		}
		return spoolOut.replace('-', '.').replace('_', ':');
	}
	
	/**
	 * Reads files from input directory into the kv-queue
	 * @author cis455
	 *
	 */
	class ReaderThread extends Thread {

		private File inputDir;
		private long mLine = 0;
		
		public ReaderThread(File inputDir) {
			this.inputDir = inputDir;
		}

		@Override
		public void run() {
			if(inputDir.exists() && inputDir.isDirectory()) {
				readDir(inputDir);
			}
			notifyComplete();
		}
		
		private void readDir(File dir) {
			File[] files = dir.listFiles();
			for(File file : files) {
				if(file.isFile()) {
					readFile(file);
				} else if(file.isDirectory()) {
					readDir(file);
				}
			}
		}
		
		private void readFile(File file) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line = null;
				while((line = reader.readLine()) != null) {
					String key = null;
					String value = line;
					int tab = line.indexOf('\t');
					if(tab >= 0) {
						key = line.substring(0, tab);
						value = line.substring(tab + 1);
					} else {
						key = String.valueOf(mLine++);
					}
					KeyValuePair pair = new KeyValuePair(key, value);
					mQueue.put(pair);
					if(mMapperListener != null) {
						mMapperListener.onKeyRead();
					}
					// Clear reference
					pair = null;
					key = null;
					value = null;
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				if(reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
			}
		}
		
		private void notifyComplete() {
			for(int i = 0; i < mThreadsCount; i++) {
				KeyValuePair exitObj = new KeyValuePair(true);
				try {
					mQueue.put(exitObj);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	/**
	 * Map key value pairs to given job
	 * @author cis455
	 *
	 */
	class MapperThread extends Thread {

		private Mapper mapper;
		
		public MapperThread(Mapper mapper) {
			this.mapper = mapper;
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					KeyValuePair pair = mQueue.take();
					if(pair.isExit()) {
						break;
					}
//					logger.debug("Mapping key: " + pair.getKey() + ", value: " + pair.getValue());
					mapper.map(pair.getKey(), pair.getValue(), mContext);
					if(mMapperListener != null) {
						mMapperListener.onKeyMapped();
					}
					// Clear reference
					pair = null;
				} catch (Throwable t) {
					logger.error(t.getMessage(), t);
				}
			}
			if(mapper instanceof MapReduceBase) {
				((MapReduceBase) mapper).cleanUp();
			}
			reportFinished();
		}
	}
	
	public class MapperContext extends BaseContext {
		
		public MapperContext(File outputDir) {
			super(outputDir);
		}

		private PrintWriter[] writers = null;
		private HashDivider divider;

		/**
		 * Create spool-out files for each worker
		 * @param nWorkers
		 */
		public void setup(String[] workers) {
			int nWorkers = workers.length;
			divider = new HashDivider();
			divider.setIntervals(nWorkers);
//			HashDivider.setDivisions(nWorkers); // Set # of intervals
			if(nWorkers >= 1) {
				writers = new PrintWriter[nWorkers];
			}
//			File spoolOutDir = new File(mStorageDir, WorkerServlet.DIR_SPOOL_OUT);
//			if(!spoolOutDir.exists()) {
//				spoolOutDir.mkdirs();
//			}
			// Should be created by servlet
			for(int i = 0; i < nWorkers; i++) {
				String name = getSpoolOutFileName(workers[i]);
				File file = new File(mOutputDir, name);
				try {
					file.createNewFile();
					writers[i] = new PrintWriter(new FileWriter(file, true), true);
				} catch (IOException e) {
				}
			}
		}
		
		public void close() {
			super.close();
			if(writers != null) {
				for(PrintWriter w : writers) {
					if(w != null) {
						w.close();
					}
				}
			}
		}
		
		@Override
		public void write(String key, String value) {
			int index = mapKeyToIndex(key);
			PrintWriter writer = null;
			if(index >= 0 && index < writers.length) {
				writer = writers[index];
			}
			if(writer != null) {
				synchronized (writer) {
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
				}
			}
		}
		
		private int mapKeyToIndex(String key) {
			byte[] input = null;
			try {
				input = key.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
			return divider.indexOf(input);
		}
	}
	
	static class KeyValuePair {

		private String key;
		private String value;
		private boolean exit = false;
		
		public KeyValuePair(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public KeyValuePair(boolean exit) {
			this.exit = exit;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		public boolean isExit() {
			return exit;
		}

		@Override
		public String toString() {
			return String.format("{%s=%s}", key, value);
		}
	}
}
