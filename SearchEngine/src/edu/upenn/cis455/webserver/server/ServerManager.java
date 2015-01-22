package edu.upenn.cis455.webserver.server;

import java.util.List;

import edu.upenn.cis455.webserver.server.ServerInstance.WorkerStatus;
import edu.upenn.cis455.webserver.servlet.app.WebAppManager;

/**
 * Global class for creating and accessing the server
 * @author Ziyi Yang
 *
 */
public class ServerManager {

	public static final String TAG = ServerManager.class.getSimpleName();
	
	private static ServerInstance mCurrentServer = null;

	/**
	 * Get a new multi-thread server instance. <br/>
	 * It will replace the current server instance in the manager
	 * @return
	 */
	public static ServerInstance newMultiThreadServer() {
		mCurrentServer = new MultiThreadServer();
		return mCurrentServer;
	}
	
	/**
	 * Get the current server instance
	 * @return
	 */
	public static ServerInstance getCurrentServer() {
		return mCurrentServer;
	}
	
	/**
	 * Get the current server config. Convenience method for calling
	 * ServerManager.getCurrentServer().getConfig()
	 * @return
	 */
	public static ServerConfig getCurrentServerConfig() {
		if(mCurrentServer == null) {
			return null;
		}
		return mCurrentServer.getConfig();
	}
	
	/**
	 * Get the current server worker status. Convenience method for calling
	 * ServerManager.getCurrentServer().getWorkerStatus()
	 * @return
	 */
	public static List<WorkerStatus> getCurrentWorkerStatus() {
		if(mCurrentServer == null) {
			return null;
		}
		return mCurrentServer.getWorkerStatus();
	}
	
	/**
	 * Get the current web app manager. Convenience method for calling
	 * ServerManager.getCurrentServer().getAppManager()
	 * @return
	 */
	public static WebAppManager getCurrentWebAppManager() {
		if(mCurrentServer == null) {
			return null;
		}
		return mCurrentServer.getAppManager();
	}
}
