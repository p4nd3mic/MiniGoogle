package edu.upenn.cis455.mapreduce.worker;

import java.lang.reflect.Method;

public abstract class Worker extends Thread {
	public Worker() {
	}

//	public Worker(TaskQueue taskQueue) {
//	}
//
//	public Worker(TaskQueue reduceTaskQueue, String num_reduce,
//			Object objectJob, Method reduceMethod, MyContext context) {
//	}

	public abstract void run();

	public abstract void stopWorker();
	
	public abstract void setPreviousDone();
	
	public abstract TaskQueue getTaskQueue();
}
