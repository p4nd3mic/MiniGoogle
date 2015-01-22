package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.upenn.cis455.client.HttpClient;
import edu.upenn.cis455.client.HttpGetRequest;
import edu.upenn.cis455.client.HttpPostRequest;
import edu.upenn.cis455.client.HttpRequest;
import edu.upenn.cis455.client.HttpResponse;
import edu.upenn.cis455.client.ResponseReader;
import edu.upenn.cis455.client.post.FileBody;
import edu.upenn.cis455.mapreduce.WorkerStatus;
import edu.upenn.cis455.mapreduce.worker.MapWorker.MapperListener;
import edu.upenn.cis455.mapreduce.worker.ReduceWorker.ReducerListener;
import edu.upenn.cis455.util.FileUtil;
import edu.upenn.cis455.util.StringUtil;
import edu.upenn.cis455.util.URLCodec;

public class WorkerServlet extends HttpServlet implements MapperListener, ReducerListener {

	public static final String TAG = WorkerServlet.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	static final long serialVersionUID = 455555002;
	public static final String SPOOL_IN_FILE = "in";
	public static final String OUTPUT_FILE = "output";
	public static final String DIR_SPOOL_IN = "spool-in";
	public static final String DIR_SPOOL_OUT = "spool-out";
//	public static final String DIR_JOBS = "jobs";
	public static final String ID_FILE = "id";
	
	private static final String EXTRA_PARAM_PREFIX = "param_";
	private static final int HEARTBEAT_INTERVAL = 10 * 1000; // 10 sec
//	private static final String FILE_SCHEME = "file://";
	
	private String mStorageDir;
	private String mMasterAddr;
	private int mMasterPort;
	private int mPort; // Workers listening port
	private File mSpoolOutDir = null;
	private File mSpoolInDir = null;
//	private File mJobsDir = null;
	
	private String mIpPort = null; // The index of this worker to the master
	private WorkerStatus mStatus = WorkerStatus.IDLE;
	private String mJobName = null;
	private int mKeysRead = 0;
	private int mKeysMapped = 0;
	private int mKeysToReduce = 0;
	private int mKeysReduced = 0;
	private int mKeysWritten = 0;
	private int mWorkersCount = 0;
	private int mPushDataReceived = 0; // The number of pushed data received
	
	// Http sender and reader for heartbeat
	private HttpClient mHttpClient = new HttpClient();
	private ResponseReader mReader = new ResponseReader();
	private HeartBeatThread mHeartBeatThread = null;
	
	private MapWorker mMapWorker;
	private ReduceWorker mReduceWorker;
	
	private String mId = null;


	@Override
	public void init() throws ServletException {
		mStorageDir = getInitParameter("storagedir");
		if(mStorageDir == null) {
			throw new ServletException("Cannot find storagedir parameter");
		}
		mSpoolOutDir = new File(mStorageDir, DIR_SPOOL_OUT);
		mSpoolInDir = new File(mStorageDir, DIR_SPOOL_IN);
//		mJobsDir = new File(mStorageDir, DIR_JOBS);
		readId();
		
		mMapWorker = new MapWorker(mStorageDir);
		mMapWorker.setMapperListener(this);
		mReduceWorker = new ReduceWorker(mStorageDir);
		mReduceWorker.setReducerListener(this);
		
		String master = getInitParameter("master");
		if(master == null) {
			throw new ServletException("Cannot find master parameter");
		}
		int colon = master.indexOf(':');
		if(colon >= 0) {
			mMasterAddr = master.substring(0, colon);
			mMasterPort = StringUtil.parseInt(master.substring(colon + 1), -1);
		} else {
			mMasterAddr = master;
			mMasterPort = 80;
		}
		mMapWorker.setMaster(mMasterAddr, mMasterPort);
		mReduceWorker.setMaster(mMasterAddr, mMasterPort);
		
		String port = getInitParameter("port");
		if(port == null) {
			throw new ServletException("Cannot find port parameter");
		}
		mPort = StringUtil.parseInt(port, -1);
		
		onInit();
		// Start heart beating
		mHeartBeatThread = new HeartBeatThread();
		mHeartBeatThread.start();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><head><title>Worker</title></head>");
		out.println("<body>Hi, I am the worker!</body></html>");
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();
		if("/runmap".equals(path)) { // Run map job
			handleRunMap(request, response);
			sendHeartbeat();
		} else if("/pushdata".equals(path)) { // Push data from other workers
			handlePushData(request, response);
		} else if("/runreduce".equals(path)) { // Run reduce job
			handleRunReduce(request, response);
			sendHeartbeat();
		}/* else if("/upload".equals(path)) { // Job file upload
			handleJobClassUpload(request, response);
		}*/
	}

