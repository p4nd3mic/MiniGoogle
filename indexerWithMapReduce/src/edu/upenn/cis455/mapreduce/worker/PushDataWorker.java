package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PushDataWorker extends Thread {
	boolean isStop = false;
	TaskQueue taskQueue;
	private static File file;

	public PushDataWorker(TaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}

	public void run() {
		while (!isStop) {
			String content = null;
			try {
				content = taskQueue.getLine();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(content == null){
				continue;
			}
			
			FileWriter fw = null;
			try {
				fw = new FileWriter(file,true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			BufferedWriter bw = new BufferedWriter(fw);
			try {
				//System.out.println("fileHashCode: " + file.hashCode());
//				System.out.println("/pushdataWorker content: " + content.substring(0,100));
				bw.write(content);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				bw.close();
				
//				BufferedReader br = null;
//				String sCurrentLine;
//				br = new BufferedReader(new FileReader(file.getAbsolutePath()));
//				sCurrentLine = br.readLine();
//				System.out.println("NOW THE FILE IS: " + sCurrentLine);
//				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void createTempFile(String spoolIn){
		file = new File(spoolIn+"/temp.txt");
		try {
			if (file.createNewFile()) {
				System.out.println("Spool-In Temp file is created!");
			} else {
				System.out.println("Temp file already exists.");
				file.delete();
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopWorker() {
		isStop = true;
	}
}
