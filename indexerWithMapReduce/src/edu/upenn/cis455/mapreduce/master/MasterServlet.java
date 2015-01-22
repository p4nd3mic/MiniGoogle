package edu.upenn.cis455.mapreduce.master;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.*;
//import javax.servlet.annotation.MultipartConfig;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

//@MultipartConfig
public class MasterServlet extends HttpServlet {

	private static String convertDateToStr(Date date) throws Exception {
		SimpleDateFormat parseDate = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss zzz");
		parseDate.setTimeZone(TimeZone.getTimeZone("GMT"));
		return parseDate.format(date);
	}

	public static Date convertStrToDate(String str) {
		Date date = null;
		SimpleDateFormat parseDate = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss zzz");
		try {
			date = parseDate.parse(str);
		} catch (ParseException e) {
			parseDate = new SimpleDateFormat("EEEEE, dd-MMM-yy HH:mm:ss zzz");
			try {
				date = parseDate.parse(str);
			} catch (ParseException e1) {
				parseDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

				try {
					date = parseDate.parse(str);
				} catch (ParseException e2) {
					System.err.println("Wrong data format !");
				}
			}
		}
		return date;
	}

	public static boolean invalid(String parameter) {
		return (parameter == null || parameter.equals(""));
	}

	static final long serialVersionUID = 455555001;
	private static ConcurrentHashMap<String, HashMap<String, String>> workerStatus = workerInfo
			.getInstance().getWorkerStatus();
	private SendMessage sendMessage;
	private SendMessageAgain sendMessageAgain;
	private boolean allIdle = true;

