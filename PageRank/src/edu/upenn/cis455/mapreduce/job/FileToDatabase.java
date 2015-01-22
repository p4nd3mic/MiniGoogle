package edu.upenn.cis455.mapreduce.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class FileToDatabase {
	public static void main(String[] args) {
		File storedRanks = new File("storedRanks");
		
		if (storedRanks.exists()) {
			storedRanks.mkdir();
		}
		AddGetFromDB ranks = new AddGetFromDB(storedRanks);
		FileReader fw;
		try {
			fw = new FileReader("output/part-r-00000");
			BufferedReader br = new BufferedReader(fw);
			
			String line;
			String[] keyVal;
			while ((line = br.readLine()) != null) {
				keyVal = line.split("/t");
				ranks.addRankToDB(keyVal[0], keyVal[1]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
}
