package edu.upenn.cis455.mapreduce.worker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class MapWorker extends Worker {
	private boolean isStop = false;
	private TaskQueue taskQueue;
	private Object objectJob;
	private Method mapMethod;
	private MapContext context;
	private boolean previousDone = false;
	private ReportMessage reportMessage;

	public MapWorker(TaskQueue taskQueue, Object objectJob, Method mapMethod,
			MapContext context) {
		this.taskQueue = taskQueue;
		this.objectJob = objectJob;
		this.mapMethod = mapMethod;
		this.context = context;

		ReportSingleton reportSingleton = ReportSingleton.getInstance();
		reportMessage = reportSingleton.getReportMessage();
	}

	public void run() {
		String line = null;

		while (true) {
			if (taskQueue.getSize() > 0) {
				try {
					line = taskQueue.getLine();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				if (getPreviousDone()) {
					break;
				} else {
					System.out.println(this.getId() + ": sleep");
					try {
						line = taskQueue.getLine();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if (line != null) {
				String[] parts = line.split("\t");
				if(parts.length < 2){
					continue;
				}
				String currKey = parts[0];
				String value = parts[1];
				try {
					mapMethod.invoke(objectJob, currKey, value, context);
					reportMessage
							.setKeysWritten(reportMessage.getKeysWritten() + 1);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
//				System.out
//					.println((this.getId() + " - " + parts[0] + " : " + parts[1]));
			}

		}

		System.out.println("Thread " + this.getId() + " I am going to die");

	}

	public synchronized void setPreviousDone() {
		previousDone = true;
	}

	public synchronized boolean getPreviousDone() {
		return previousDone;
	}

	public void stopWorker() {
		isStop = true;
	}

	public TaskQueue getTaskQueue() {
		return taskQueue;
	}
}
