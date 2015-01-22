package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.upenn.cis455.mapreduce.Context;

public class MapContext implements Context {
	private String spoolOutDirPath;
	private WriteSpoolOutSingleton writeSpool = WriteSpoolOutSingleton
			.getInstance();
	private String numWorkers;

	public MapContext(String spoolOutDirPath) {
		this.spoolOutDirPath = spoolOutDirPath;
	}

	public void write(String key, String value) {
		int index = HashText.whichOneWorker(key, numWorkers);
		String output = key+"\t"+value+"\n";
		try {
			writeSpool.getBufferedWriter(index).write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setNumWorkers(int numWorkers) {
		this.numWorkers = String.valueOf(numWorkers);
	}
}
