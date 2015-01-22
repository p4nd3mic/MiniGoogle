package edu.upenn.cis455.webserver.servlet.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServlet;

import edu.upenn.cis455.webserver.servlet.Context;

public class WebAppManager {

	public static final String TAG = WebAppManager.class.getSimpleName();
	
	public final static String APP_ROOT = "<root>";
	
	private String mServerInfo = null;
	private Map<String, AppInfo> mWebAppInfos = new HashMap<String, WebAppManager.AppInfo>();
	private Map<String, WebApp> mWebApps = new HashMap<String, WebApp>();
	
	public void importRootApp(String webXmlPath) {
		if(webXmlPath == null) {
			return;
		}
		AppInfo info = new AppInfo();
		info.contextPath = "";
		info.filePath = webXmlPath;
		info.name = APP_ROOT;
		info.installed = false;
		mWebAppInfos.put(APP_ROOT, info);
		installApp(info);
	}
	
	public void importApp(String appName, String webXmlPath) {
		AppInfo info = new AppInfo();
		info.name = appName;
		info.contextPath = "/" + appName;
		info.installed = false;
		info.filePath = webXmlPath;
		mWebAppInfos.put(appName, info);
		installApp(info);
	}

	public void installAppByName(String name) {
		installApp(getAppInfoByName(name));
	}
	
	public void removeAppByName(String appName) {
		removeApp(getAppInfoByName(appName));
	}
	
	public WebApp getRootApp() {
		return getApp(APP_ROOT);
	}

	public WebApp getApp(String appName) {
		return mWebApps.get(appName);
	}
	
	public ServletPathInfo searchRequestSevlet(String requestUri) {
		ServletPathInfo info = new ServletPathInfo();
		Set<Entry<String, WebApp>> entries = mWebApps.entrySet();
		for(Entry<String, WebApp> entry : entries) {
			WebApp app = entry.getValue();
			if(app.searchForServlet(requestUri, info)) {
				return info;
			}
		}
		return null;
	}
	
	public void destroyApps() {
		Set<Entry<String, WebApp>> set = mWebApps.entrySet();
		for(Entry<String, WebApp> entry : set) {
			WebApp app = entry.getValue();
			app.destroyServlets();
		}
	}
	
	public List<AppInfo> getAppInfo() {
		List<AppInfo> list = new ArrayList<WebAppManager.AppInfo>();
		Set<Entry<String, AppInfo>> set = mWebAppInfos.entrySet();
		for(Entry<String, AppInfo> entry : set) {
			list.add(entry.getValue());
		}
		return list;
	}
	
	public boolean hasApp(String name) {
		return mWebAppInfos.containsKey(name);
	}

	public void setServerInfo(String serverInfo) {
		this.mServerInfo = serverInfo;
	}

	public static class ServletPathInfo {
		public Context context;
		public HttpServlet servlet;
		
		public String contextPath;
		public String servletPath;
		public String pathInfo;
	}
	
	public static class AppInfo {
		private String name;
		private String filePath;
		private String contextPath;
		private boolean installed = false;
		
		public String getAppName() {
			return name;
		}
		public String getFilePath() {
			return filePath;
		}
		public String getContextPath() {
			return contextPath;
		}
		public boolean isInstalled() {
			return installed;
		}
	}
	
	private void installApp(AppInfo info) {
		if(info == null || info.installed) {
			return;
		}
		WebApp app = WebAppFactory.create(info);
		if(app != null) {
			app.setServerInfo(mServerInfo);
			mWebApps.put(info.name, app);
			info.installed = true;
		}
	}
	
	private void removeApp(AppInfo info) {
		if(info == null || !info.installed) {
			return;
		}
		mWebApps.remove(info.name);
		info.installed = false;
	}
	
	private AppInfo getAppInfoByName(String name) {
		return mWebAppInfos.get(name);
	}
}
