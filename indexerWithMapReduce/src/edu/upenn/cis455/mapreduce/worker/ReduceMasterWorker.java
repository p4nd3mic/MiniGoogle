package edu.upenn.cis455.mapreduce.worker;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class ReduceMasterWorker extends Thread {
	private boolean isStop = false;
	private TaskQueue reduceTaskQueue;
	private String num_reduce;
	private Object objectJob;
	private Method reduceMethod;
	private ReduceContext context;
	private ThreadPool threadpool;
	private ArrayList<TaskQueue> ownTaskQueue;
	private boolean previousDone = false;
	private ReportMessage reportMessage;

	public ReduceMasterWorker(TaskQueue reduceTaskQueue, String num_reduce,
			Object objectJob, Method reduceMethod, ReduceContext context) {
		this.reduceTaskQueue = reduceTaskQueue;
		this.num_reduce = num_reduce;
		this.objectJob = objectJob;
		this.reduceMethod = reduceMethod;
		this.context = context;

		ReportSingleton reportSingleton = ReportSingleton.getInstance();
		reportMessage = reportSingleton.getReportMessage();

		// For each worker
		int num = Integer.valueOf(num_reduce);
		this.ownTaskQueue = new ArrayList<TaskQueue>();
		// System.out.println("num_reduce: " + num);
		for (int i = 0; i < num; i++) {
			ownTaskQueue.add(new TaskQueue());
		}

		threadpool = new ThreadPool(num, ownTaskQueue, objectJob, reduceMethod,
				context);
	}

	public void run() {
		while (!isStop()) {
			String content = null;
			try {
				if (reduceTaskQueue.getSize() > 0) {
					content = reduceTaskQueue.getLine();
					if (content != null) {
						reportMessage.addKeysRead();
						;
					}
				} else {
					if (getPreviousDone()) {
						break;
					} else {
						content = reduceTaskQueue.getLine();
						if (content != null) {
							reportMessage.addKeysRead();
						}
					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (content == null) {
				continue;
			}

			int index = HashText.whichOneThread(content, num_reduce);
			try {
				// System.out.println("index: " + index + " - " + "content: "
				// + content);
				ownTaskQueue.get(index).addLine(content);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		threadpool.AllWorkerSetPreviousDone();
		notifyAllWorkers();

		for (Worker worker : threadpool.getPool()) {
			try {
				worker.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("ReduceMaster: I am going to die..");
		// while(!threadpool.allWaiting()){
		// }
	}

	public synchronized void notifyAllWorkers() {
		threadpool.AllWorkerNotify();
	}

	public synchronized boolean isStop() {
		return isStop;
	}

	public synchronized void stopWorker() {
		isStop = true;
	}

	public synchronized void setPreviousDone() {
		previousDone = true;

	}

	public synchronized boolean getPreviousDone() {
		return previousDone;
	}
}
