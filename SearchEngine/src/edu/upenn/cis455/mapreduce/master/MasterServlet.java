package edu.upenn.cis455.mapreduce.master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.upenn.cis455.client.HttpClient;
import edu.upenn.cis455.client.HttpException;
import edu.upenn.cis455.client.HttpPostRequest;
import edu.upenn.cis455.client.HttpResponse;
import edu.upenn.cis455.client.post.UrlEncodedBody;
import edu.upenn.cis455.concurrency.BlockingQueue;
import edu.upenn.cis455.mapreduce.Mapper;
import edu.upenn.cis455.mapreduce.Reducer;
import edu.upenn.cis455.mapreduce.WorkerStatus;
import edu.upenn.cis455.util.Digest;
import edu.upenn.cis455.util.FileUtil;
import edu.upenn.cis455.util.StringUtil;
import edu.upenn.cis455.util.URLCodec;

public class MasterServlet extends HttpServlet {
	
	public static final String TAG = MasterServlet.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

	static final long serialVersionUID = 455555001;
	
	private static final long ACTIVE_INTERVAL = 30 * 1000; // 30 sec
	private static final String EXTRA_PARAM_PREFIX = "param_";
	
	private String mDialog = null; // Dialog message at start up
	private String mMessage = null;
	private JobInfo mJobInfo = null; // The job which is currently running
	private boolean mReduceSent = false; // Whether runreduce is sent to every workers
	private Map<String, WorkerInfo> mWorkerInfos = new TreeMap<String, WorkerInfo>();
	private Map<String, Integer> mCounters = new HashMap<String, Integer>();
	private BlockingQueue<String> mWaiter = new BlockingQueue<String>();
	private boolean mWaiting = false;
	
	private HttpClient mHttpClient = new HttpClient();

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {
		String path = request.getPathInfo();
		if("/workerstatus".equals(path)) { // Worker heartbeat
			handleWorkerStatus(request, response);
		} else if("/status".equals(path)) { // Status page
			handleStatusPage(request, response);
		} else if("/counter".equals(path)) {
			handleCounter(request, response);
		} else {
			handleContent(request, response);
		}
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String path = req.getPathInfo();
		if("/submitjob".equals(path)) { // Submit job
			handleJobSubmission(req, resp);
		}
	}
	
