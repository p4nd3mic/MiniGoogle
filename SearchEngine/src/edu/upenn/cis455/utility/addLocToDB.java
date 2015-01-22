package edu.upenn.cis455.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;

public class addLocToDB {

	public static void main(String[] args) throws IOException {
		DBSingleton.setDbPath("/home/cloudera/Desktop/DB");
		DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();
		
//		System.out.println(wrapper.getLocDateInfo("19104"));
		
		
		String sCurrentLine;
		BufferedReader br = new BufferedReader(new FileReader("US.txt"));
		while ((sCurrentLine = br.readLine()) != null) {
			String[] parts = sCurrentLine.split("\t");
			String postCode = parts[1];
			String cityName = parts[2].toLowerCase();
			String stateName = parts[3].toLowerCase();
			String stateAbbr = parts[4].toLowerCase();
			String latitude = parts[8];
			String longitude = parts[9];
			wrapper.addLocDateEntity(postCode, cityName, stateName, stateAbbr,
					latitude, longitude);
		}

		if (br != null){
			br.close();
		}
			
		System.out.println("Finish...");
		DBSingleton.getInstance().closeBDBstore();
	}
	
	public static void addLocToDb(DatabaseWrapper wrapper, File locFile)
			throws IOException {
		String sCurrentLine;
		BufferedReader br = new BufferedReader(new FileReader(locFile));
		while ((sCurrentLine = br.readLine()) != null) {
			String[] parts = sCurrentLine.split("\t");
			String postCode = parts[1];
			String cityName = parts[2].toLowerCase();
			String stateName = parts[3].toLowerCase();
			String stateAbbr = parts[4].toLowerCase();
			String latitude = parts[8];
			String longitude = parts[9];
			wrapper.addLocDateEntity(postCode, cityName, stateName, stateAbbr,
					latitude, longitude);
		}

		if (br != null){
			br.close();
		}
	}
}
