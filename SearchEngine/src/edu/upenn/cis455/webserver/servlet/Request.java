package edu.upenn.cis455.webserver.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.webserver.servlet.app.SessionManager;

public class Request implements HttpServletRequest {

	public static final String TAG = Request.class.getSimpleName();
//	private static Logger logger = Logger.getLogger(TAG);
	
	private DateFormat[] mDateFormats = new DateFormat[] {
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US),
			new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss z", Locale.US),
			new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US)
	};
	
	private String mEncoding = "ISO-8859-1";
	private Locale mLocale = null;
	private String mProtocol = null;
	private String mMethod = null;
	private String mServerName = null;
	private int mServerPort = -1;
	private String mRemoteHost = null;
	private String mRemoteAddress = null;
	private int mRemotePort = -1;
	private String mLocalAddress = null;
	private int mLocalPort = -1;
	private String mLocalName = null;
	private String mContextPath = null;
	private String mQueryString = null;
	private String mServletPath = null;
	private String mPathInfo = null;
	private String mRequestUri = null;
	private Parameters mHeaders = new Parameters();
	private Parameters mParameters = new Parameters();
	private Map<String, Object> mAttributes = new HashMap<String, Object>();
	private List<Cookie> mCookies = new ArrayList<Cookie>();
	
	private Context mContext = null;
	private Response mResponse = null;
	
	private String mRequestedSessionId = null;
	private Session mSession = null;

	private boolean mParamParsed = false;
	private boolean mSessionFromCookie = false;
	private boolean mSessionFromURL = false;
	
	private InputStream mStream = null;
	private BufferedReader mReader = null;
	private WrappedServletInputStream mInputStream = null;
	
	public Request() {
		TimeZone tz = TimeZone.getTimeZone("GMT");
		for(DateFormat df : mDateFormats) {
			df.setTimeZone(tz);
		}
	}
	
	@Override
	public Object getAttribute(String name) {
		return mAttributes.get(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(mAttributes.keySet());
	}

	@Override
	public String getCharacterEncoding() {
		return mEncoding;
	}

	@Override
	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		this.mEncoding = env;
	}

	@Override
	public int getContentLength() {
		int length = -1;
		try {
			length = getIntHeader("Content-Length");
		} catch (NumberFormatException e) {
		}
		return length;
	}

	@Override
	public String getContentType() {
		return getHeader("Content-Type");
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if(mInputStream == null) {
			if(mStream != null) {
				mInputStream = new WrappedServletInputStream(mStream);
			}
		}
		return mInputStream;
		
	}

	@Override
	public String getParameter(String name) {
		if(!mParamParsed) {
			parseParameters();
		}
		return mParameters.getParameter(name);
	}

	@Override
	public Enumeration getParameterNames() {
		if(!mParamParsed) {
			parseParameters();
		}
		return mParameters.getParameterNames();
	}

	@Override
	public String[] getParameterValues(String name) {
		if(!mParamParsed) {
			parseParameters();
		}
		return mParameters.getParameterValues(name);
	}

	@Override
	public Map getParameterMap() {
		if(!mParamParsed) {
			parseParameters();
		}
		return mParameters.getParameterMap();
	}

	@Override
	public String getProtocol() {
		return mProtocol;
	}
	
	@Override
	public String getScheme() {
		return "http";
	}

	@Override
	public String getServerName() {
		return mServerName;
	}

	@Override
	public int getServerPort() {
		return mServerPort;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		if(mReader == null) {
			if(mStream != null) {
				mReader = new BufferedReader(new InputStreamReader(mStream));
			}
		}
		return mReader;
	}

	@Override
	public String getRemoteAddr() {
		return mRemoteAddress;
	}

	@Override
	public String getRemoteHost() {
		return mRemoteHost;
	}

	@Override
	public void setAttribute(String name, Object o) {
		mAttributes.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		mAttributes.remove(name);
	}

	@Override
	public Locale getLocale() {
		return mLocale;
	}

	@Override
	public Enumeration getLocales() {
		return null;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	@Override
	@Deprecated
	public String getRealPath(String path) {
		return null;
	}

	@Override
	public int getRemotePort() {
		return mRemotePort;
	}

	@Override
	public String getLocalName() {
		return mLocalName;
	}

	@Override
	public String getLocalAddr() {
		return mLocalAddress;
	}

	@Override
	public int getLocalPort() {
		return mLocalPort;
	}

	@Override
	public String getAuthType() {
		return BASIC_AUTH;
	}

	@Override
	public Cookie[] getCookies() {
		if(mCookies == null || mCookies.isEmpty()) {
			return null;
		}
		Cookie[] cookies = new Cookie[mCookies.size()];
		return mCookies.toArray(cookies);
	}

	@Override
	public long getDateHeader(String name) {
		String dateStr = mHeaders.getParameter(name);
		if(dateStr == null) {
			return -1;
		}
		Date date = parseDate(dateStr);
		if(date == null) {
			throw new IllegalArgumentException("Header " + name + ": " + dateStr + " cannot be parse into date");
		}
		return date.getTime();
	}

	@Override
	public String getHeader(String name) {
		return mHeaders.getParameter(name);
	}

	@Override
	public Enumeration getHeaders(String name) {
		if(mHeaders.hasName(name)) {
			return Collections.enumeration(Arrays.asList(mHeaders.getParameterValues(name)));
		} else {
			return Collections.emptyEnumeration();
		}
	}

	@Override
	public Enumeration getHeaderNames() {
		return mHeaders.getParameterNames();
	}

	@Override
	public int getIntHeader(String name) {
		String value = mHeaders.getParameter(name);
		if(value == null) {
			return -1;
		}
		return Integer.parseInt(value);
	}

	@Override
	public String getMethod() {
		return mMethod;
	}
	
	@Override
	public String getPathInfo() {
		return mPathInfo;
	}

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getContextPath() {
		return mContextPath ;
	}

	@Override
	public String getQueryString() {
		return mQueryString;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return mRequestedSessionId;
	}

	@Override
	public String getRequestURI() {
		return mRequestUri;
	}

	@Override
	public StringBuffer getRequestURL() {
		StringBuffer sb = new StringBuffer();
		int port = getServerPort();
		if(port < 0) {
			port = 80;
		}
		sb.append(getScheme()).append("://")
		.append(getServerName());
		if(port != 80) {
			sb.append(':').append(port);
		}
		sb.append(getRequestURI());
		return sb;
	}

	@Override
	public String getServletPath() {
		return mServletPath;
	}
	
	@Override
	public HttpSession getSession(boolean create) {
		if(mContext == null) {
			return null;
		}
		
		// Return the current session if it exists and is valid
		if(mSession != null && !mSession.isValid()) {
			mSession = null;
		}
		if(mSession != null) { // Current session is still valid
			return mSession;
		}
		
		// Return the requested session if it exists and is valid
		SessionManager manager = mContext.getSessionManager();
		if(manager == null) {
			return null;
		}
		if(mRequestedSessionId != null) {
			mSession = manager.getSession(mRequestedSessionId);
		}
		if(mSession != null && !mSession.isValid()) {
			mSession = null;
		}
		if(mSession != null) {
			mSession.access();
			return mSession;
		}
		
		if(!create) {
			return null;
		}
		if(mResponse != null && mResponse.isCommitted()) {
			throw new IllegalArgumentException("Response has been committed");
		}
		
		// Attempt to reuse session id if one was submitted in a cookie
        // Do not reuse the session id if it is from a URL, to prevent possible
        // phishing attacks
		if(isRequestedSessionIdFromCookie()) {
			mSession = manager.createSession(getRequestedSessionId());
		} else {
			mSession = manager.createSession(null);
		}
		
		// Creating a new session cookie based on that session
		if(mSession != null && getContext() != null) {
			Cookie cookie = new Cookie("JSESSIONID", mSession.getId());
			configSessionCookie(cookie);
			if(mResponse != null) {
				mResponse.addSessionCookie(cookie);
			}
		}
		
		if(mSession != null) {
			mSession.access();
			return mSession;
		} else {
			return null;
		}
	}
	
	@Override
	public HttpSession getSession() {
		return getSession(true);
	}
	
	public Session getSessionInternal() {
		return mSession;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		if(mRequestedSessionId == null) {
			return false;
		}
		if(mContext == null) {
			return false;
		}
		SessionManager manager = mContext.getSessionManager();
		if(manager == null) {
			return false;
		}
		Session session = manager.getSession(mRequestedSessionId);
		if(session != null && session.isValid()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		if(mRequestedSessionId != null) {
			return mSessionFromCookie;
		}
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		if(mRequestedSessionId != null) {
			return mSessionFromURL;
		}
		return false;
	}

	@Override
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}
	
	public void setMethod(String method) {
		this.mMethod = method;
	}

	public void setProtocol(String protocol) {
		this.mProtocol = protocol;
	}
	
	public InputStream getStream() {
		return mStream;
	}

	public void setStream(InputStream inputStream) {
		this.mStream = inputStream;
		mReader = new BufferedReader(new InputStreamReader(mStream));
		mInputStream = new WrappedServletInputStream(mStream);
	}

	public void setLocale(Locale locale) {
		this.mLocale = locale;
	}

	public void setServerName(String serverName) {
		this.mServerName = serverName;
	}

	public void setServerPort(int serverPort) {
		this.mServerPort = serverPort;
	}

	public void setLocalAddr(String localAddr) {
		this.mLocalAddress = localAddr;
	}

	public void setLocalPort(int localPort) {
		this.mLocalPort = localPort;
	}

	public void setLocalName(String localName) {
		this.mLocalName = localName;
	}

	public void setRemoteHost(String remoteHost) {
		this.mRemoteHost = remoteHost;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.mRemoteAddress = remoteAddr;
	}

	public void setRemotePort(int remotePort) {
		this.mRemotePort = remotePort;
	}

	public void setContextPath(String contextPath) {
		this.mContextPath = contextPath;
	}

	public void setQueryString(String queryString) {
		this.mQueryString = queryString;
	}

	public void setServletPath(String servletPath) {
		this.mServletPath = servletPath;
	}

	public void setPathInfo(String pathInfo) {
		this.mPathInfo = pathInfo;
	}
	
	public void setRequestUri(String requestUri) {
		this.mRequestUri = requestUri;
	}
	
	public void addHeader(String name, String[] values) {
		mHeaders.addParameter(name, values);
	}
	
	public void addCookie(Cookie cookie) {
		mCookies.add(cookie);
	}
	
	public Response getResponse() {
		return mResponse;
	}

	public void setResponse(Response response) {
		this.mResponse = response;
		response.setRequest(this);
	}

	public Context getContext() {
		return mContext;
	}

	public void setContext(Context context) {
		this.mContext = context;
	}

	public void setRequestedSessionId(String requestedSessionId) {
		this.mRequestedSessionId = requestedSessionId;
	}

	public void setSessionFromCookie(boolean sessionFromCookie) {
		this.mSessionFromCookie = sessionFromCookie;
	}

	public void setSessionFromURL(boolean sessionFromURL) {
		this.mSessionFromURL = sessionFromURL;
	}
	
	public void recycle() {
		mParamParsed = false;
		mEncoding = "ISO-8859-1";
		mLocale = Locale.US;
		mProtocol = null;
		mMethod = null;
		mServerName = null;
		mServerPort = -1;
		mRemoteHost = null;
		mRemoteAddress = null;
		mRemotePort = -1;
		mLocalAddress = null;
		mLocalPort = -1;
		mLocalName = null;
		mContextPath = null;
		mQueryString = null;
		mServletPath = null;
		mPathInfo = null;
		mRequestUri = null;
		mHeaders.clear();
		mParameters.clear();
		mAttributes.clear();
		mCookies.clear();
	}
	
	private Date parseDate(String str) {
		if(str == null) {
			throw new NullPointerException("Date string cannot be null");
		}
		Date date = null;
		for(DateFormat format : mDateFormats) {
			date = tryParseDate(str, format);
			if(date != null) {
				break;
			}
		}
		return date;
	}
	
	private Date tryParseDate(String str, DateFormat format) {
		try {
			return format.parse(str);
		} catch (ParseException e) {
		}
		return null;
	}
	
	private void configSessionCookie(Cookie cookie) {
		cookie.setMaxAge(-1);
		
		if(mContextPath != null && !mContextPath.isEmpty()) {
			cookie.setPath(mContextPath);
		} else {
			cookie.setPath("/");
		}
	}

	private void parseParameters() {
		mParamParsed = true;
		
		String query = getQueryString();
		if(query != null) {
			mParameters.handleQueryString(query);
		}
		
		if("GET".equals(getMethod())) {
			return;
		}
		// POST parameters
		String contentType = getContentType();
		if(contentType == null) {
			contentType = "";
		}
		int semiColon = contentType.indexOf(';');
		if(semiColon != -1) {
			contentType = contentType.substring(0, semiColon).trim();
		} else {
			contentType = contentType.trim();
		}
		if(!"application/x-www-form-urlencoded".equals(contentType)) {
			return;
		}
		try {
			int len = getContentLength();
			if(len != -1) {
				if(mStream != null) {
					byte[] buff = new byte[len];
					if(readPostBody(buff, len) != len) {
						return;
					}
					String formData = new String(buff, getCharacterEncoding());
					mParameters.handleQueryString(formData);
				}
			} else if("chunked"
				.equalsIgnoreCase(getHeader("Transfer-Encoding"))) { // Chunked
				if(mStream != null) {
					byte[] data = readChunkedData();
					String formData = new String(data, getCharacterEncoding());
					mParameters.handleQueryString(formData);
				}
			}
		} catch (IOException e) {
			System.err.println(TAG + ": " + e.getMessage());
		}
	}
	
	private int readPostBody(byte[] buff, int len) throws IOException {
		int offset = 0;
		do {
			int inputLen = mStream.read(buff, offset, len - offset);
			if(inputLen < 0) {
				return offset;
			}
			offset += inputLen;
		} while(len - offset > 0);
		return len;
	}
	
	private byte[] readChunkedData() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		LineReader reader = new LineReader(mStream);
		boolean end = false;
		while(!end) {
			String sizeStr = reader.readLine();
			int semicolon = sizeStr.indexOf(';');
			if(semicolon >= 0) {
				sizeStr = sizeStr.substring(0, semicolon);
			}
			int size = Integer.parseInt(sizeStr, 16);
			if(size == 0) {
				end = true;
			} else {
				byte[] buf = new byte[size];
				readPostBody(buf, size);
				buffer.write(buf);
				reader.readLine(); // next line
			}
		}
		return buffer.toByteArray();
	}
}
