package edu.upenn.cis455.webserver.server;

import java.io.IOException;
import java.util.List;

import edu.upenn.cis455.webserver.servlet.app.WebAppManager;

public interface ServerInstance {
	
	/**
	 * Get config for this server
	 * @return
	 */
	ServerConfig getConfig();

	/**
	 * Launch the server. Start listening incoming connections
	 * @return True if the server is successfully started. False otherwise
	 */
	void start() throws IOException;
	
	/**
	 * Shutdown the server
	 */
	void shutdown();
	
	WebAppManager getAppManager();
	
	/**
	 * Get the status of current thread pool
	 * @return
	 */
	List<WorkerStatus> getWorkerStatus();
	
	String getServerInfo();
	
	boolean isDebug();
	
	void setDebug(boolean debug);
	
	public static class WorkerStatus {
		/**
		 * Is the thread currently processing requests
		 */
		public boolean running;
		/**
		 * The url the thread is currently processing, null if idle
		 */
		public String url;
	}
}
