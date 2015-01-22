package edu.upenn.cis455.webserver.servlet;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import edu.upenn.cis455.webserver.servlet.app.SessionManager;

public class Context implements ServletContext {

	private Map<String, Object> mAttributes = new HashMap<String, Object>();
	private Map<String, String> mInitParams = new HashMap<String, String>();
	private String mServerInfo;
	private String mBasePath;
	private String mContextName = null;
	private SessionManager mSessionManager = null;
	
	public Context(String basePath) {
		this.mBasePath = basePath;
	}

	@Override
	public ServletContext getContext(String uripath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMajorVersion() {
		return 2;
	}

	@Override
	public int getMinorVersion() {
		return 4;
	}

	@Override
	public String getMimeType(String file) {
		return null;
	}

	@Override
	public Set getResourcePaths(String path) {
		return null;
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return null;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		return null;
	}

	@Override
	@Deprecated
	public Servlet getServlet(String name) throws ServletException {
		return null;
	}

	@Override
	@Deprecated
	public Enumeration getServlets() {
		return null;
	}

	@Override
	@Deprecated
	public Enumeration getServletNames() {
		return null;
	}

	@Override
	public void log(String msg) {
	}

	@Override
	public void log(Exception exception, String msg) {
	}

	@Override
	public void log(String message, Throwable throwable) {
	}

	@Override
	public String getRealPath(String path) {
		File file = new File(mBasePath, path);
		return file.getAbsolutePath();
	}

	@Override
	public String getServerInfo() {
		return mServerInfo;
	}

	@Override
	public String getInitParameter(String name) {
		if(mInitParams == null) {
			return null;
		}
		return mInitParams.get(name);
	}

	@Override
	public Enumeration getInitParameterNames() {
		return Collections.enumeration(mInitParams.keySet());
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
	public void setAttribute(String name, Object object) {
		mAttributes.put(name, object);
	}

	@Override
	public void removeAttribute(String name) {
		mAttributes.remove(name);
	}

	@Override
	public String getServletContextName() {
		return mContextName;
	}

	public void setServerInfo(String serverInfo) {
		this.mServerInfo = serverInfo;
	}
	
	public void setInitParams(Map<String, String> params) {
		this.mInitParams = params;
	}

	public void setServletContextName(String contextName) {
		this.mContextName = contextName;
	}

	public SessionManager getSessionManager() {
		return mSessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.mSessionManager = sessionManager;
	}
}
