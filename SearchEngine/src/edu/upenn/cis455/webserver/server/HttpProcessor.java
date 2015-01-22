package edu.upenn.cis455.webserver.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.upenn.cis455.webserver.Const;
import edu.upenn.cis455.webserver.exception.BadRequestException;
import edu.upenn.cis455.webserver.servlet.Headers;
import edu.upenn.cis455.webserver.servlet.Headers.Header;
import edu.upenn.cis455.webserver.servlet.Request;
import edu.upenn.cis455.webserver.servlet.Response;
import edu.upenn.cis455.webserver.servlet.app.WebAppManager;
import edu.upenn.cis455.webserver.servlet.app.WebAppManager.ServletPathInfo;

public class HttpProcessor implements ActionListener {

	public static final String TAG = HttpProcessor.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private ServerInstance mServer;
	private Socket mSocket;
	private Request mRequest;
	private Response mResponse;
	
	private RequestReader mRequestReader;
	private ResponseSender mResponseSender;
	
	private boolean mHttp11 = false;
	private boolean mError = false;
	private boolean mKeepAlive = false;
	private boolean mExpecting = false;
	
	private String mProcessingUrl = null;
	
	public HttpProcessor(ServerInstance server) {
		mServer = server;
		
		mRequest = new Request();
		mRequestReader = new RequestReader(mRequest);
		mRequestReader.setDebug(mServer.isDebug());
		
		mResponse = new Response();
		mRequest.setResponse(mResponse);
		mResponse.setActionListener(this);
		mResponseSender = new ResponseSender(mResponse);
	}

	public void process(Socket socket) throws IOException {
		mSocket = socket;
		mRequestReader.setInputStream(mSocket.getInputStream());
		mResponseSender.setOutputStream(mSocket.getOutputStream());
		
		mError = false;
		mKeepAlive = true;
		mExpecting = false;
		
		int timeout = mServer.getConfig().getSocketTimeout();
		try {
			mSocket.setSoTimeout(timeout);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			mError = true;
		}
		
		while(mKeepAlive && !mError) {
			try {
				mRequestReader.readRequestLine();
				mProcessingUrl = mRequest.getRequestURI();
				setServerParams();
				mRequestReader.readHeaders();
			} catch (SocketTimeoutException e) {
				mError = true;
				printDebug("Socket time out");
				break;
			} catch (IOException e) {
				mError = true;
				logger.error(e.getMessage(), e);
				break;
			} catch (BadRequestException e) {
				mError = true;
				mResponse.setStatus(Const.HTTP_BAD_REQUEST);
			}
			
			if(!mError) {
				prepareRequest();
			}
			
			if(!mError) {
				try {
					doService();
				} catch (IOException e) {
					mError = true;
					logger.error(e.getMessage(), e);
				} catch (Throwable e) {
					mError = true;
					logger.error(e.getMessage(), e);
					mResponse.setStatus(Const.HTTP_INTERNAL_ERROR);
				}
			}
			
			try {
				handleErrors();
			} catch (IOException e) {
			}
			
			try {
				mResponse.finishResponse();
				mResponseSender.finish();
			} catch (IOException io) {
				mError = true;
			}
			
			mRequestReader.nextRequest();
			mResponseSender.nextRequest();
			mProcessingUrl = null;
		}
		
		mRequest.recycle();
		mResponse.recycle();
		mSocket = null;
//		closeSocket();
	}

	@Override
	public void sendContinue() {
		try {
			mResponseSender.sendContinue();
		} catch (IOException e) {
			mError = true;
		}
	}

	@Override
	public void commit() {
		if(mResponse.isCommitted()) {
			return;
		}
		prepareResponse();
		try {
			mResponseSender.commit();
		} catch (IOException e) {
			mError = true;
		}
	}

	public String getProcessingUrl() {
		return mProcessingUrl;
	}

//	private void closeSocket() {
//		if(mSocket != null) {
//			try {
//				mSocket.close();
//			} catch (IOException e) {
//				logger.error(e.getMessage(), e);
//			}
//		}
//	}

	private void prepareRequest() {
		String protocol = mRequest.getProtocol();
		// Check protocol
		if(Const.VERSION_1_0.equals(protocol)) {
			mHttp11 = false;
			mKeepAlive = false;
		} else if(Const.VERSION_1_1.equals(protocol)) {
			mHttp11 = true;
		} else {
			mError = true;
			mResponse.setStatus(Const.HTTP_VERSION_NOT_SUPPORTED);
		}
		// Check Connection header
		String connection = mRequest.getHeader("Connection");
		if(connection != null) {
			if("close".equalsIgnoreCase(connection)) {
				mKeepAlive = false;
			} else if("keep-alive".equalsIgnoreCase(connection)) {
				mKeepAlive = true;
			}
		}
		// Check expecting header
		if(mHttp11) {
			String expect = mRequest.getHeader("Expect");
			if("100-continue".equalsIgnoreCase(expect)) {
				mExpecting = true;
			}
		}
		// Check host header
		String host = mRequest.getHeader("Host");
		if(mHttp11 && host == null) {
			mError = true;
			mResponse.setStatus(Const.HTTP_BAD_REQUEST);
		}
		parseHostHeader(host);
		// Parse cookie header
		String cookie = mRequest.getHeader("Cookie");
		parseCookieHeader(cookie);
	}
	
