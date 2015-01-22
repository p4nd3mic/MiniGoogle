package edu.upenn.cis455.mapreduce.worker;

import java.lang.reflect.Method;
import java.util.ArrayList;

/*
 * This class ThreadPool simulates ExecutorService in Java
 */
public class ThreadPool {
	private ArrayList<Worker> pool = new ArrayList<Worker>();
	private boolean allThreadStop = false;

	public ThreadPool(int size, ArrayList<TaskQueue> ownTaskQueue,
			Object objectJob, Method reduceMethod, ReduceContext context) {
		for (int i = 0; i < size; i++) {
			Worker worker = new ReduceWorker(ownTaskQueue.get(i), objectJob,
					reduceMethod, context);
			pool.add(worker);
			worker.start();
		}
	}

	public ThreadPool(int size, ArrayList<TaskQueue> ownTaskQueue,
			Object objectJob, Method mapMethod, MapContext context) {
		for (int i = 0; i < size; i++) {
			Worker worker = new MapWorker(ownTaskQueue.get(i), objectJob,
					mapMethod, context);
			pool.add(worker);
			worker.start();
		}
	}

	public int getNumberOfWorker() {
		return pool.size();
	}

	public synchronized void stopAllWorker() {
		for (Worker worker : pool) {
			worker.stopWorker();
		}
		allThreadStop = true;
	}

	public synchronized void AllWorkerNotify() {
		for (Worker worker : pool) {
			System.out.println("begin to notify");
			worker.getTaskQueue().AllWorkerNotify();
		}
	}

	public synchronized void AllWorkerSetPreviousDone() {
		for (Worker worker : pool) {
			worker.setPreviousDone();
		}
	}

	public synchronized boolean isAllThreadStop() {
		return allThreadStop;
	}
	
	public ArrayList<Worker> getPool(){
		return pool;
	}
	
//	public synchronized boolean allWaiting(){
//		for (Worker worker : pool) {
//			System.out.println(worker.getId() + " : " + worker.getState());
//			if(worker.getState() != Thread.State.WAITING){
//				return false;
//			}
//		}
//		return true;
//	}
}
