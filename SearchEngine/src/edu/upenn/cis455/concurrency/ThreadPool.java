package edu.upenn.cis455.concurrency;

import java.lang.Thread.State;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

public class ThreadPool {
	
	public static final String TAG = ThreadPool.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

	private BlockingQueue<Runnable> mQueue;
	private HashSet<Worker> mWorkers;
	private int mMinCapacity;
	private int mMaxCapacity;
	private long mTimeout;
	private boolean mShutdown;
	
	/**
	 * Construct a new thread pool
	 * @param minCapacity the minimum number of threads that will be kept alive in the pool
	 * @param maxCapacity the maximum number of threads allowed in the pool
	 * @param timeout how long a non-kept-alive thread will live in the pool, in millisecond
	 */
	public ThreadPool(int minCapacity, int maxCapacity, long timeout) {
		if(minCapacity < 0) {
			throw new IllegalArgumentException("Min capacity cannot be less than 0");
		}
		if(minCapacity > maxCapacity) {
			throw new IllegalArgumentException("Min capacity cannot be greater than max capacity");
		}
		if(timeout < 0) {
			throw new IllegalArgumentException("Timeout cannot be less than 0");
		}
		this.mMinCapacity = minCapacity;
		this.mMaxCapacity = maxCapacity;
		this.mTimeout = timeout;
		this.mQueue = new BlockingQueue<Runnable>();
		this.mWorkers = new HashSet<ThreadPool.Worker>(mMinCapacity);
		prepare();
	}
	
	/**
	 * Fill the thread pool with keep alive workers
	 */
	private void prepare() {
		mWorkers.clear();
		for(int i = 0; i < mMinCapacity; i++) {
			Worker w = new Worker(true);
			mWorkers.add(w);
			w.start();
		}
	}
	
	/**
	 * The worker thread
	 * @author Ziyi Yang
	 *
	 */
	public class Worker extends Thread {
		
		volatile Runnable task = null;
		private boolean keepAlive;
		
		public Worker(boolean keepAlive) {
			this(keepAlive, null);
		}
		
		public Worker(boolean keepAlive, Runnable firstTask) {
			this.keepAlive = keepAlive;
			this.task = firstTask;
		}

		public Runnable getTask() {
			return task;
		}
		
		public synchronized boolean isRunning() {
			return task != null;
		}

		@Override
		public void run() {
			if(task != null) {
				task.run();
				task = null;
			}
			try {
				while(true) {	// Wait loop
					if(keepAlive) {
						task = mQueue.take();
					} else {
						task = mQueue.poll(mTimeout);
					}
					if(task != null) {
						task.run();
						task = null;
					} else if(!keepAlive) {
						break;
					}
					if(mShutdown) {
						logger.info("Worker has finished its task and ready to shutdown");
						break;
//						interrupt();
					}
				}
			} catch (InterruptedException e) {
				logger.info("Worker has been interrupted");
			}
			// Remove it self
			mWorkers.remove(this);
		}
	}

	/**
	 * Execute the task in the thread pool
	 * @param r
	 */
	public void execute(Runnable r) {
		if(mShutdown) {
			return;
		}
		// Remove terminated workers
		Iterator<Worker> iter = mWorkers.iterator();
		while(iter.hasNext()) {
			Worker w = iter.next();
			if(w.getState() == State.TERMINATED) {
				iter.remove();
			}
		}
		
		// 
		if(mWorkers.size() < mMinCapacity) {
			Worker w = new Worker(true, r);
			mWorkers.add(w);
			w.start();
		} else {
			try {
				// If there is idle thread, add into the task queue
				for(Worker worker : mWorkers) {
					if(!worker.isRunning()) {
						mQueue.put(r);
						return;
					}
				}
				// All threads are busy: add a worker thread if allowed or...
				if(mWorkers.size() < mMaxCapacity) {
					Worker w = new Worker(false, r);
					mWorkers.add(w);
					w.start();
				} else {	// ... add into task queue 
					mQueue.put(r);
				}
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public void printWorkerStatus() {
		int i = 0;
		System.out.print("Worker ");
		for(Worker w : mWorkers) {
			System.out.print("#" + (i++) + ": " + w.getState().name() + ", ");
		}
		System.out.println();
	}
	
	public Set<Worker> getWorkers() {
		return mWorkers;
	}
	
	public void shutdownAll() {
		mShutdown = true;
		for(Worker w : mWorkers) {
			if(!w.isRunning()) {
				w.interrupt();
			}
		}
		mWorkers.clear();
	}
}
