package edu.upenn.cis455.webserver.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.upenn.cis455.concurrency.BlockingQueue;
import edu.upenn.cis455.webserver.server.ServerInstance.WorkerStatus;

public class ProcessorPool {

	public static final String TAG = ProcessorPool.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private BlockingQueue<Socket> mSocketQueue;
	private Set<ProcessorThread> mProcessors;
	private ServerInstance mServerInstance;
	private int mPoolSize;
	private boolean mRunning = false;
	
	public ProcessorPool(ServerInstance server, int nProcessors) {
		mSocketQueue = new BlockingQueue<Socket>();
		mProcessors = new HashSet<ProcessorPool.ProcessorThread>(nProcessors);
		mServerInstance = server;
		mPoolSize = nProcessors;
		start();
	}
	
	public void assign(Socket socket) {
		int diff = mPoolSize - mProcessors.size();
		if(diff > 0) {
			for(int i = 0; i < diff; i++) {
				ProcessorThread t = new ProcessorThread();
				mProcessors.add(t);
				t.start();
			}
		}
		try {
			mSocketQueue.put(socket);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}

	private void start() {
		mRunning = true;
		mProcessors.clear();
		for(int i = 0; i < mPoolSize; i++) {
			ProcessorThread t = new ProcessorThread();
			mProcessors.add(t);
			t.start();
		}
	}
	
	public void shutdown() {
		mRunning = false;
		for(ProcessorThread t : mProcessors) {
			t.interrupt();
		}
		mProcessors.clear();
	}
	
	public List<WorkerStatus> getProcessorStatus() {
		List<WorkerStatus> list = new ArrayList<WorkerStatus>();
		for(ProcessorThread thread : mProcessors) {
			WorkerStatus status = new WorkerStatus();
			status.running = thread.isRunning();
			HttpProcessor processor = thread.getProcessor();
			status.url = processor.getProcessingUrl();
			list.add(status);
		}
		return list;
	}

	private class ProcessorThread extends Thread {
		
		private HttpProcessor processor;
		private volatile boolean running;

		public ProcessorThread() {
			processor = new HttpProcessor(mServerInstance);
		}

		public HttpProcessor getProcessor() {
			return processor;
		}

		public boolean isRunning() {
			return running;
		}

		@Override
		public void run() {
			try {
				while(mRunning) {
					Socket socket = mSocketQueue.take();
					if(socket != null) {
						running = true;
						try {
							processor.process(socket);
						} catch (Exception e) {
							System.err.println(e.getMessage());
						}
						running = false;
					}
					
					try {
						socket.close();
					} catch (IOException e) {
					}
					socket = null;
				}
				
//				logger.info("Processor has finished its task and ready to shutdown");
			} catch (InterruptedException e) {
//				System.out.println("Processor has been interrupted");
			}
		}
	}
}
