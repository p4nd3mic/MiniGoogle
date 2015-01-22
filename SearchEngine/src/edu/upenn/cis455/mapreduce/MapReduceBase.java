package edu.upenn.cis455.mapreduce;

import java.util.Map;

public class MapReduceBase {
	
	private String storageDir = null;

	public void setup(Map<String, String> params) {}
	
	public void cleanUp() {}

	public String getStorageDir() {
		return storageDir;
	}

	public void setStorageDir(String storageDir) {
		this.storageDir = storageDir;
	}
}
