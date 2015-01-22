package edu.upenn.cis455.starter;

import java.io.File;

import edu.upenn.cis455.servlets.CrawlerNodeServlet;
import edu.upenn.cis455.util.StringUtil;
import edu.upenn.cis455.webserver.server.ServerConfig;
import edu.upenn.cis455.webserver.server.ServerInstance;
import edu.upenn.cis455.webserver.server.ServerManager;
import edu.upenn.cis455.webserver.servlet.app.WebAppFactory;
import edu.upenn.cis455.webserver.servlet.app.WebAppFactory.ServletInfo;
import edu.upenn.cis455.webserver.servlet.app.WebAppManager;

public class StartNode {
	
	public static final String DEFAULT_WEB_XML_PATH = "WEB-INF/web.xml";

	public static void main(String[] args) {
		String webXmlPath = DEFAULT_WEB_XML_PATH;
		
		if (args.length >= 1) {
			webXmlPath = args[0];
		}
		File file = new File(webXmlPath);
		if(!file.exists()) {
			System.err.println("Cannot find " + file.getAbsolutePath());
			return;
		}
		// Parse web.xml
		ServletInfo info = getServletInfo(file);
		if(info == null) {
			System.err.println("Cannot parse " + file.getAbsolutePath());
			return;
		}
		
		String portStr = null;
		String master = null;
		String storage = null;
		if(info != null) {
			portStr = info.initParams.get("port");
			master = info.initParams.get("master");
			storage = info.initParams.get("storagedir");
		}
		if (portStr == null) {
			System.err.println("Cannot find port init parameter in web.xml");
			return;
		}
		if(master == null) {
			System.err.println("Cannot find master init parameter in web.xml");
			return;
		} else {
			System.out.println("Master address: " + master);
		}
		if(storage == null) {
			System.err.println("Cannot find storagedir init parameter in web.xml");
			return;
		}
		int port = StringUtil.parseInt(portStr, -1);
		if(port == -1) {
			System.err.println("The value of the port init parameter is not a parsable int");
			return;
		}
		File storageDir = new File(storage);
		if(!storageDir.exists()) {
			System.err.println("Storage directory " + storageDir.getAbsolutePath()
					+ " does not exist");
			return;
		} else if(!storageDir.isDirectory()) {
			System.err.println("Storage directory " + storageDir.getAbsolutePath()
					+ " is not a directory");
			return;
		} else {
			System.out.println("Storage directory: " + storageDir.getAbsolutePath());
		}
		
		ServerInstance server = ServerManager.newMultiThreadServer();
//		server.setDebug(true);
		ServerConfig config = server.getConfig();
		config.setPort(port);
		config.setThreadPoolSize(10);
		
		try {
			server.start();
		} catch (Throwable t) {
			System.err.println(t.getMessage());
			System.exit(-1);
		}
		
		System.out.println("Worker is listening on port " + port);
		// Install worker app
		WebAppManager manager = server.getAppManager();
		manager.importRootApp(webXmlPath);
//		} else {
//			System.out.println("Usage: ~ PATH_TO_WEB_XML");
//		}
	}
	
	private static ServletInfo getServletInfo(File file) {
		WebAppFactory factory = WebAppFactory.parseWebXml(file);
		if (factory == null) {
			return null;
		}
		String servletName = CrawlerNodeServlet.class.getSimpleName();
		return factory.servletInfoMap.get(servletName);
	}
}