	/**
	 * Get currently active workers connected to the master
	 * @return
	 */
	protected List<WorkerInfo> getActiveWorkers() {
		List<WorkerInfo> list = new ArrayList<WorkerInfo>();
		Iterator<Entry<String, WorkerInfo>> iter = mWorkerInfos.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, WorkerInfo> entry = iter.next();
			WorkerInfo status = entry.getValue();
			if(status.time + ACTIVE_INTERVAL >= System.currentTimeMillis()) {
				list.add(status);
			} else {
				iter.remove();
			}
		}
//		Collections.sort(list);
		return list;
	}

	/**
	 * Get counter's value
	 * @param name Name of the counter
	 * @return The value of the counter. If not exist, it will create
	 * a new counter with initial value 0.
	 */
	protected int getCounter(String name) {
		Integer counter = mCounters.get(name);
		if(counter == null) {
			counter = 0;
			mCounters.put(name, counter);
		}
		return counter;
	}
	
	/**
	 * Increment the counter with given value
	 * @param name Name of the counter
	 * @param value Value to increase
	 * @return The value after the counter was incremented
	 */
	protected int incrementCounter(String name, int value) {
		Integer counter = mCounters.get(name);
		if(counter == null) {
			counter = 0;
		}
		int newCounter = value + counter;
		mCounters.put(name, newCounter);
		return newCounter;
	}
	
	/**
	 * Submit and job MapReduce job. This method will not block
	 * after submission.
	 * @param job The MapReduce job to run
	 * @return Whether the job starts to run successfully
	 */
	protected boolean submitJob(JobInfo job) {
//		if(mJobInfo != null) {
//			return false;
//		}
		mJobInfo = job;
		List<WorkerInfo> workers = getActiveWorkers();
		if(workers == null || workers.isEmpty()) {
			return false;
		}
		mReduceSent = false;
		// Clear counters
		mCounters.clear();
		logger.info("Submit job: " + job.getJobName());
		// POST runmap to active workers
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("name", job.getJobName());
		params.put("mapper", job.getMapperClass().getName());
		params.put("input", job.getInputDir());
		params.put("numThreads", String.valueOf(job.getMappersCount()));
		params.put("numWorkers", String.valueOf(workers.size()));
		Enumeration<String> paramNames = job.getParameterNames();
		while(paramNames.hasMoreElements()) {
			String name = paramNames.nextElement();
			String value = job.getParameter(name);
			params.put(EXTRA_PARAM_PREFIX + name, value);
		}
		for(int i = 0; i < workers.size(); i++) {
			WorkerInfo info = workers.get(i);
			String name = "worker" + (i + 1);
			String value = info.host + ":" + info.port;
			params.put(name, value);
		}
		
		HttpResponse response = null;
		boolean success = true;
		for(WorkerInfo worker : workers) {
			try {
				response = sendPostToWorker(worker, "/runmap", params);
				success = success && response.getStatusCode() == 200;
				if(!success) {
					logger.error("Cannot send runmap to worker " + worker.toString());
					break;
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
		}
		return success;
	}
	
	/**
	 * Submit the job and wait for its completion. The method will block
	 * if the job is submitted successfully until it's done
	 * @param job The MapReduce job to run
	 * @return Whether the job completes successfully
	 */
	protected boolean waitForCompletion(JobInfo job) {
		if(submitJob(job)) {
			mWaiting = true;
			try {
				mWaiter.take();
			} catch (InterruptedException e) {
			}
			mWaiting = false;
			return true;
		} else {
			logger.info("Submit job: " + job.getJobName() + " failed");
			return false;
		}
	}
	
	protected void printWorkerStatuses(PrintWriter pw) {
		pw.print("<h2>Worker Statuses</h2>");
		List<WorkerInfo> workers = getActiveWorkers();
		if(workers.size() > 0) {
			pw.print("<table border=\"1\">");
			pw.print("<tr><th>ID</th><th>IP:port</th><th>Status</th><th>Job</th>");
			pw.print("<th>Keys Read</th><th>Keys Written</th></tr>");
			for(WorkerInfo info : workers) {
				pw.print("<tr>");
				pw.print("<td>");
				pw.print(info.id);
				pw.print("</td>");
				pw.print("<td width=\"100\">");
				String ipPort = info.host + ":" + info.port;
				pw.print("<a href=\"http://");
				pw.print(ipPort);
				pw.print("\" target=\"_blank\">");
				pw.print(ipPort);
				pw.print("</a>");
				pw.print("</td>");
				pw.print("<td width=\"80\" style=\"text-align:center\">");
				String statusStr;
				WorkerStatus status = info.status;
				if(status != null) {
					statusStr = status.getName();
				} else {
					statusStr = "";
				}
				pw.print(statusStr);
				pw.print("</td>");
				pw.print("<td width=\"200\">");
				pw.print(info.job);
				pw.print("</td>");
				pw.print("<td width=\"120\" style=\"text-align:right\">");
				pw.print(info.keysRead);
				pw.print("</td>");
				pw.print("<td width=\"120\" style=\"text-align:right\">");
				pw.print(info.keysWritten);
				pw.print("</td>");
				pw.print("</tr>");
			}
			pw.print("</table>");
		} else {
			pw.print("<p>No active workers currently</p>");
		}
	}

	/**
	 * Send POST request to a given worker
	 * @param worker The worker to which the request is sent
	 * @param path Request path
	 * @param params Request parameters
	 * @return 
	 * @throws HttpException
	 */
	protected HttpResponse sendPostToWorker(WorkerInfo worker, String path,
			Map<String, String> params) throws Exception {
		StringBuilder sb = new StringBuilder("http://");
		sb.append(worker.host);
		if(worker.port != 80) {
			sb.append(':').append(worker.port);
		}
		sb.append(path);
		HttpPostRequest request = new HttpPostRequest(sb.toString());
		if(params != null) {
			request.setPostBody(new UrlEncodedBody(params));
		}
		return mHttpClient.execute(request);
	}

	protected void requestToAllWorkers(String path, Map<String, String> params) {
		List<WorkerInfo> workers = getActiveWorkers();
		HttpResponse response = null;
		for(WorkerInfo worker : workers) {
			try {
				response = sendPostToWorker(worker, path, params);
				int status = response.getStatusCode();
				if(status != 200) {
					logger.error("Request move output to " + worker.toString() + "failed. "
							+ "Status code: " + status);
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
		}
	}
	
	/**
	 * Get the currently running job
	 * @return
	 */
	protected JobInfo getRunningJob() {
		return mJobInfo;
	}
	
	private void handleWorkerStatus(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		int port = StringUtil.parseInt(URLCodec.decode(request.getParameter("port")), -1);
		if(port == -1) {
			return;
		}
		
		String addr = request.getRemoteAddr();
		String id = URLCodec.decode(request.getParameter("id"));
		if(StringUtil.isEmpty(id)) { // Need to assign id
			id = Digest.md5(addr + ":" + port).substring(0, 8);
		}
//		String key = addr + ":" + port;
//		logger.info("Received heartbeat from " + key);
		WorkerInfo info = mWorkerInfos.get(id);
		if(info == null) {
			info = new WorkerInfo();
			info.id = id;
			mWorkerInfos.put(id, info);
		}
		info.host = addr;
		info.port = port;
		String statusStr = URLCodec.decode(request.getParameter("status"));
		info.status = WorkerStatus.getStatus(statusStr);
		info.job = URLCodec.decode(request.getParameter("job"));
		info.keysRead = StringUtil.parseInt(URLCodec.decode(request.getParameter("keysRead")), -1);
		info.keysWritten = StringUtil.parseInt(URLCodec.decode(request.getParameter("keysWritten")), -1);
		info.keysMapped = StringUtil.parseInt(URLCodec.decode(request.getParameter("keysMapped")), 0);
		info.keysToReduce = StringUtil.parseInt(URLCodec.decode(request.getParameter("keysToReduce")), 0);
		info.keysReduced = StringUtil.parseInt(URLCodec.decode(request.getParameter("keysReduced")), 0);
		info.time = System.currentTimeMillis();
		
		PrintWriter pw = response.getWriter(); // Reply with ID
		response.setContentType("text/plain");
		pw.print(id);
		
		// Add extra parameters
		Enumeration<String> paramNames = request.getParameterNames();
		while(paramNames.hasMoreElements()) {
			String name = paramNames.nextElement();
			String value = URLCodec.decode(request.getParameter(name));
			if(name.startsWith(EXTRA_PARAM_PREFIX)) {
				String pname = name.substring(EXTRA_PARAM_PREFIX.length());
				info.addParameter(pname, value);
			}
		}
		
//		logger.debug("mJobInfo " + (mJobInfo != null ? "!=" : "==") + "null");
		if(mJobInfo != null) { // Handle heartbeat when doing job
			handleHeartbeat();
		}
	}

	private void handleStatusPage(HttpServletRequest request,
			HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.print("<html><body");
		if(mDialog != null) { // Show message
			pw.print(" onload=\"alert(\'");
			pw.print(mDialog);
			pw.print("\');\" >");
			mDialog = null;
		} else {
			pw.print(">");
		}
		pw.print("<h1>MapReduce Master</h1>");
		if(mMessage != null) {
			pw.print("<p>");
			pw.print(mMessage);
			pw.print("</p>");
			mMessage = null;
		}
		pw.print("<p>Author: Ziyi Yang<br/>");
		pw.print("SEAS Login: yangziyi</p>");
		pw.print("<hr/>");
		printWorkerStatuses(pw);
		pw.print("<hr/>");
		printJobForm(pw, request.getSession(false));
		pw.print("</body></html>");
	}
	
	private void handleJobSubmission(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
//		String jobClass = URLCodec.decode(request.getParameter("class"));
		String jobName = URLCodec.decode(request.getParameter("jobname"));
		String mapperClassStr = URLCodec.decode(request.getParameter("mapper"));
		String reducerClassStr = URLCodec.decode(request.getParameter("reducer"));
		String inputDir = URLCodec.decode(request.getParameter("input"));
		String outputDir = URLCodec.decode(request.getParameter("output"));
		String nMapThread = URLCodec.decode(request.getParameter("nmap"));
		String nReduceThread = URLCodec.decode(request.getParameter("nreduce"));
		String paramsStr = URLCodec.decode(request.getParameter("params"));
		
		// Check validity
		String errMsg = null;
		boolean invalid = false;
		int mappers = StringUtil.parseInt(nMapThread, -1);
		int reducers = StringUtil.parseInt(nReduceThread, -1);
		if(StringUtil.isEmpty(jobName)) {
			errMsg = "Job name is empty";
			invalid = true;
		} else if(StringUtil.isEmpty(mapperClassStr)) {
			errMsg = "Mapper class is empty";
			invalid = true;
		} else if(StringUtil.isEmpty(reducerClassStr)) {
			errMsg = "Reducer class is empty";
			invalid = true;
		} else if(StringUtil.isEmpty(inputDir)) {
			errMsg = "Input directory is empty";
			invalid = true;
		} else if(StringUtil.isEmpty(outputDir)) {
			errMsg = "Output directory is empty";
			invalid = true;
		} else if(StringUtil.isEmpty(nMapThread)) {
			errMsg = "Map thread count is empty";
			invalid = true;
		} else if(mappers == -1) {
			errMsg = "Map thread count is invalid";
			invalid = true;
		} else if(StringUtil.isEmpty(nReduceThread)) {
			errMsg = "Reduce thread count is empty";
			invalid = true;
		} else if(reducers == -1) {
			errMsg = "Reduce thread count is invalid";
			invalid = true;
		}
		
		Class<? extends Mapper> mapperClass = null;
		Class<? extends Reducer> reducerClass = null;
		try {
			mapperClass = (Class<? extends Mapper>) Class.forName(mapperClassStr);
		} catch (ClassNotFoundException e) {
			errMsg = "Cannot find class " + mapperClassStr;
			invalid = true;
		} catch (ClassCastException e) {
			errMsg = "Cannot cast class " + mapperClassStr + " to Mapper class";
			invalid = true;
		}
		try {
			reducerClass = (Class<? extends Reducer>) Class.forName(reducerClassStr);
		} catch (ClassNotFoundException e) {
			errMsg = "Cannot find class " + reducerClassStr;
			invalid = true;
		} catch (ClassCastException e) {
			errMsg = "Cannot cast class " + reducerClassStr + " to Reducer class";
			invalid = true;
		}
		
		if(invalid) {
			logger.info("Job submission failed: " + errMsg);
			mDialog = errMsg;
			response.sendRedirect("status");
			return;
		}

		List<WorkerInfo> workers = getActiveWorkers();
		if(workers.size() == 0) {
			errMsg = "Unable to start job. No active workers available";
			invalid = true;
		} else if(mJobInfo != null) {
			errMsg = "A job is running, please wait until it completes";
			invalid = true;
		}
		if(invalid) {
			response.sendRedirect("status");
			return;
		}
		// Save job info
		JobInfo job = new JobInfo();
		job.setJobName(jobName);
		job.setMapperClass(mapperClass);
		job.setReducerClass(reducerClass);
		job.setInputDir(inputDir);
		job.setOutputDir(outputDir);
		job.setMappersCount(mappers);
		job.setReducersCount(reducers);

		// Extra parameters
		if(!StringUtil.isEmpty(paramsStr)) {
			String[] paramsPairs = paramsStr.split(";");
			for(String paramPair : paramsPairs) {
				int equals = paramPair.indexOf('=');
				if(equals >= 0) {
					String pname = paramPair.substring(0, equals).trim();
					String pvalue = paramPair.substring(equals + 1).trim();
					job.addParameter(pname, pvalue);
				}
			}
		}
		
		submitJob(job);
		
		mMessage = "Job is now running...";
		response.sendRedirect("status");
	}
	
	private void handleCounter(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String name = URLCodec.decode(request.getParameter("name"));
		String incr = URLCodec.decode(request.getParameter("incr"));
		
		PrintWriter pw = response.getWriter();
		if(StringUtil.isEmpty(name)) {
			pw.print(-1);
			return;
		}
		if(StringUtil.isEmpty(incr)) {
			int counter = getCounter(name);
			pw.print(counter);
		} else {
			int value = 0;
			try {
				value = Integer.parseInt(incr);
			} catch (NumberFormatException e) {
			}
			int newCounter = incrementCounter(name, value);
			pw.print(newCounter);
		}
	}

	private void handleHeartbeat() {
		List<WorkerInfo> workers = getActiveWorkers();
		if(!mReduceSent) { // Before reduce is sent, wait for all WAITING
//			logger.debug("Reduce has not been sent");
			boolean allWaiting = true;
			for(WorkerInfo worker : workers) {
//				logger.debug(worker.toString());
				if(worker.status != WorkerStatus.WAITING) {
					allWaiting = false;
					break;
				}
			}
			if(allWaiting) { // Map done, start reducing
				logger.info("Job " + mJobInfo.getJobName() + " has finished mapping");
				mReduceSent = true;
				Map<String, String> params = new LinkedHashMap<String, String>();
				params.put("reducer", mJobInfo.getReducerClass().getName());
				params.put("output", mJobInfo.getOutputDir());
				params.put("numThreads", String.valueOf(mJobInfo.getReducersCount()));
				// Extra params
				Enumeration<String> paramNames = mJobInfo.getParameterNames();
				while(paramNames.hasMoreElements()) {
					String name = paramNames.nextElement();
					String value = mJobInfo.getParameter(name);
					params.put(EXTRA_PARAM_PREFIX + name, value);
				}
				HttpResponse response = null;
				for(WorkerInfo worker : workers) {
					try {
						response = sendPostToWorker(worker, "/runreduce", params);
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
				}
			}
		} else { // After reduce is sent, wait for all IDLE
//			logger.debug("Reduce has been sent");
			boolean allIdle = true;
			for(WorkerInfo worker : workers) {
//				logger.debug(worker.toString());
				if(worker.status != WorkerStatus.IDLE) {
					allIdle = false;
					break;
				}
			}
			if(allIdle) {
				logger.info("Job " + mJobInfo.getJobName() + " is done");
				mJobInfo = null; // Fix hanging bug
				// Refresh page
				mMessage = "Job is done!";
				mReduceSent = false;
				
				if(mWaiting) {
					onMapReduceJobDone();
				}
			}
		}
	}
	
	private void handleContent(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String path = request.getPathInfo();
		if(StringUtil.isEmpty(path) || path.equals("/")) {
			response.sendRedirect("index.html");
			return;
		}
		
		if(path.startsWith("/WEB-INF")) { // Forbidden folder
			response.sendError(403);
			return;
		}
		
		InputStream input = getServletContext().getResourceAsStream(path);
		if(input == null) { // No resource found
			response.sendError(404);
		} else {
			String contentType = FileUtil.getMIMEType(path);
			if(contentType != null) {
				response.setContentType(contentType);
			}
			
			// Output image
			if(contentType != null && contentType.startsWith("image")) {
//				FileInputStream fis = new FileInputStream(file);
				ServletOutputStream os = response.getOutputStream();
				byte[] buf = new byte[FileUtil.DEFAULT_BUFFER_SIZE];
				int len;
				while((len = input.read(buf)) != -1) {
					os.write(buf, 0, len);
				}
				input.close();
			} else {
				// Output text content
				BufferedReader reader = null;
				PrintWriter writer = response.getWriter();
				reader = new BufferedReader(new InputStreamReader(input));
				String line;
				while((line = reader.readLine()) != null) {
					writer.println(line);
				}
				reader.close();
//				writer.flush();
			}
		}
	}
	
	private void printJobForm(PrintWriter pw, HttpSession session) {
		pw.print("<h2>Job Submission</h2>");
		pw.print("<form method=\"post\" action=\"submitjob\">");
		pw.print("<table>");
		pw.print("<tr><td>Job name</td>");
		pw.print("<td><input type=\"text\" name=\"jobname\" size=\"40\"/></td></tr>");
		pw.print("<tr><td>Mapper class</td>");
		pw.print("<td><input type=\"text\" name=\"mapper\" size=\"40\"/></td></tr>");
		pw.print("<tr><td>Reducer class</td>");
		pw.print("<td><input type=\"text\" name=\"reducer\" size=\"40\"/></td></tr>");
		pw.print("<tr><td>Input directory</td>");
		pw.print("<td><input type=\"text\" name=\"input\" size=\"40\"/></td></tr>");
		pw.print("<tr><td>Output directory</td>");
		pw.print("<td><input type=\"text\" name=\"output\" size=\"40\"/></td></tr>");
		pw.print("<tr><td>Mapper threads count</td>");
		pw.print("<td style=\"text-align:right\">");
		pw.print("<input style=\"text-align:right\" type=\"number\" name=\"nmap\"");
		pw.print(" min=\"1\" size=\"10\"/></td></tr><tr><td>Reducer threads count</td>");
		pw.print("<td style=\"text-align:right\">");
		pw.print("<input style=\"text-align:right\" type=\"number\" name=\"nreduce\"");
		pw.print(" min=\"1\" size=\"10\"/></td></tr>");
		pw.print("<tr><td>Parameters</td>");
		pw.print("<td><input type=\"text\" name=\"params\" size=\"40\"");
		pw.print(" placeholder=\"Separate with semicolon\"/></td></tr></table>");
		pw.print("<input type=\"submit\" value=\"Submit\"/>");
		pw.print("</form>");
	}
	
	private void onMapReduceJobDone() {
		try {
			mWaiter.put("done");
		} catch (InterruptedException e) {
		}
	}
}
