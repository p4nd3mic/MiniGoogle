package edu.upenn.cis455.mapreduce.master;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import edu.upenn.cis455.mapreduce.WorkerStatus;

public class WorkerInfo implements Comparable<WorkerInfo> {

	public String id;
	public String host;
	public int port;
	public WorkerStatus status;
	public String job;
	public int keysRead;
	public int keysWritten;
	// Parameters for estimating progress
	public int keysMapped;
	public int keysToReduce;
	public int keysReduced;
	public long time;
	private Map<String, String> params = new HashMap<String, String>();

	public String getParameter(String name) {
		return params.get(name);
	}

	public void addParameter(String name, String value) {
		params.put(name, value);
	}

	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(params.keySet());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(host).append(':').append(port).append('\t')
				.append(status != null ? status.getName() : "").append('\t');
		return sb.toString();
	}

	@Override
	public int compareTo(WorkerInfo arg0) {
		return id.compareTo(arg0.id);
	}
}
