package edu.upenn.cis455.mapreduce.worker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ReduceWorker extends Worker {
	private boolean isStop = false;
	private TaskQueue taskQueue;
	private Object objectJob;
	private Method reduceMethod;
	private ReduceContext context;
	private boolean previousDone = false;
	private ReportMessage reportMessage;

	public ReduceWorker(TaskQueue taskQueue, Object objectJob,
			Method reduceMethod, ReduceContext context) {
		this.taskQueue = taskQueue;
		this.objectJob = objectJob;
		this.reduceMethod = reduceMethod;
		this.context = context;

		ReportSingleton reportSingleton = ReportSingleton.getInstance();
		reportMessage = reportSingleton.getReportMessage();
	}

	public void run() {
		// while (!isStop) {
		String line = null;
		try {
			line = taskQueue.getLine();
			if (line != null) {
				ArrayList<String> value = new ArrayList<String>();
				String[] values = null;
				String[] parts = line.split("\t");
				System.out.println("line: " + line);

				String currKey = parts[0];
				System.out.println("currKey: " + currKey);
				value.add(parts[1]);
				while (true) {
					if (taskQueue.getSize() > 0) {
						line = taskQueue.getLine();
					} else {
						if (getPreviousDone()) {
							break;
						} else {
							System.out.println(this.getId() + ": sleep");
							line = taskQueue.getLine();
						}
					}
					if (line != null) {
						parts = line.split("\t");						
						if (!currKey.equals(parts[0])) {
							values = value.toArray(new String[value.size()]);
							// run reduce function
//							System.out.println(this.getId()
//									+ ": invoke reduce funcion -- " + currKey);

							reportMessage.addKeysWritten();;

							reduceMethod.invoke(objectJob, currKey, values,
									context);
							value.clear();
							currKey = parts[0];
							value.add(parts[1]);
						} else {
							value.add(parts[1]);
						}
//						System.out.println((this.getId() + " - " + parts[0]
//								+ " : " + parts[1]));
					}

				}
				// run reduce function
				values = value.toArray(new String[value.size()]);
				if (values != null) {
//					System.out.println(this.getId()
//							+ ": invoke final reduce funcion -- " + currKey);
					reportMessage.addKeysWritten();
					
					reduceMethod.invoke(objectJob, currKey, values, context);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		System.out.println("Thread " + this.getId() + " I am going to die");
		// }
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
