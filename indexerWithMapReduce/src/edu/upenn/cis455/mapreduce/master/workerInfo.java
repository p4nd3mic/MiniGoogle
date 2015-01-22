package edu.upenn.cis455.mapreduce.master;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class workerInfo {
	private ConcurrentHashMap<String, HashMap<String, String>> workerStatus = new ConcurrentHashMap<String, HashMap<String, String>>();
	private static final workerInfo info = new workerInfo();

	private workerInfo() {

	}

	public static workerInfo getInstance() {
		return info;
	}
	
	public ConcurrentHashMap<String, HashMap<String, String>> getWorkerStatus(){
		return workerStatus;
	}

}
