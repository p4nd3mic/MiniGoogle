package edu.upenn.cis455.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import edu.upenn.cis455.client.post.PostBody;

public class HttpClient {

	public static final String TAG = HttpClient.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private static final String CRLF = "\r\n";
	private static final String VERSION = "HTTP/1.1";
	private static final String CONTENT_LENGTH = "Content-Length";
	private static final String CONTENT_TYPE = "Content-Type";
	
	private int mConnTimeout = 30 * 1000;
	private int mTimeout = 0;
	private boolean mKeepAlive = false;
	
	public HttpResponse execute(HttpRequest request)
			throws IOException {
		if(request == null) {
			throw new NullPointerException("Request is null");
		}
		
		String scheme = request.getScheme();
		if(!"http".equals(scheme)) {
			throw new IllegalArgumentException("Unsupported protocol: " + scheme);
		}
		
		HttpResponseImpl response = null;
		String host = request.getHost();
		int port = request.getPort();
		Socket socket = null;
		try {
			socket = new Socket();
			socket.setSoTimeout(mTimeout);
			InetSocketAddress addr = new InetSocketAddress(host, port);
			socket.connect(addr, mConnTimeout);
			
//			if("localhost".equalsIgnoreCase(host)) {
//				socket = new Socket(InetAddress.getLocalHost(), port);
//			} else {
//				socket = new Socket(host, port);
//			}
//			socket.setSoTimeout(mTimeout);
			writeRequest(socket, request);
			
			response = new HttpResponseImpl();
			response.setSocket(socket);
			readResponse(socket, response);
		} catch (SocketTimeoutException e) { // Time out, close socket
			logger.error("Time out when connecting: " + request.getUrl());
			if(socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
				}
			}
//			return null;
		} catch (Exception e) {
			if(socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
				}
			}
			throw e;
		}
		return response;
	}
	
	public void setConnectionTimeout(int timeoutMilli) {
		this.mConnTimeout = timeoutMilli;
	}
	
	/**
	 * Set read time out in millisec
	 * @param timeoutMilli
	 */
	public void setTimeout(int timeoutMilli) {
		this.mTimeout = timeoutMilli;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.mKeepAlive = keepAlive;
	}

	private void writeRequest(Socket socket, HttpRequest request)
			throws IOException {
		String method = request.getMethod();
		String path = request.getPath();
		String host = request.getHost();
		int port = request.getPort();
		
		PostBody postBody = null;
		
		if("POST".equals(method)) {
			HttpPostRequest postRequest = (HttpPostRequest) request;
			postBody = postRequest.getPostBody();
			if(postBody != null) {
				if(!request.hasHeader(CONTENT_TYPE) && postBody.getContentType() != null) {
					request.addHeader(CONTENT_TYPE, postBody.getContentType());
				}
				if(!request.hasHeader(CONTENT_LENGTH)) {
					request.addHeader(CONTENT_LENGTH, String.valueOf(postBody.getContentLength()));
				}
			}
		}
		
		if(!request.hasHeader("Host")) {
			String hostHeader = host;
			if(port != 80) {
				hostHeader += ":" + port;
			}
			request.addHeader("Host", hostHeader);
		}
		if(!mKeepAlive) {
			// Close connection
			request.addHeader("Connection", "close");
		}
		
		OutputStream os = socket.getOutputStream();
		PrintWriter writer = new PrintWriter(os);
		StringBuilder sb = new StringBuilder();
		
		// Write request line
		sb.append(method).append(' ')
		.append(path).append(' ')
		.append(VERSION).append(CRLF);
		
		// Write headers
		Enumeration<String> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			String value = request.getHeader(name);
			sb.append(name).append(": ")
			.append(value).append(CRLF);
		}
		sb.append(CRLF);
		
		writer.print(sb.toString());
		writer.flush();
		
		if(postBody != null) { // Write post body
			InputStream input = postBody.getContent();
			if(input != null) {
				DataInputStream dis = null;
				try {
					dis = new DataInputStream(input);
					DataOutputStream dos = new DataOutputStream(os);
					byte[] buf = new byte[8192];
					int len;
					while((len = dis.read(buf)) != -1) {
						dos.write(buf, 0, len);
					}
				} finally {
					if(dis != null) {
						dis.close();
					}
				}
			}
		}
	}
	
	private void readResponse(Socket socket, HttpResponseImpl response)
			throws IOException {
		// Read response
		InputStream input = socket.getInputStream();
		response.setContent(input);
		LineReader reader = new LineReader(input);
		
		String responseLine = reader.readLine();
		parseResponse(responseLine, response);
		String line = null;
		while((line = reader.readLine()) != null) {
			if(!line.isEmpty()) {
				parseHeader(line, response);
			} else {
				break;
			}
		}
	}
	
	private static void parseResponse(String responseLine, HttpResponseImpl response) {
		response.setStatusCode(getStatusCode(responseLine));
	}

	private static void parseHeader(String headerLine, HttpResponseImpl response) {
		int colon = headerLine.indexOf(':');
		if(colon >= 0) {
			String name = headerLine.substring(0, colon).trim().toLowerCase();
			String value = headerLine.substring(colon + 1).trim();
			response.addHeader(name, value);
		}
	}
	
	private static int getStatusCode(String responseLine) {
		if(responseLine == null) {
			return -1;
		}
		int start = 0, end = 0;
		for(int i = 0; i < responseLine.length(); i++) {
			if(responseLine.charAt(i) == ' ') {
				start = i + 1;
				break;
			}
		}
		
		for(int i = start; i < responseLine.length(); i++) {
			if(responseLine.charAt(i) == ' ') {
				end = i;
				break;
			}
		}
		if(start < end) {
			return Integer.parseInt(responseLine.substring(start, end));
		} else {
			return -1;
		}
	}
}
