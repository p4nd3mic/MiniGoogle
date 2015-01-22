package edu.upenn.cis455.webserver.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.webserver.server.ActionListener;
import edu.upenn.cis455.webserver.servlet.Headers.Header;

public class Response implements HttpServletResponse {
	
	public static final String TAG = Response.class.getSimpleName();

	private final DateFormat mDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	private final String pastDate = mDateFormat.format(new Date(10000));
	
	private String mEncoding = "ISO-8859-1";
	private boolean mCommited = false;
	private Locale mLocale = Locale.US;
	private Headers mHeaders = new Headers();
	private int mStatusCode = 200;
	private String mMessage = null;
	private Request mRequest = null;
	private boolean mUseWriter = false;
	private boolean mUseOutputStream = false;
	private boolean mError = false;
	private int mContentLength = -1;

	private OutputBuffer mOutputBuffer;
	private BufferedWriter mWriter = null;
	private BufferdOutputStream mOutputStream = null;
	
	private ActionListener mActionListener = null;

	public Response() {
		mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
	@Override
	public String getCharacterEncoding() {
		return mEncoding;
	}

	@Override
	public String getContentType() {
		return mHeaders.getHeader("Content-Type");
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if(mUseWriter) {
			throw new IllegalStateException("Already called getWriter()");
		}
		mUseOutputStream = true;
		if(mOutputStream == null) {
			mOutputStream = new BufferdOutputStream(mOutputBuffer);
		}
		return mOutputStream;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if(mUseOutputStream) {
			throw new IllegalStateException("Already called getOutputStream()");
		}
		mUseWriter = true;
		if(mWriter == null) {
			mWriter = new BufferedWriter(mOutputBuffer);
		}
		return mWriter;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		this.mEncoding = charset;
	}

	@Override
	public void setContentLength(int len) {
		mContentLength = len;
		setIntHeader("Content-Length", len);
	}

	@Override
	public void setContentType(String type) {
		setHeader("Content-Type", type);
	}

	@Override
	public void setBufferSize(int size) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		mOutputBuffer.flush();
	}

	@Override
	public void resetBuffer() {
		if(mCommited) {
			throw new IllegalStateException("Response has been commited");
		}
		mOutputBuffer.recycle();
	}

	@Override
	public boolean isCommitted() {
		return mCommited;
	}
	
	public void setCommitted(boolean committed) {
		this.mCommited = committed;
	}

	@Override
	public void reset() {
		if(mCommited) {
			throw new IllegalStateException("Response has been commited");
		}
		setStatus(SC_OK);
		mOutputBuffer.recycle();
		mHeaders.clear();
	}

	@Override
	public void setLocale(Locale loc) {
		this.mLocale = loc;
	}

	@Override
	public Locale getLocale() {
		return mLocale ;
	}

	@Override
	public void addCookie(Cookie cookie) {
		if(isCommitted()) {
			return;
		}
		
		String cookieString = generateCookieString(cookie, false);
		addHeader("Set-Cookie", cookieString);
	}

	@Override
	public boolean containsHeader(String name) {
		return mHeaders.hasHeader(name);
	}

	@Override
	public String encodeURL(String url) {
		if(shouldEncode()) {
			return encodeSessionId(url, mRequest.getSession().getId());
		} else {
			return url;
		}
	}

	@Override
	public String encodeRedirectURL(String url) {
		if(shouldEncode()) {
			return encodeSessionId(url, mRequest.getSession().getId());
		} else {
			return url;
		}
	}

	@Override
	@Deprecated
	public String encodeUrl(String url) {
		return null;
	}

	@Override
	@Deprecated
	public String encodeRedirectUrl(String url) {
		return null;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		if(mCommited) {
			throw new IllegalStateException("Response has been commited");
		}
		setStatus(sc);
		if(msg != null) {
			setMessage(msg);
		}
		setError(true);
		resetBuffer();
	}

	@Override
	public void sendError(int sc) throws IOException {
		sendError(sc, null);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		if(mCommited) {
			throw new IllegalStateException("Response has been commited");
		}
		String absolute = toAbsolutePath(location);
		setStatus(SC_FOUND);
		addHeader("Location", absolute);
		
		resetBuffer();
	}

	@Override
	public void setDateHeader(String name, long date) {
		String dateStr = mDateFormat.format(new Date(date));
		mHeaders.setHeader(name, dateStr);
	}

	@Override
	public void addDateHeader(String name, long date) {
		String dateStr = mDateFormat.format(new Date(date));
		mHeaders.addHeader(name, dateStr);
	}

	@Override
	public void setHeader(String name, String value) {
		mHeaders.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		mHeaders.appendHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		mHeaders.setHeader(name, String.valueOf(value));
	}

	@Override
	public void addIntHeader(String name, int value) {
		mHeaders.appendHeader(name, String.valueOf(value));
	}

	@Override
	public void setStatus(int sc) {
		this.mStatusCode = sc;
	}

	@Override
	@Deprecated
	public void setStatus(int sc, String sm) {
	}
	
	public int getStatus() {
		return mStatusCode;
	}
	
//	public void setOutputBuffer(ByteArrayOutputStream buffer) {
//		mOutputBuffer = new OutputBuffer();
//		mOutputBuffer.setResponse(this);
//		mOutputBuffer.setSocketBuffer(buffer);
//		mWriter = new BufferedWriter(mOutputBuffer);
//		mOutputStream = new BufferdOutputStream(mOutputBuffer);
//	}
//	
//	public String getProtocol() {
//		return mProtocol;
//	}
//
//	public void setProtocol(String protocol) {
//		this.mProtocol = protocol;
//	}
	
