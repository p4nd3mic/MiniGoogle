package edu.upenn.cis455.concurrency;

import java.util.LinkedList;
import java.util.Queue;

public class BlockingQueue<E> {

	public static final String TAG = BlockingQueue.class.getSimpleName();
	
	private Queue<E> mQueue;
	private int mCapacity;
	
	public BlockingQueue() {
		this(Integer.MAX_VALUE);
	}
	
	public BlockingQueue(int capacity) {
		if(capacity < 1) {
			throw new IllegalArgumentException();
		}
		this.mCapacity = capacity;
		this.mQueue = new LinkedList<E>();
	}
	
	/**
	 * Insert an item into the queue, block if the queue is full
	 * @param e the item to add
	 * @throws InterruptedException 
	 */
	public synchronized void put(E e) throws InterruptedException {
		if(e == null) {
			throw new NullPointerException();
		}
		while(mQueue.size() >= mCapacity) {	// In while loop to avoid "spurious wakeups"
			wait();
		}
		if(mQueue.size() == 0) {	// If not 0 then no threads are waiting
			notifyAll();
		}
		mQueue.add(e);
		
	}
	
	/**
	 * Take the first item in the queue, block if the queue if empty
	 * @return The first item in the queue
	 * @throws InterruptedException 
	 */
	public synchronized E take() throws InterruptedException {
		while(mQueue.isEmpty()) {	// In while loop to avoid "spurious wakeups"
			wait();
		}
		if(mQueue.size() == mCapacity) {	// If not full then no threads are waiting
			notifyAll();
		}
		E item = mQueue.remove();
		return item;
	}
	
	/**
	 * Take the first item in the queue, wait until timeout
	 * @param timeout timeout in millisecond
	 * @return The first item in the queue, null if timeout
	 * @throws InterruptedException 
	 */
	public synchronized E poll(long timeout) throws InterruptedException {
		while(mQueue.isEmpty()) {
			long time1 = System.currentTimeMillis();
			wait(timeout);
			long time2 = System.currentTimeMillis();
			if(time2 - time1 >= timeout - 10) {
				return null;
			}
		}
		if(mQueue.size() == mCapacity) {	// If not full then no threads are waiting
			notifyAll();
		}
		E item = mQueue.remove();
		return item;
	}
	
	/**
	 * Take the first item in the queue, return null if it is empty
	 * @return
	 */
	public synchronized E poll() {
		E item = null;
		if(!mQueue.isEmpty()) {
			item = mQueue.remove();
		}
		return item;
	}
	
	public int size() {
		return mQueue.size();
	}
}