	@Override
	public void destroy() {
		if(mHeartBeatThread != null) {
			mHeartBeatThread.interrupt();
		}
	}

	@Override
	public void onKeyRead() {
		mKeysRead++;
	}

	@Override
	public void onKeyMapped() {
		mKeysMapped++;
	}
	
	@Override
	public void onMapFinished() {
		logger.info("Map finished");
		File spoolOut = new File(mStorageDir, DIR_SPOOL_OUT);
		if(!spoolOut.exists() || !spoolOut.isDirectory()) {
			logger.info("Spool out does not exist");
			return;
		}
		// Push spool-out files
		File[] files = spoolOut.listFiles();
		for(File file : files) {
			String name = file.getName();
			String ipPort = MapWorker.getIpPort(name);
			if(ipPort.equals(mIpPort)) {
				// Own machine: Move to spool-in folder
				if(!mSpoolInDir.exists()) {
					mSpoolInDir.mkdirs();
				}
				// Move to its own spool-in
				File newFile = new File(mSpoolInDir, name);
				file.renameTo(newFile);
			} else { // Push data to other workers
				logger.info("Sending " + file.getName() + " to " + ipPort);
				sendPushData(ipPort, file);
//				new Thread(new SendPushDataTask(ipPort, file)).start();
			}
		}
		onPushDataReceived();
	}

	@Override
	public void onKeyWritten() {
		mKeysWritten++;
	}

	@Override
	public void onReduceKeyRead() {
		mKeysToReduce++;
	}
	
	@Override
	public void onKeyReduced() {
		mKeysReduced++;
	}
	
	@Override
	public void onReduceFinished() {
		logger.info("Reduce finished");
		// Clean spool-in and spool-out dir
		FileUtil.deleteDir(mSpoolInDir);
		FileUtil.deleteDir(mSpoolOutDir);
		mPushDataReceived = 0;
		
		mStatus = WorkerStatus.IDLE;
		sendHeartbeat();
	}

	/**
	 * Do things before heart beat thread is started
	 */
	protected void onInit() {}

	protected String getStorageDir() {
		return mStorageDir;
	}
	
	protected String getJobName() {
		return mJobName;
	}
	
	protected void addHeartBeatParameters(Map<String, String> params) {}
	
	protected String readString(InputStream input, int length) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if(length > 0) {
				byte[] buf = new byte[8192];
				int offset = 0;
				int read;
				do {
					read = input.read(buf, 0,
							length - offset > buf.length ? buf.length : length - offset);
					baos.write(buf, 0, read);
					offset += read;
				} while(length - offset > 0);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		byte[] bytes = baos.toByteArray();
		return new String(bytes);
	}