	/*public String getServerInfo() {
		return mServerInfo;
	}

	public void setServerInfo(String serverInfo) {
		this.mServerInfo = serverInfo;
	}*/

	public void setRequest(Request request) {
		this.mRequest = request;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		this.mMessage = message;
	}
	
	public void sendContinue() {
		if(mActionListener != null) {
			mActionListener.sendContinue();
		}
	}

	public void sendHeaders() throws IOException {
		if(mActionListener != null) {
			mActionListener.commit();
		}
		mCommited = true;
	}
	
	public boolean isError() {
		return mError;
	}

	public void setError(boolean error) {
		this.mError = error;
	}
	
	public void finishResponse() throws IOException {
		mOutputBuffer.close();
	}
	
	public void recycle() {
		mEncoding = "ISO-8859-1";
		mCommited = false;
		mLocale = Locale.US;
		mHeaders.clear();
		mStatusCode = 200;
		mMessage = null;
//		mProtocol = null;
		mUseWriter = false;
		mUseOutputStream = false;
		mError = false;
		mContentLength = -1;
		mOutputBuffer.recycle();
	}
	
	/**
	 * Add session cookie header, override previously set ones
	 * @param cookie
	 */
	public void addSessionCookie(Cookie cookie) {
		String cookieString = generateCookieString(cookie, false);
		Iterator<Header> iter = mHeaders.getList().iterator();
		final String setCookie = "Set-Cookie";
		final String sessionCookie = cookie.getName() + "=";
		boolean set = false;
		while(iter.hasNext()) {
			Header header = iter.next();
			if(setCookie.equalsIgnoreCase(header.getName())) {
				if(header.getValue().startsWith(sessionCookie)) {
					header.setValue(cookieString);
					set = true;
				}
			}
		}
		if(!set) {
			addHeader(setCookie, cookieString);
		}
	}
	
	public ActionListener getActionListener() {
		return mActionListener;
	}
	
	public void setActionListener(ActionListener actionListener) {
		this.mActionListener = actionListener;
	}
	
	public void setSocketBuffer(ByteArrayOutputStream socketBuffer) {
		mOutputBuffer = new OutputBuffer(socketBuffer);
		mOutputBuffer.setResponse(this);
		mWriter = new BufferedWriter(mOutputBuffer);
		mOutputStream = new BufferdOutputStream(mOutputBuffer);
	}
	
	public Headers getHeaders() {
		return mHeaders;
	}
	
	public int getContentLength() {
		return mContentLength;
	}
	
	private String generateCookieString(Cookie cookie, boolean httpOnly) {
		StringBuilder builder = new StringBuilder();
		
		String name = cookie.getName();
		String value = cookie.getValue();
		builder.append(name).append("=").append(value);
		
		int version = cookie.getVersion();
		if(version == 1) {
			builder.append("; Version=1");
		}
		
		String comment = cookie.getComment();
		if(comment != null) {
			builder.append("; Comment=").append(comment);
		}
		
		String domain = cookie.getDomain();
		if(domain != null) {
			builder.append("; Domain=").append(domain);
		}
		
		int maxAge = cookie.getMaxAge();
		if(maxAge >= 0) {
			builder.append("; Expires=");
			if(maxAge == 0) {
				builder.append(pastDate);
			} else {
				Date expires = new Date(System.currentTimeMillis() + maxAge * 1000L);
				builder.append(mDateFormat.format(expires));
			}
		}
		
		String path = cookie.getPath();
		if(path != null) {
			builder.append("; Path=")
			.append(path);
		}
		
		if(cookie.getSecure()) {
			builder.append("; Secure");
		}
		
		if(httpOnly) {
			builder.append("; HttpOnly");
		}
		
		return builder.toString();
	}
	
	private String toAbsolutePath(String url) {
		boolean hasScheme = url.indexOf("://") != -1;
		if(url.startsWith("/") || !hasScheme) {
			StringBuilder sb = new StringBuilder();
			String scheme = mRequest.getScheme();
	        String name = mRequest.getServerName();
	        int port = mRequest.getServerPort();
	        String contextPath = mRequest.getContextPath();
	        
	        sb.append(scheme).append("://").append(name);
	        if(port != 80) {
	        	sb.append(":").append(port);
	        }
	        
	        if(contextPath != null && !contextPath.isEmpty()) {
	        	if(!contextPath.startsWith("/")) {
		        	sb.append("/");
		        }
		        sb.append(contextPath);
	        }
	        
	        if(!url.startsWith("/")) {
	        	sb.append("/");
	        }
	        sb.append(url);
	        return sb.toString();
		} else {
			return url;
		}
	}
	
	private String encodeSessionId(String url, String sessionId) {
        if ((url == null) || (sessionId == null))
            return (url);

        String path = url;
        String query = "";
        String anchor = "";
        int question = url.indexOf('?');
        if (question >= 0) {
            path = url.substring(0, question);
            query = url.substring(question);
        }
        int pound = path.indexOf('#');
        if (pound >= 0) {
            anchor = path.substring(pound);
            path = path.substring(0, pound);
        }
        StringBuffer sb = new StringBuffer(path);
        if( sb.length() > 0 ) { // jsessionid can't be first.
            sb.append(";jsessionid=")
            .append(sessionId);
        }
        sb.append(anchor);
        sb.append(query);
        return (sb.toString());
    }
	
	
	private boolean shouldEncode() {
		if(mRequest == null) {
			return false;
		}
		final Session session = mRequest.getSessionInternal();
		if(session == null) {
			return false;
		}
		if(mRequest.isRequestedSessionIdFromCookie()) {
			return false;
		} else {
			return true;
		}
	}
}