	private String uploadPath;

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
		uploadPath = getServletConfig().getInitParameter("uploadStorage");
		// System.out.println("uploadPath:" + uploadPath);
		if (uploadPath != null) {
			System.out.println("create uploadStorage: " + makeDir(uploadPath));
		}
		System.out.println("master init finish..");
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {
		String path = request.getRequestURI();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		// Accept work status -- begin
		if (path.endsWith("/workerstatus")) {
			String address = request.getRemoteAddr();
			// System.out.println("address: " + address);
			String port = request.getParameter("port");
			// System.out.println("port: " + port);
			String status = request.getParameter("status");
			String job = request.getParameter("job");
			String keysRead = request.getParameter("keysRead");
			String keysWritten = request.getParameter("keysWritten");
			String date = null;
			try {
				date = convertDateToStr(new Date());
				// System.out.println(date);
			} catch (Exception e) {
				System.err.println("cannot convert date to str");
				e.printStackTrace();
			}

			String addressPort = address + ":" + port;
			System.out.println("address:port gonna update:" + addressPort);
			HashMap<String, String> info = new HashMap<String, String>();
			info.put("status", status);
			info.put("job", job);
			info.put("keysRead", keysRead);
			info.put("keysWritten", keysWritten);
			info.put("date", date);
			System.out.println("put workerStatus: " + addressPort +" : "+info);
			workerStatus.put(addressPort, info);
			System.out.println("workerStatus: " + workerStatus);
			
			boolean allWaiting = true;
			for (HashMap<String, String> value : workerStatus.values()) {
				System.out.println("waiting value: " + value);
				
				if (!value.get("status").equals("waiting")) {
					allWaiting = false;
					break;
				}
				
			}

			if (allWaiting && sendMessageAgain != null
					&& sendMessageAgain.getState() == Thread.State.NEW) {
				System.out.println("sendMessageAgainLOOP: "
						+ sendMessageAgain.getState());
				sendMessageAgain.start();
			}
			System.out.println("go on...");

			for (HashMap<String, String> value : workerStatus.values()) {
				if (!value.get("status").equals("idle")) {
					allIdle = false;
					break;
				}
				allIdle = true;
			}
		}
		// Accept work status -- end

		// Submit job and show status of workers -- begin
		if (path.endsWith("/status")) {
			out.print("<html>");
			out.print("<body>");

			// Display the info of workers
			out.print("<table width=\"100%\">");
			out.print("<tr>");
			out.print("<td>");
			out.print("IP:port");
			out.print("</td>");
			out.print("<td>");
			out.print("status");
			out.print("</td>");
			out.print("<td>");
			out.print("job");
			out.print("</td>");
			out.print("<td>");
			out.print("keysRead");
			out.print("</td>");
			out.print("<td>");
			out.print("keysWritten");
			out.print("</td>");
			out.print("</tr>");

			for (Map.Entry<String, HashMap<String, String>> entry : workerStatus
					.entrySet()) {
				String addressPort = entry.getKey();
				HashMap<String, String> info = entry.getValue();
				long currTime = new Date().getTime();
				long lastPost = 0;
				if (info.containsKey("date")) {
					lastPost = convertStrToDate(info.get("date")).getTime();
				}
				// System.out.println((lastPost + 30 * 1000) +" : "+ currTime);
				if (lastPost + 30 * 1000 < currTime) {
					workerStatus.remove(addressPort);
					continue;
				}

				out.print("<tr>");
				out.print("<td>");
				out.print(addressPort);
				out.print("</td>");
				for (String key : info.keySet()) {
					if (!key.equals("date")) {
						out.print("<td>");
						out.print(info.get(key));
						out.print("</td>");
					}
				}
				out.print("</tr>");
			}
			out.print("</table>");
			out.print("<br>");
			out.print("<br>");
			out.print("<br>");
			// Display the form for submitting the job
			if (allIdle) {
				out.print("<form action=\"/ready\" method=\"POST\">");

				out.print("The class name of the job: ");
				out.print("<input type=text size=50 name=class_name><br>");

				out.print("The input directory: ");
				out.print("<input type=text size=50 name=input_path><br>");

				out.print("The output directory: ");
				out.print("<input type=text size=50 name=output_path><br>");

				out.print("The number of map threads: ");
				out.print("<input type=text size=50 name=num_map><br>");

				out.print("The number of reduce threads: ");
				out.print("<input type=text size=50 name=num_reduce><br>");

				out.print("<input type=submit value = \"submit\">");
				out.print("</form>");
			} else {
				out.print("<div>Job is running currently, you cannot submit a job now</div>");
			}

			out.print("</body>");
			out.print("</html>");
		}
		// Submit job and show status of workers -- end

		// Upload the file -- begin
		if (path.endsWith("/upload")) {
			out.println("<html>");
			out.println("<body>");
			out.print("<form action=\"/upload\" method=\"POST\" enctype=\"multipart/form-data\">");
			out.print("<input name=\"file\" type=\"file\" size=\"50\">");
			out.print("<input name=\"submit\" type=\"submit\" value=\"submit\">");
			out.print("</form>");
			out.println("</body>");
			out.println("</html>");
		}
		// Upload the file -- end
		
		out.println("<html><head><title>Master</title><body>Hi, I'm Master</body></head>");

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String class_name = null;
		String input_path = null;
		String output_path = null;
		String num_map = null;
		String num_reduce = null;

		String path = request.getRequestURI();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		// send job parameter to both mapWorker and reduceWorker -- begin
		if (path.endsWith("/ready")) {
			class_name = request.getParameter("class_name");
			input_path = request.getParameter("input_path");
			output_path = request.getParameter("output_path");
			num_map = request.getParameter("num_map");
			num_reduce = request.getParameter("num_reduce");

			if (invalid(class_name) || invalid(input_path)
					|| invalid(output_path) || invalid(num_map)
					|| invalid(num_reduce)) {
				out.println("<html>");
				out.println("<body>");
				out.println("<div>Wrong input</div>");
				out.println("</body>");
				out.println("</html>");
				out.close();
				return;
			}

			int numWorkers = workerStatus.size();

			StringBuilder workerInfoSerial = new StringBuilder();

			int numTest = 0;

			for (String addressPort : workerStatus.keySet()) {
				numTest++;
				String onePiece = "worker" + numTest + "%3D" + addressPort;
				if (numTest < numWorkers) {
					onePiece = onePiece + "%3B";
				}
				workerInfoSerial.append(onePiece);
			}
			System.out.println("numTest == numWorkers: "
					+ (numTest == numWorkers));

			sendMessage = new SendMessage(class_name, input_path, num_map,
					workerInfoSerial.toString(), numWorkers);

			sendMessageAgain = new SendMessageAgain(class_name, output_path,
					num_reduce);
			System.out.println("sendMessageAgainSTART: "
					+ sendMessageAgain.getState());
			sendMessage.start();

			response.sendRedirect(response
					.encodeRedirectURL("/status"));
		}
		// send job parameter to both mapWorker and reduceWorker -- end

		// store the upload file -- begin
//		if (path.endsWith("/upload")) {
//			Part filePart = request.getPart("file");
//			String filename = getFilename(filePart);
//
//			if (invalid(filename)) {
//				out.println("<html>");
//				out.println("<body>");
//				out.println("<div>Wrong input</div>");
//				out.println("</body>");
//				out.println("</html>");
//				out.close();
//				return;
//			}
//
//			InputStream filecontent = filePart.getInputStream();
//			// System.out.println("/upload uploadPath: " + uploadPath);
//			File temp = new File(uploadPath + filename);
//			try {
//				if (!temp.createNewFile()) {
//					temp.delete();
//					System.out.println(temp.createNewFile());
//				}
//			} catch (IOException e) {
//				System.out.println("Cannot create file");
//				e.printStackTrace();
//			}
//			OutputStream outputStream = new FileOutputStream(temp);
//			int read = 0;
//			byte[] bytes = new byte[1024];
//			while ((read = filecontent.read(bytes)) != -1) {
//				outputStream.write(bytes, 0, read);
//			}
//			outputStream.close();
//
//			for (String address : workerStatus.keySet()) {
//				try {
//					if (!address.startsWith("http://")) {
//						address = "http://" + address;
//					}
//					System.out.println("upload file to workers: " + address
//							+ "/receive");
//					uploadFilePost(address + "/receive", filename);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			out.println("<html>");
//			out.println("<body>");
//			out.println("<div>Upload succeed</div>");
//			out.println("</body>");
//			out.println("</html>");
//			out.close();
//		}
		// store the upload file -- end
		
		
	}