	@SuppressWarnings("unchecked")
	private void handleRunMap(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String jobName = null;
		String mapperClassName = null;
		String inputDir = null;
		int nThreads = 1;
		int nWorkers = StringUtil.parseInt(URLCodec.decode(request.getParameter("numWorkers")), -1);
		String[] workers = null;
		if(nWorkers != -1) {
			workers = new String[nWorkers];
		}
		
		mIpPort = request.getServerName() + ":" + request.getServerPort();
		Enumeration<String> params = request.getParameterNames();
		Map<String, String> extraParams = new HashMap<String, String>();
		while(params.hasMoreElements()) {
			String name = params.nextElement();
			String value = URLCodec.decode(request.getParameter(name));
			if("name".equals(name)) {
				jobName = value;
			} else if("mapper".equals(name)) {
				mapperClassName = value;
			} else if("input".equals(name)) {
				inputDir = value;
			} else if("numThreads".equals(name)) {
				nThreads = StringUtil.parseInt(value, -1);
			} else if(name != null && name.startsWith("worker")) {
				if(workers != null) {
					String indexStr = name.substring("worker".length());
					workers[Integer.parseInt(indexStr) - 1] = value;
				}
			} else if(name != null && name.startsWith(EXTRA_PARAM_PREFIX)) {
				String paramName = name.substring(EXTRA_PARAM_PREFIX.length());
				extraParams.put(paramName, value);
			}
		}
		
		// Check validity
		Class<?> mapperClass = null;
		try {
			mapperClass = Class.forName(mapperClassName);
		} catch (ClassNotFoundException e) {
		}
		if(mapperClass == null) {
			logger.error("Cannot find class " + mapperClassName);
			return;
		}
		
		if(inputDir == null) {
			logger.error("Cannot get input directory");
			return;
		}
		if(nThreads < 1) {
			logger.error("Invalid number of threads " + nThreads);
			return;
		}
		if(workers == null) {
			logger.error("Cannot get worker's infomation");
			return;
		}
		// Clear spool-out and spool-in dirs
//		clearDir(mSpoolInDir);
//		clearDir(mSpoolOutDir);
		
		// Start the worker
		mKeysRead = 0;
		mKeysMapped = 0;
		mKeysToReduce = 0;
		mKeysReduced = 0;
		mKeysWritten = 0;
		mWorkersCount = workers.length;
		mJobName = jobName;
		
		mMapWorker.setMapperClass(mapperClass);
		mMapWorker.setInputDir(inputDir);
		mMapWorker.setParameters(extraParams);
		mMapWorker.setThreadsCount(nThreads);
		mMapWorker.setWorkers(workers);
		if(mMapWorker.start()) {
			logger.info("Job " + mJobName +
					" started, reading from '" + inputDir + "'");
			mStatus = WorkerStatus.MAPPING;
//			sendHeartbeat();
		}
	}
	
	// Copy the push data to a temp file and pass it to mapworker
	private void handlePushData(HttpServletRequest request,
		HttpServletResponse response) throws IOException {
		String ipPort = request.getRemoteAddr() + ":" + request.getRemotePort();
		String name = MapWorker.getSpoolOutFileName(ipPort) + ".tmp";
		if(!mSpoolInDir.exists()) {
			mSpoolInDir.mkdirs();
		}
		File spoolInFile = new File(mSpoolInDir, name);
		InputStream input = request.getInputStream();
		int length = request.getContentLength();
		// Save pushed file
		logger.info("Receiving " + spoolInFile.getName());
		savePushData(input, length, spoolInFile);
//		new Thread(new ReceivePushDataTask(input, length, spoolInFile)).start();
//		savePostedFile(input, spoolInFile, length);
//		onPushDataReceived();
	}
	
	private void handleRunReduce(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String reducerClassName = URLCodec.decode(request.getParameter("reducer"));
		String outputDir = URLCodec.decode(request.getParameter("output"));
		int nThreads = StringUtil.parseInt(URLCodec.decode(request.getParameter("numThreads")), -1);
		
		Map<String, String> extraParams = new HashMap<String, String>();
		Enumeration<String> names = request.getParameterNames();
		while(names.hasMoreElements()) {
			String name = names.nextElement();
			if(name.startsWith(EXTRA_PARAM_PREFIX)) {
				String value = request.getParameter(name);
				String paramName = name.substring(EXTRA_PARAM_PREFIX.length());
				extraParams.put(paramName, value);
			}
		}
		
		// Check validity
		Class<?> reducerClass = null;
		try {
			reducerClass = Class.forName(reducerClassName);
		} catch (ClassNotFoundException e) {
		}
		if(reducerClass == null) {
			logger.error("Cannot find class " + reducerClassName);
			return;
		}
		if(outputDir == null) {
			logger.error("Output directory not specified");
			return;
		}
		if(nThreads < 1) {
			logger.error("Invalid number of threads " + nThreads);
			return;
		}
		// Start the reducer
		mReduceWorker.setReducerClass(reducerClass);
		mReduceWorker.setParameters(extraParams);
		mReduceWorker.setOutputDir(outputDir);
		mReduceWorker.setThreadsCount(nThreads);
		if(mReduceWorker.start()) {
			mStatus = WorkerStatus.REDUCING;
//			sendHeartbeat();
		}
	}
	