	private void prepareResponse() {
		Headers headers = mResponse.getHeaders();
		String date = headers.getHeader("Date");
		if(date == null) {
			mResponse.addDateHeader("Date", System.currentTimeMillis());
		}
		
		String server = headers.getHeader("Server");
		if(server == null) {
			server = mServer.getServerInfo();
			mResponse.addHeader("Server", server);
		}
		
		int contentLength = mResponse.getContentLength();
		if(contentLength == -1 && mHttp11) { // Chunked
			mResponse.setHeader(Const.HEADER_TRANSFER_ENCODING, "chunked");
		}
		
		int status = mResponse.getStatus();
		mKeepAlive = mKeepAlive && !shouldCloseConnection(status);
		if(mKeepAlive) {
			if(!mHttp11 && !mError) {
				mResponse.setHeader("Connection", "Keep-Alive");
			}
		} else {
			mResponse.setHeader("Connection", "close");
		}
		
		mResponseSender.writeStatus();
		
		List<Header> list = headers.getList();
		for(Header header : list) {
			mResponseSender.writeHeader(header.getName(), header.getValues());
		}
		mResponseSender.endHeaders();
	}

	private void setServerParams() {
		if(mSocket == null) {
			return;
		}
		InetSocketAddress localAddr = (InetSocketAddress) mSocket.getLocalSocketAddress();
		mRequest.setServerName(localAddr.getHostString());
		mRequest.setServerPort(localAddr.getPort());
		mRequest.setLocalAddr(localAddr.getHostString());
		mRequest.setLocalPort(localAddr.getPort());
		
		InetSocketAddress remoteAddr = (InetSocketAddress) mSocket.getRemoteSocketAddress();
		mRequest.setRemoteAddr(remoteAddr.getHostString());
		mRequest.setRemoteHost(remoteAddr.getHostString());
		mRequest.setRemotePort(remoteAddr.getPort());
	}

	private void parseHostHeader(String hostValue) {
		if(hostValue == null) {
			return;
		}
		String name = hostValue;
		int port = 80;
		int index = hostValue.indexOf(':');
		if(index != -1) {
			name = hostValue.substring(0, index).trim();
			try {
				port = Integer.parseInt(hostValue.substring(index + 1).trim());
			} catch (NumberFormatException e) {
				logger.error(e.getMessage(), e);
			}
		}
		mRequest.setServerName(name);
		mRequest.setServerPort(port);
	}
	
	private void parseCookieHeader(String cookieHeader) {
		if(cookieHeader == null) {
			return;
		}
		String[] cookies = cookieHeader.split(";");
		for(String cookieStr : cookies) {
			cookieStr = cookieStr.trim();
			int equals = cookieStr.indexOf('=');
			if(equals != -1) {
				String name = cookieStr.substring(0, equals).trim();
				String value = cookieStr.substring(equals + 1).trim();
				Cookie cookie = new Cookie(name, value);
				mRequest.addCookie(cookie);
				
				if("JSESSIONID".equalsIgnoreCase(name)) { // Session cookie
					mRequest.setRequestedSessionId(value);
					mRequest.setSessionFromCookie(true);
				}
			}
		}
	}
	
	private void doService() throws ServletException, IOException {
		if(mExpecting) {
			mResponse.sendContinue();
		}
		
		WebAppManager manager = mServer.getAppManager();
		ServletPathInfo info = manager.searchRequestSevlet(mRequest.getRequestURI());
		HttpServlet servlet = null;
		if(info != null) { // Servlet request
			mRequest.setContextPath(info.contextPath);
			mRequest.setServletPath(info.servletPath);
			mRequest.setPathInfo(info.pathInfo);
			mRequest.setContext(info.context);
			servlet = info.servlet;
		}
		if(servlet != null) {
			try {
				servlet.service(mRequest, mResponse);
			} catch (RuntimeException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private boolean shouldCloseConnection(int status) {
        return status == Const.HTTP_BAD_REQUEST ||
               status == Const.HTTP_INTERNAL_ERROR ||
               status == Const.HTTP_NOT_IMPLEMENTED;
    }
	
	private void handleErrors() throws IOException {
		if(mResponse.isCommitted()) {
			return;
		}
		int statusCode = mResponse.getStatus();
		if(statusCode == HttpServletResponse.SC_NOT_FOUND) {
			PrintWriter writer = mResponse.getWriter();
			String uri = mRequest.getRequestURI();
			writer.print("<html><body><h1>Not Found</h1>");
			writer.print("<p>Cannot find resource: ");
			writer.print(uri);
			writer.print("</p></body></html>");
		} else if(statusCode == HttpServletResponse.SC_FORBIDDEN) {
			PrintWriter writer = mResponse.getWriter();
			String uri = mRequest.getRequestURI();
			writer.print("<html><body><h1>Forbidden</h1>");
			writer.print("<p>You are not allowed to access: ");
			writer.print(uri);
			writer.print("</p></body></html>");
		} else if(statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
			PrintWriter writer = mResponse.getWriter();
			writer.print("<html><body><h1>500 Internal Server Error</h1>");
			writer.print("<p>An internal error occurred</p></body></html>");
		} else if(statusCode == HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
			mResponse.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
			mResponse.setMessage(Const.getStatusMessage(HttpServletResponse.SC_NOT_IMPLEMENTED));
		}
	}
	
	private void printDebug(String message) {
		if(mServer.isDebug()) {
			logger.debug(message);
		}
	}
}
