package edu.upenn.cis455.webserver.server;

import java.net.InetAddress;

/**
 * Static class storing server's config parameters
 * @author Ziyi Yang
 *
 */
public class ServerConfig {

	public static final String TAG = ServerConfig.class.getSimpleName();
	
	private static final int DEFAULT_SOCKET_TIMEOUT = 60 * 1000;
	private static final int DEFAULT_BACKLOG_SIZE = 100;
	
	private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
	private int backlogSize = DEFAULT_BACKLOG_SIZE;
	private InetAddress hostAddress;
	private int port = 80;
//	private String serverInfo;
	private int threadPoolSize = 4;
	
	public int getSocketTimeout() {
		return socketTimeout;
	}
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	public int getBacklogSize() {
		return backlogSize;
	}
	public void setBacklogSize(int backlogSize) {
		this.backlogSize = backlogSize;
	}
	public InetAddress getHostAddress() {
		return hostAddress;
	}
	public void setHostAddress(InetAddress hostAddress) {
		this.hostAddress = hostAddress;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
//	public String getServerInfo() {
//		return serverInfo;
//	}
//	public void setServerInfo(String serverInfo) {
//		this.serverInfo = serverInfo;
//	}
	public int getThreadPoolSize() {
		return threadPoolSize;
	}
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}
}
