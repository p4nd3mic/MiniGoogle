package edu.upenn.cis455.webserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import edu.upenn.cis455.webserver.servlet.app.WebAppManager;

/**
 * Multithread server instance
 * @author Ziyi Yang
 *
 */
public class MultiThreadServer implements ServerInstance {

	public static final String TAG = MultiThreadServer.class.getSimpleName();
	public static final String SERVER_INFO = "zyyang-web-server/0.2";
//	private static Logger logger = Logger.getLogger(TAG);
	
	private final ServerConfig mConfig;
	private ServerSocket mServerSocket;
	
	private ProcessorPool mProcessorPool = null;
	private ListenerThread mListener = null;
	
	private WebAppManager mAppManager;
	private boolean mStarted = false;
	private boolean mDebug = false;
	
	public MultiThreadServer() {
		mConfig = new ServerConfig();
		mAppManager = new WebAppManager();
		mAppManager.setServerInfo(SERVER_INFO);
	}
	
	@Override
	public ServerConfig getConfig() {
		return mConfig;
	}

	@Override
	public void start() throws IOException {
		if(mStarted) {
			throw new IllegalStateException("Server has benn started");
		}
		int port = mConfig.getPort();
		if(port < 0 || port > 65535) {
			throw new IllegalArgumentException("Invalid port number: " + port);
		}
		int tpSize = mConfig.getThreadPoolSize();
		if(tpSize < 1) {
			throw new IllegalArgumentException("Thread pool size should be at least 1");
		}
		
		mStarted = true;
		mProcessorPool = new ProcessorPool(this, tpSize);
		mServerSocket = new ServerSocket(port, mConfig.getBacklogSize());
		mListener = new ListenerThread();
		mListener.start();
	}
	
	@Override
	public void shutdown() {
		if(!mStarted) {
			throw new IllegalStateException("Server has not benn started");
		}
		mStarted = false;
		System.out.println("Shutting down server...");
		try {
			mListener.interrupt();
			mProcessorPool.shutdown();
			mServerSocket.close();
			mAppManager.destroyApps();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	@Override
	public WebAppManager getAppManager() {
		return mAppManager;
	}

	@Override
	public String getServerInfo() {
		return SERVER_INFO;
	}

	@Override
	public List<WorkerStatus> getWorkerStatus() {
		return mProcessorPool.getProcessorStatus();
	}
	
	@Override
	public boolean isDebug() {
		return mDebug;
	}

	@Override
	public void setDebug(boolean debug) {
		mDebug = debug;
	}

	/**
	 * Thread listening for incoming requests
	 * @author Ziyi Yang
	 *
	 */
	class ListenerThread extends Thread {
		
		@Override
		public void run() {
			try {
				while(true) {
					Socket socket = mServerSocket.accept();
					if(!mStarted) {
						System.out.println("Listener has shutdown");
						break;
					}
					mProcessorPool.assign(socket);
				}
			} catch (SocketException e) {
				System.out.println("Listener has been interrupted");
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
