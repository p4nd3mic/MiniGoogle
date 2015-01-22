package edu.upenn.cis455.indexMapReduce;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class DocumentStore {
	private String mapFilePath;
	private String reduceFilePath;
	private BufferedReader br = null;

	public DocumentStore(String mapFilePath, String reduceFilePath) {
		this.mapFilePath = mapFilePath;
		this.reduceFilePath = reduceFilePath;
	}

	public void mapStore() throws Exception {
		String sCurrentLine;

		br = new BufferedReader(new FileReader(mapFilePath));

		while ((sCurrentLine = br.readLine()) != null) {
			System.out.println(sCurrentLine);
		}
		
	}

	public void reduceStore() throws Exception {
		String sCurrentLine;

		br = new BufferedReader(new FileReader(reduceFilePath));

		while ((sCurrentLine = br.readLine()) != null) {
			System.out.println(sCurrentLine);
		}
		
		
		
	}
}
