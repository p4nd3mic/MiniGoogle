package edu.upenn.cis455.mapreduce.job;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import edu.upenn.cis455.mapreduce.Context;

public class MapContext implements Context {
	String storageDirectory;
	boolean mapReduce;
	String output;
	boolean first = true;
	String worker;
	int keyWritten;
	Map<String, BigInteger[]> workerRanges = new HashMap<String, BigInteger[]>();
	Map<String, String> workerNames = new HashMap<String, String>();
	
	public void setWorkerNames(Map<String, String> workerNames) {
		this.workerNames = workerNames;
	}
	public void setWorkerRanges(Map<String, BigInteger[]> workerRanges) {
		this.workerRanges = workerRanges;
	}
	public void setStorageDirectory(String storageDirectory) {
		this.storageDirectory = storageDirectory;
	}
	public void setFirst(boolean first) {
		this.first = first;
	}
	public void setMapReduce(boolean mapReduce) {
		this.mapReduce = mapReduce;
	}
	public void setOutputDirectory(String output) {
		this.output = output;
	}
	public void setKeysWritten(int keys) {
		this.keyWritten = keys;
	}
	public void setWorker(String worker) {
		this.worker = worker;
	}
	public int getWrites() {
		return this.keyWritten;
	}
	
	public String checkWorker(String key) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
			byte[] digest = md.digest(key.getBytes());
			BigInteger bi = new BigInteger(1, digest);
			String worker = "";
			for (String s : workerRanges.keySet()) {
				BigInteger[] biArr = workerRanges.get(s);
				if ((bi.compareTo(biArr[0])) == 1
						&& (bi.compareTo(biArr[1])) == -1) {
					worker = s;

					break;
				} 
			}
			return worker;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	@Override
	public void write(String key, String value) {
		//should write to spoolout I believe
		//send to file. Then run reduce in worker. After get hashing working. Test multiple workers tmrw.
		//will need to know if this is a map or reduce
		if (mapReduce) {
		try {
			first = true;
			String worker = checkWorker(key);
			String name = workerNames.get(worker);
			File file = new File(storageDirectory+"spool-out/"+name);
			if (!file.exists()) {
				file.createNewFile();
			}
			keyWritten += 1;
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(key+"\t"+value+"\n");
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		} else {
			File outputDir = new File(storageDirectory+this.output);
			if (first && outputDir.exists()) {
				deleteDir(outputDir);
				outputDir.mkdir();
				first = false;
			} 
				if (!outputDir.exists()) {
					outputDir.mkdir();
					first = false;
				}
				FileWriter fw;
				try {
					File file = new File(outputDir+"/part-r-00000");
					if (!file.exists()) {
							file.createNewFile();
					}
					fw = new FileWriter(file.getAbsoluteFile(), true);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(key+"\t"+value+"\n");
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			
		}
		//hash and send to spoolout
	}
	public void deleteDir(File directory) {
		if (directory.isDirectory()) {
			if (directory.list().length == 0) {
				directory.delete();
			} else {
				// list all contents
				String files[] = directory.list();

				for (String temp : files) {
					File fileDelete = new File(directory, temp);
					deleteDir(fileDelete);
				}
				if (directory.list().length == 0) {
					directory.delete();
				}
			}
		} else {
			// if file then delete file
			directory.delete();
		}

	}
}
