package edu.upenn.cis455.mapreduce.worker;


import java.util.LinkedList;
import java.net.*;
/*
 * This class stores lines of file
 */
public class TaskQueue {
	private final static int capacity = Integer.MAX_VALUE;
	private LinkedList<String> taskQueue;
	private boolean notTermianl = true;
	public TaskQueue() {
		taskQueue = new LinkedList<String>();
	}

	public synchronized void addLine(String str) throws InterruptedException {
		if (taskQueue.size() < capacity) {
			taskQueue.add(str);
			notify();
		} 
	}

	public synchronized String getLine() throws InterruptedException {
		while (taskQueue.isEmpty() && notTermianl) {
			wait();
		} 
		if(taskQueue.size() > 0){
			return taskQueue.remove();
		}else{
			return null;
		}
		
	}

	public static int getCapacity() {
		return capacity;
	}

	public synchronized int getSize() {
		return taskQueue.size();
	}
	
	public synchronized void AllWorkerNotify(){
		notTermianl = false;
		notify();
	}
}