	private void sendHeartbeat() {
		StringBuilder sb = new StringBuilder("http://");
		sb.append(mMasterAddr);
		if(mMasterPort != 80) {
			sb.append(':').append(mMasterPort);
		}
		
		String status = mStatus != null ? mStatus.getName() : "";
		String job = mJobName != null ? mJobName : "";
		String id = mId != null ? mId : "";
		boolean noId = StringUtil.isEmpty(id);
		
		sb.append("/workerstatus")
		.append("?port=").append(mPort)
		.append("&id=").append(id) // Send with id
		.append("&status=").append(status)
		.append("&job=").append(URLCodec.encode(job))
		.append("&keysRead=").append(mKeysRead)
		.append("&keysWritten=").append(mKeysWritten)
		.append("&keysMapped=").append(mKeysMapped)
		.append("&keysToReduce=").append(mKeysToReduce)
		.append("&keysReduced=").append(mKeysReduced);
		
		// Add additional parameters
		Map<String, String> params = new HashMap<String, String>();
		addHeartBeatParameters(params);
		for(Entry<String, String> entry : params.entrySet()) {
			sb.append('&').append(EXTRA_PARAM_PREFIX)
			.append(entry.getKey()).append('=')
			.append(entry.getValue());
		}
		
		String url = sb.toString();
//		logger.info("Sending heartbeat: " + url);
		HttpRequest request = new HttpGetRequest(url);
		HttpResponse response = null;
		try {
			response = mHttpClient.execute(request);
			if(response.getStatusCode() == 200) {
				id = mReader.readText(response);
				if(noId) { // Wait for master to assign ID
					logger.info("ID assigned: " + id);
					saveId(id);
				}
			}
		} catch (Exception e) {
//			logger.error(e.getMessage());
		} finally {
			if(response != null) {
				try {
					response.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}
	
	private void onPushDataReceived() {
		mPushDataReceived++;
		if(mPushDataReceived >= mWorkersCount) {
			logger.debug("Merging spool-in files");
			// Received all push data, merge and ready to reduce
			FileUtil.mergeFiles(mSpoolInDir, SPOOL_IN_FILE);
			// Change status to waiting and send heartbeat
			mStatus = WorkerStatus.WAITING;
			sendHeartbeat();
		}
	}

	private void readId() {
		File idFile = new File(mStorageDir, ID_FILE);
		if(!idFile.exists()) {
			mId = "";
		} else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(idFile));
				mId = reader.readLine();
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
	}
	
	private void saveId(String id) {
		mId = id;
		File idFile = new File(mStorageDir, ID_FILE);
		
		PrintWriter writer = null;
		try {
			if(!idFile.exists()) {
				idFile.createNewFile();
			} else if(idFile.isDirectory()) {
				FileUtil.deleteDir(idFile);
			}
			writer = new PrintWriter(new FileWriter(idFile));
			writer.println(mId);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if(writer != null) {
				writer.close();
			}
		}
	}

	private void sendPushData(String ipPort, File file) {
		HttpClient http = new HttpClient();
		StringBuilder sb = new StringBuilder("http://");
		sb.append(ipPort);
		sb.append("/pushdata");
		String url = sb.toString();
		HttpPostRequest request = new HttpPostRequest(url);
		try {
			FileBody body = new FileBody(file);
			body.setContentType("text/plain");
			request.setPostBody(body);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			return;
		}
		HttpResponse response = null;
		try {
			response = http.execute(request);
			int statusCode = response.getStatusCode();
			if(statusCode != 200) {
				logger.error("Failed to send push data to " + ipPort);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if(response != null) {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
		logger.info("Sending push data to " + ipPort + " finished");
	}
	
	private void savePushData(InputStream input, int length,
				File file) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			if(length > 0) {
				byte[] buf = new byte[8192];
				int offset = 0;
				int read;
				do {
					read = input.read(buf, 0,
							length - offset > buf.length ? buf.length : length - offset);
					fos.write(buf, 0, read);
					offset += read;
				} while(length - offset > 0);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
			}
		}
		logger.info("Receiving " + file.getName() + " finished");
		onPushDataReceived();
	}
	
	// Thread to perform heart beat
	class HeartBeatThread extends Thread {
		
		@Override
		public void run() {
			while(true) {
				sendHeartbeat();
				try {
					Thread.sleep(HEARTBEAT_INTERVAL);
				} catch (InterruptedException e) {
					logger.info("Heart beat thread interrupted");
					break;
				}
			}
		}
	}
}