	// upload helper function -- begin
//	private static String getFilename(Part part) {
//		for (String cd : part.getHeader("content-disposition").split(";")) {
//			if (cd.trim().startsWith("filename")) {
//				String filename = cd.substring(cd.indexOf('=') + 1).trim()
//						.replace("\"", "");
//				return filename.substring(filename.lastIndexOf('/') + 1)
//						.substring(filename.lastIndexOf('\\') + 1);
//			}
//		}
//		return null;
//	}
//
//	// HTTP POST request, sends data in filename to the hostUrl
//	private void uploadFilePost(String hostUrl, String filename)
//			throws Exception {
//
//		URL url = new URL(hostUrl);
//		HttpURLConnection con = (HttpURLConnection) url.openConnection();
//
//		// add reuqest header
//		con.setRequestMethod("POST");
//		con.setRequestProperty("File-Name", filename);
//		// Send post request
//		con.setDoOutput(true);
//		DataOutputStream remoteStream = new DataOutputStream(
//				con.getOutputStream());
//
//		byte[] fileBuffer = new byte[1024];
//		FileInputStream partFile = new FileInputStream(uploadPath + filename);
//		BufferedInputStream bufferedStream = new BufferedInputStream(partFile);
//
//		// read from local filePart file and write to remote server
//		int bytesRead = -1;
//		while ((bytesRead = bufferedStream.read(fileBuffer)) != -1) {
//			remoteStream.write(fileBuffer, 0, bytesRead);
//		}
//
//		bufferedStream.close();
//
//		remoteStream.flush();
//		remoteStream.close();
//
//		int responseCode = con.getResponseCode();
//		System.out.println("\nSending 'POST' request to URL : " + hostUrl);
//		System.out.println("Response Code : " + responseCode);
//	}
	// upload helper function -- end
}
