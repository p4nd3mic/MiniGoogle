package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class WriteSpoolOutSingleton {
	private static final WriteSpoolOutSingleton writePermit = new WriteSpoolOutSingleton();
	private ArrayList<BufferedWriter> writerArray;

	private WriteSpoolOutSingleton() {

	}

	public static WriteSpoolOutSingleton getInstance() {
		return writePermit;
	}
	
	//index start:0
	public BufferedWriter getBufferedWriter(int index){
		return writerArray.get(index);
	}
	
	public void createWriterForEachFile(ArrayList<File> fileArray) {
		writerArray = new ArrayList<BufferedWriter>();
		for (File file : fileArray) {
			FileWriter fw = null;
			try {
				fw = new FileWriter(file.getAbsoluteFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
			BufferedWriter bw = new BufferedWriter(fw);
			writerArray.add(bw);
		}
	}
	
	public void closeWriterForEachFile() {
		for (BufferedWriter bufferedWriter : writerArray) {
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
