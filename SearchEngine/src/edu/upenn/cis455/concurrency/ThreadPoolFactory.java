package edu.upenn.cis455.concurrency;


public class ThreadPoolFactory {

	/**
	 * Create a thread pool with one single thread
	 * @return
	 */
	public static ThreadPool newSingleThreadPool() {
		return new ThreadPool(1, 1, 0);
	}
	
	/**
	 * Create a thread pool with a fixed number of threads
	 * @param nThreads Number of threads in the pool
	 * @return
	 */
	public static ThreadPool newFixedThreadPool(int nThreads) {
		if(nThreads < 1) {
			throw new IllegalArgumentException("Number of threads cannot be less than 1");
		}
		return new ThreadPool(nThreads, nThreads, 0);
	}
	
	/**
	 * Create a thread pool that will reuse previously created threads,
	 * and will remove a thread if being idle for 60 seconds
	 * @return
	 */
	public static ThreadPool newCachedThreadPool() {
		return new ThreadPool(0, Integer.MAX_VALUE, 60 * 1000);
	}
	
	/**
	 * Create a thread pool that will reuse previously created threads,
	 * and will remove a thread if being idle for 60 seconds
	 * @param limit Maximum number of threads allowed in the pool
	 * @return
	 */
	public static ThreadPool newCachedThreadPool(int limit) {
		return new ThreadPool(0, limit, 60 * 1000);
	}
}
