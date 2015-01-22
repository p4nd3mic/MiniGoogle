package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.upenn.cis455.mapreduce.Context;

public class ReduceContext implements Context{
	private String outputDirPath;
	private File file;
	
	public ReduceContext(String outputDirPath){
		this.outputDirPath = outputDirPath;
		makeDir(outputDirPath);
		createFile(outputDirPath);
	}

	public static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					delete(fileDelete);
				}
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			file.delete();
		}
	}

	public static boolean makeDir(String fileName) {
		File directory = new File(fileName);
		if (directory.exists()) {
			try {
				delete(directory);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return directory.mkdir();
		} else {
			return directory.mkdir();
		}
	}
	
	public void createFile(String outputDirPath){
		file = new File(outputDirPath+"/result.txt");
		try {
			if (file.createNewFile()) {
				System.out.println("Result file is created!");
			} else {
				System.out.println("Result file already exists.");
				file.delete();
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public synchronized void write(String key, String value) {
		String content = key + "\t" + value;
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(file,true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BufferedWriter bw = new BufferedWriter(fw);
		try {
			bw.write(content);
			bw.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
