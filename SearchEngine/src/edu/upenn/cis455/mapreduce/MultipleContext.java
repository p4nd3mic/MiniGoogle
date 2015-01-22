package edu.upenn.cis455.mapreduce;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

public class MultipleContext {
	
	public static final String TAG = MultipleContext.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

//	private File mOutputDir;
//	private String mName;
	private PrintWriter mWriter;
	
	public MultipleContext(File outputDir, String name) {
//		mOutputDir = outputDir;
//		mName = name;
		File outputFile = new File(outputDir, name);
		if(outputFile.exists()) {
			outputFile.delete();
		}
		try {
			outputFile.createNewFile();
			mWriter = new PrintWriter(new FileWriter(outputFile, true), true);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void write(String key, String value) {
		synchronized (mWriter) {
			if(key != null) {
				mWriter.print(key);
			}
			if(key != null && value != null) {
				mWriter.print('\t');
			}
			if(value != null) {
				mWriter.print(value);
			}
			mWriter.println();
		}
	}
	
	public void close() {
		mWriter.close();
	}
}
