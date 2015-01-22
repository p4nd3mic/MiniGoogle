package edu.upenn.cis455.mapreduce.worker;

import java.io.*;
import java.lang.reflect.Method;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.mapreduce.Context;

public class WorkerServlet extends HttpServlet {

	static final long serialVersionUID = 455555002;
	private String remoteAddr;
	private String relativePath;
	private ReportMessage reportMessage;
	private String portNum;
	private String spoolOut;
	private String spoolIn;

	private TaskQueue pushDataTask;
	private PushDataWorker pushDataWorker;

	private String uploadPath;

	public static boolean invalid(String parameter) {
		return (parameter == null || parameter.equals(""));
	}

	public static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					delete(fileDelete);
				}
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			file.delete();
		}
	}

	public static boolean makeDir(String fileName) {
		File directory = new File(fileName);
		if (directory.exists()) {
			try {
				delete(directory);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return directory.mkdir();
		} else {
			return directory.mkdir();
		}
	}

	public void init() throws ServletException {
		
		remoteAddr = getServletConfig().getInitParameter("master");
		relativePath = getServletConfig().getInitParameter("storagedir");
		portNum = getServletConfig().getInitParameter("port");
		uploadPath = getServletConfig().getInitParameter("uploadStorage");

		// System.out.println("portNum:" + portNum);

		ReportSingleton reportSingleton = ReportSingleton.getInstance();
		reportSingleton.setRemoteAddr(remoteAddr);
		reportSingleton.setPortNum(portNum);
		reportSingleton.startReportMessage();
		reportMessage = reportSingleton.getReportMessage();

		spoolOut = relativePath + "spool-out";
		spoolIn = relativePath + "spool-in";

		System.out.println("create spool-out: " + makeDir(spoolOut));
		System.out.println("create spool-in: " + makeDir(spoolIn));

		pushDataTask = new TaskQueue();
		pushDataWorker = new PushDataWorker(pushDataTask);
		PushDataWorker.createTempFile(spoolIn);
		pushDataWorker.start();

		System.out.println("init finish..");
	}

	public Object invokeClass(String class_name) throws Exception {
		Class jobClass = Class.forName(class_name);
		Object jobObject = jobClass.newInstance();
		return jobObject;
	}

	public Method invokeMethod(String class_name, boolean isMap)
			throws Exception {

		Method method = null;
		if (isMap) {
			method = Class.forName(class_name).getMethod("map", String.class,
					String.class, Context.class);
		} else {
			method = Class.forName(class_name).getMethod("reduce",
					String.class, String[].class, Context.class);
		}
		return method;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><head><title>Worker</title><body>Hi, I'm worker</body></head>");
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getRequestURI();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		// Run map function -- begin
		if (path.endsWith("/runmap")) {

			DBSingleton.setDbPath(getServletConfig().getInitParameter(
					"berkeleyPath"));

			System.out.println("NOW begin to runmap!");
			// makeDir(spoolOut);
			// makeDir(spoolIn);
			// PushDataWorker.createTempFile(spoolIn);

			String class_name = null;
			String input_path = null;
			String num_map = null;
			String workinfo = null;
			String numWorkers = null;

			BufferedReader reader = request.getReader();
			StringBuffer jb = new StringBuffer();
			String lineBody = null;
			while ((lineBody = reader.readLine()) != null) {
				jb.append(lineBody);
			}

			String content = jb.toString();
			String[] parameters = content.split("&");

			for (String parameter : parameters) {
				String[] parts = parameter.split("=");
				String key = parts[0];
				String value = parts[1];
				if (key.equals("class_name")) {
					class_name = value;
					// System.out.println("class_name: " + class_name);
				} else if (key.equals("input_path")) {
					input_path = value;
					// System.out.println("output_path: " + output_path);
				} else if (key.equals("num_map")) {
					num_map = value;
					// System.out.println("num_reduce: " + num_reduce);
				} else if (key.equals("workinfo")) {
					workinfo = value;
				} else if (key.equals("numWorkers")) {
					numWorkers = value;
				}
			}

			if (invalid(class_name) || invalid(input_path) || invalid(num_map)
					|| invalid(workinfo) || invalid(numWorkers)) {
				out.println("<html>");
				out.println("<body>");
				out.println("<div>Wrong input</div>");
				out.println("</body>");
				out.println("</html>");
				out.close();
				return;
			}
			int numberWorker = 0;
			try {
				numberWorker = Integer.valueOf(numWorkers);
			} catch (NumberFormatException e1) {
				out.println("<html>");
				out.println("<body>");
				out.println("<div>Wrong input</div>");
				out.println("</body>");
				out.println("</html>");
				out.close();
				return;
			}

			Object objectJob = null;
			Method mapMethod = null;
			MapContext context = null;
			try {
				objectJob = invokeClass(class_name);
				mapMethod = invokeMethod(class_name, true);
				context = new MapContext(spoolOut);
			} catch (Exception e) {
				System.out.println("Cannot get mapMethod!");
				e.printStackTrace();
			}

			String inputDirPath = relativePath + input_path;

			File directory = new File(inputDirPath);
			if (!directory.exists()) {
				out.println("<html>");
				out.println("<body>");
				out.println("<div>File does not exist</div>");
				out.println("</body>");
				out.println("</html>");
				out.close();
				return;
			}
			// All Things Are Ready Here!
			out.close();

			reportMessage.setKeysRead(0);
			reportMessage.setKeysWritten(0);

			TaskQueue mapTaskQueue = new TaskQueue();
			MapMasterWorker master = new MapMasterWorker(mapTaskQueue, num_map,
					workinfo, numberWorker, spoolOut, objectJob, mapMethod,
					context);
			master.start();

			reportMessage.setStatus("mapping");
			reportMessage.setJob(class_name);

			final File folder = new File(inputDirPath);
			HashText.ReadAllFilesInFolder(folder, mapTaskQueue);

			master.setPreviousDone();
			try {
				master.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			reportMessage.setStatus("waiting");
			reportMessage.interrupt();

		}
		// Run map function -- end

		// Run reduce function -- begin
		if (path.endsWith("/runreduce")) {
			String class_name = null;
			String output_path = null;
			String num_reduce = null;

			BufferedReader reader = request.getReader();
			StringBuffer jb = new StringBuffer();
			String lineBody = null;
			while ((lineBody = reader.readLine()) != null) {
				jb.append(lineBody);
			}

			String content = jb.toString();
			String[] parameters = content.split("&");

			for (String parameter : parameters) {
				String[] parts = parameter.split("=");
				String key = parts[0];
				String value = parts[1];
				if (key.equals("class_name")) {
					class_name = value;
					// System.out.println("class_name: " + class_name);
				} else if (key.equals("output_path")) {
					output_path = value;
					// System.out.println("output_path: " + output_path);
				} else if (key.equals("num_reduce")) {
					num_reduce = value;
					// System.out.println("num_reduce: " + num_reduce);
				}
			}

			if (invalid(class_name) || invalid(output_path)
					|| invalid(num_reduce)) {
				out.println("<html>");
				out.println("<body>");
				out.println("<div>Wrong input</div>");
				out.println("</body>");
				out.println("</html>");
				out.close();
				return;
			}
			out.close();
			String outputDirPath = relativePath + output_path;
			Object objectJob = null;
			Method reduceMethod = null;
			ReduceContext context = null;
			try {
				objectJob = invokeClass(class_name);
				reduceMethod = invokeMethod(class_name, false);
				context = new ReduceContext(outputDirPath);
			} catch (Exception e) {
				System.out.println("Cannot get mapMethod!");
				e.printStackTrace();
			}

			reportMessage.setKeysRead(0);
			reportMessage.setKeysWritten(0);
			reportMessage.setStatus("reducing");
			reportMessage.setJob(class_name);

			try {
				final Process proc = Runtime.getRuntime().exec(
						"sort " + spoolIn + "/temp.txt");

				final BufferedReader outputReader = new BufferedReader(
						new InputStreamReader(proc.getInputStream()));
				final BufferedReader errorReader = new BufferedReader(
						new InputStreamReader(proc.getErrorStream()));

				TaskQueue reduceTaskQueue = new TaskQueue();
				ReduceMasterWorker master = new ReduceMasterWorker(
						reduceTaskQueue, num_reduce, objectJob, reduceMethod,
						context);
				master.start();

				String line = null;
				while ((line = outputReader.readLine()) != null) {
					reduceTaskQueue.addLine(line);
				}
				master.setPreviousDone();
				master.join();

				while ((line = errorReader.readLine()) != null) {
					System.err.println(line);
				}

			} catch (final IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			reportMessage.setJob("NoJob");
			reportMessage.setStatus("idle");
			reportMessage.interrupt();
			// out.close();
			makeDir(spoolOut);
			makeDir(spoolIn);
			
			DBSingleton.getInstance().closeBDBstore();
		}
		// Run reduce function -- end

		// Store the data -- begin
		if (path.endsWith("/pushdata")) {
			System.out.println("/pushdata invoked");

			BufferedReader reader = request.getReader();
			StringBuffer jb = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				jb.append(line + "\n");
			}

			String content = jb.toString();
//			System.out.println("/pushdata content: "
//					+ content.substring(0, 2));
			try {
				pushDataTask.addLine(content);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			out.close();
		}
		// Store the data -- end

		// receive file -- begin
		if (path.endsWith("/receive")) {
			InputStream reader = request.getInputStream();
			String fileName = request.getHeader("File-Name");
			System.out.println("upload fileName: " + fileName);
			String DestinationPath = uploadPath + fileName;

			File temp = new File(DestinationPath);
			try {
				temp.createNewFile();
			} catch (IOException e) {
				System.out.println("Cannot create file");
				e.printStackTrace();
			}

			OutputStream outputStream = new FileOutputStream(temp);
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = reader.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.close();

		}
		// receive file -- end
	}
}
