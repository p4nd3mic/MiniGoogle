package edu.upenn.cis455.starter;

import java.io.File;

import edu.upenn.cis455.util.StringUtil;
import edu.upenn.cis455.webserver.server.ServerConfig;
import edu.upenn.cis455.webserver.server.ServerInstance;
import edu.upenn.cis455.webserver.server.ServerManager;
import edu.upenn.cis455.webserver.servlet.app.WebAppManager;

public class StartMaster {
	
	public static final String DEFAULT_WEB_XML_PATH = "WEB-INF/web.xml"; // Default web.xml path
	public static final int DEFAULT_LISTENING_PORT = 80; 

	public static void main(String[] args) {
		String webXmlPath = DEFAULT_WEB_XML_PATH;
		int port = DEFAULT_LISTENING_PORT;
		
		if(args.length >= 1) {
			webXmlPath = args[0];
		}
		
		if(args.length >= 2) {
			String portStr = args[1];
			port = StringUtil.parseInt(portStr, -1);
			if(port == -1) {
				System.err.println("Invalid port number: " + portStr);
				return;
			}
		}
		
		File file = new File(webXmlPath);
		if(!file.exists()) {
			System.err.println("Cannot find " + file.getAbsolutePath());
			return;
		}
		ServerInstance server = ServerManager.newMultiThreadServer();
//		server.setDebug(true);
		ServerConfig config = server.getConfig();
		config.setPort(port);
		config.setThreadPoolSize(10);
		config.setSocketTimeout(15);
		
		try {
			server.start();
		} catch (Throwable t) {
			System.err.println(t.getMessage());
			System.exit(-1);
		}
		
		System.out.println("Master is listening on port " + port);
		// Install master app
		WebAppManager manager = server.getAppManager();
		manager.importRootApp(webXmlPath);
//		} else {
//			System.out.println("Usage: ~ [PATH_TO_WEB_XML] [PORT_NUMBER]");
//		}
	}

}
