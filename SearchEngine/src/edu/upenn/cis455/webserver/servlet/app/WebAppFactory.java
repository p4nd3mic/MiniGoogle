package edu.upenn.cis455.webserver.servlet.app;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.upenn.cis455.util.StringUtil;
import edu.upenn.cis455.webserver.servlet.app.WebAppManager.AppInfo;

public class WebAppFactory {

	public static final String TAG = WebAppFactory.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private static SAXParser saxParser;
	
	public String basePath = null;
	public String contextPath = null;
	public String displayName = null;
	public Map<String, String> contextParams = new HashMap<String, String>();
	public Map<String, ServletInfo> servletInfoMap = new HashMap<String, ServletInfo>();
	public Map<String, String> urlMappings = new HashMap<String, String>();
	public int sessionTimeout = 60;
	
	static {
		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public static WebApp create(AppInfo info) {
		WebApp app = null;
		File file = new File(info.getFilePath());
		WebAppFactory factory = parseWebXml(file);
		if(factory != null) {
			factory.contextPath = info.getContextPath();
			factory.basePath = file.getParentFile().getAbsolutePath();
			app = new WebApp(factory);
		}
		return app;
	}
	
	public static WebAppFactory parseWebXml(File webXmlFile) {
		WebXmlHandler handler = new WebXmlHandler();
		try {
			saxParser.parse(webXmlFile, handler);
			return handler.getFactory();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	/*public static WebApp create(File file) {
		WebApp app = null;
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			WebXmlHandler handler = new WebXmlHandler();
			parser.parse(file, handler);
			
			WebAppFactory factory = handler.getFactory();
			factory.basePath = file.getParentFile().getAbsolutePath();
			String parentPath = file.getParent();
			int index = parentPath.indexOf(appRoot);
			if(index != -1) {
				parentPath = parentPath.substring(index + appRoot.length());
			}
			String appName = parentPath;
			if(!appName.isEmpty()) {
				char firstChar = appName.charAt(0);
				if(firstChar == '/' || firstChar == '\\') {
					appName = appName.substring(1);
				}
			}
			
			String contextPath = appName;
			if(!contextPath.isEmpty()) {
				contextPath = '/' + appName;
			}
			factory.contextPath = contextPath;
			
			if(appName.isEmpty()) {
				appName = "<ROOT>";
			}
			factory.appName = appName;
			
			app = new WebApp(factory);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return app;
	}
	
	public static String getAppRoot() {
		return appRoot;
	}

	public static void setAppRoot(String appRoot) {
		WebAppFactory.appRoot = appRoot;
	}*/

	public static class ServletInfo {
		public String name;
		public String className;
		public Map<String, String> initParams = new HashMap<String, String>();
		public int loadOnStart = -1;
	}
	
	private static class WebXmlHandler extends DefaultHandler {

		private static final String TAG_WEB_APP = "web-app";
		private static final String TAG_DISPLAY_NAME = "display-name";
		private static final String TAG_SERVLET = "servlet";
		private static final String TAG_SERVLET_NAME = "servlet-name";
		private static final String TAG_SERVLET_CLASS = "servlet-class";
		private static final String TAG_CONTEXT_PARAM = "context-param";
		private static final String TAG_INIT_PARAM = "init-param";
		private static final String TAG_PARAM_NAME = "param-name";
		private static final String TAG_PARAM_VALUE = "param-value";
		private static final String TAG_LOAD_ON_STARTUP = "load-on-startup";
		private static final String TAG_SERVLET_MAPPING = "servlet-mapping";
		private static final String TAG_URL_PATTERN = "url-pattern";
		private static final String TAG_SESSION_TIMEOUT = "session-timeout";
		
		private WebAppFactory factory = null;
		private Stack<String> nodeStack = new Stack<String>();
		
		private String value = null;
		private String paramName = null;
		private ServletInfo servletInfo = null;
		private String servletName = null;
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			nodeStack.push(qName);
			if(TAG_WEB_APP.equalsIgnoreCase(qName)) {
				factory = new WebAppFactory();
			} else if(TAG_SERVLET.equalsIgnoreCase(qName)) {
				servletInfo = new ServletInfo();
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			value = new String(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			nodeStack.pop();
			if(TAG_DISPLAY_NAME.equalsIgnoreCase(qName)) {
				factory.displayName = value;
			} else if(TAG_SERVLET_NAME.equalsIgnoreCase(qName)) {
				String parent = nodeStack.peek();
				if(TAG_SERVLET.equalsIgnoreCase(parent)) {
					if(servletInfo != null) {
						servletInfo.name = value;
					}
				} else if(TAG_SERVLET_MAPPING.equalsIgnoreCase(parent)) {
					servletName = value;
				}
			} else if(TAG_SERVLET_CLASS.equalsIgnoreCase(qName)) {
				if(servletInfo != null) {
					servletInfo.className = value;
				}
			} else if(TAG_PARAM_NAME.equalsIgnoreCase(qName)) {
				paramName = value;
			} else if(TAG_PARAM_VALUE.equalsIgnoreCase(qName)) {
				String parent = nodeStack.peek();
				if(TAG_CONTEXT_PARAM.equalsIgnoreCase(parent)) {
					if(paramName != null) {
						factory.contextParams.put(paramName, value);
					}
					paramName = null;
				} else if(TAG_INIT_PARAM.equalsIgnoreCase(parent)) {
					if(servletInfo != null) {
						if(paramName != null) {
							servletInfo.initParams.put(paramName, value);
						}
						paramName = null;
					}
				}
			} else if(TAG_LOAD_ON_STARTUP.equalsIgnoreCase(qName)) {
				if(servletInfo != null) {
					int load = 0;
					String strValue = value.trim();
					if(!StringUtil.isEmpty(strValue)) {
						load = StringUtil.parseInt(strValue, 0);
					}
					servletInfo.loadOnStart = load;
				}
			} else if(TAG_SERVLET.equals(qName)) {
				if(servletInfo != null) {
					factory.servletInfoMap.put(servletInfo.name, servletInfo);
					servletInfo = null;
				}
			} else if(TAG_URL_PATTERN.equalsIgnoreCase(qName)) {
				if(servletName != null) {
					factory.urlMappings.put(value, servletName);
				}
			} else if(TAG_SERVLET_MAPPING.equalsIgnoreCase(qName)) {
				servletName = null;
			} else if(TAG_SESSION_TIMEOUT.equalsIgnoreCase(qName)) {
				factory.sessionTimeout = StringUtil.parseInt(value, 0);
			}
			
			value = null;
		}

		public WebAppFactory getFactory() {
			return factory;
		}
	}
}
