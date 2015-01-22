package edu.upenn.cis455.webserver.servlet.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import edu.upenn.cis455.webserver.servlet.Config;
import edu.upenn.cis455.webserver.servlet.Context;
import edu.upenn.cis455.webserver.servlet.app.WebAppFactory.ServletInfo;
import edu.upenn.cis455.webserver.servlet.app.WebAppManager.ServletPathInfo;

public class WebApp {

	public static final String TAG = WebApp.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
//	private String mAppName;
	private String mContextPath;
	private String mDisplayName;
	private Context mContext;
	private Map<String, ServletInfo> mServletInfoMap;
	private Map<String, HttpServlet> mServletMap;
	private List<UrlPattern> mUrlMappings;
	
	public WebApp(WebAppFactory factory) {
		if(factory == null) {
			return;
		}
//		mAppName = factory.appName;
		mContextPath = factory.contextPath;
		mDisplayName = factory.displayName;
		
		mContext = getContext(factory);
		mServletInfoMap = factory.servletInfoMap;
		initUrlMappings(factory.urlMappings);
		
		loadStartupServlets();
	}
	
//	public String getAppName() {
//		return mAppName;
//	}
//
//	public void setAppName(String appName) {
//		this.mAppName = appName;
//	}

	public String getDisplayName() {
		return mDisplayName;
	}
	
	public HttpServlet getServletByName(String name) {
		return getOrLoadServlet(name);
	}
	
	public boolean searchForServlet(String uri, ServletPathInfo info) {
		String url = uri;
		boolean matches = false;
		if(url.startsWith(mContextPath)) {
			url = url.substring(mContextPath.length()); // Remove context path
			
			int semicolon = url.indexOf(';');	// Remove parts after ";"
			if(semicolon >= 0) {
				url = url.substring(0, semicolon);
			}
			
			String servletPath = null;
			UrlPattern matchedPattern = null;
			for(int i = mUrlMappings.size() - 1; i >= 0; i--) { // Searching for servlet path
				UrlPattern pattern = mUrlMappings.get(i);
				servletPath = pattern.matches(url);
				if(servletPath != null) {
					matchedPattern = pattern;
					break;
				}
			}
			if(servletPath != null && matchedPattern != null) { // Found
				matches = true;
				info.contextPath = mContextPath;
				info.servletPath = servletPath;
				
				int index = url.indexOf(servletPath);
				if(index != -1) {
					String pathInfo = url.substring(servletPath.length());
					if(pathInfo.isEmpty()) {
						pathInfo = null;
					}
					info.pathInfo = pathInfo;
				}
				info.context = mContext;
				info.servlet = getServletByName(matchedPattern.servletName);
			}
		}
		return matches;
	}
	
	public void destroyServlets() {
		Set<Entry<String, HttpServlet>> set = mServletMap.entrySet();
		for(Entry<String, HttpServlet> entry : set) {
			HttpServlet servlet = entry.getValue();
			servlet.destroy();
		}
	}
	
	public void setServerInfo(String info) {
		if(mContext != null) {
			mContext.setServerInfo(info);
		}
	}
	
	public ServletContext getContext() {
		return mContext;
	}

	private Context getContext(WebAppFactory factory) {
		Context context = new Context(factory.basePath);
		context.setServletContextName(factory.displayName);
//		context.setServerInfo(ServerManager.getCurrentServerConfig().getServerInfo());
		context.setInitParams(factory.contextParams);
		context.setSessionManager(getSessionManager(factory, context));
		return context;
	}
	
	private SessionManager getSessionManager(WebAppFactory factory, Context context) {
		SessionManager manager = new SessionManager(context);
		manager.setSessionTimeout(factory.sessionTimeout);
		return manager;
	}

	private void initUrlMappings(Map<String, String> mappings) {
		mUrlMappings = new ArrayList<UrlPattern>();
		Iterator<Entry<String, String>> iter = mappings.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			UrlPattern pattern = new UrlPattern(entry.getValue(), entry.getKey());
			mUrlMappings.add(pattern);
		}
	}

	private void loadStartupServlets() {
		mServletMap = new HashMap<String, HttpServlet>();
		Iterator<Entry<String, ServletInfo>> iter = mServletInfoMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, ServletInfo> entry = iter.next();
			String name = entry.getKey();
			ServletInfo info = entry.getValue();
			if(info.loadOnStart >= 0) {
				HttpServlet servlet = loadServlet(info);
				if(servlet != null) {
					mServletMap.put(name, servlet);
					iter.remove();
				}
			}
		}
	}
	
	private HttpServlet getOrLoadServlet(String name) {
		if(name == null) {
			return null;
		}
		HttpServlet servlet = mServletMap.get(name);
		if(servlet == null) {
			ServletInfo info = mServletInfoMap.get(name);
			if(info != null) {
				servlet = loadServlet(info);
				mServletMap.put(name, servlet);
			}
		}
		return servlet;
	}
	
	private HttpServlet loadServlet(ServletInfo info) {
		HttpServlet servlet = null;
		Config config = new Config(info.name, mContext, info.initParams);
		try {
			Class<?> servletClass = Class.forName(info.className);
			servlet = (HttpServlet) servletClass.newInstance();
			servlet.init(config);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return servlet;
	}
	
	static class UrlPattern {
		
		private final String servletName;
		private final String urlPattern;
		
		public UrlPattern(String servletName, String urlPattern) {
			this.servletName = servletName;
			this.urlPattern = urlPattern;
		}

		public String getServletName() {
			return servletName;
		}

		public String getUrlPattern() {
			return urlPattern;
		}
		
		public String matches(String url) {
			int index = urlPattern.indexOf("/*");
			if(index != -1) { // Wildcard
				final String pattern = urlPattern.substring(0, index);
				String match = urlPattern.substring(0, index + 1);
				if(url.startsWith(match) || (url + "/").equals(match)) {
					return pattern;
				}
			} else { // Exact matching
				if(url.equals(urlPattern)) {
					return urlPattern;
				}
			}
			return null;
		}
	}
}
