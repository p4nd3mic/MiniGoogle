package edu.upenn.cis455;

import java.util.ArrayList;
import java.util.HashMap;

public class SingleKeyResult {

	public HashMap<String, ArrayList<DocEntry>> wordDocEntries = null;
	// public ArrayList<DocEntry> docEntries=null;
	public String keyword;
	public double idf;
	public int type;
	
	public SingleKeyResult() {
		wordDocEntries = new HashMap<String, ArrayList<DocEntry>>();
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public void setIdf(double idf) {
		this.idf = idf;
	}

	public void setType(int type){
		this.type=type;
	}
	
	
	public void addDocEntry(String originalWord, DocEntry docEntry) {
		ArrayList<DocEntry> currentDocEntries = wordDocEntries
				.get(originalWord);
		if (currentDocEntries == null) {
			currentDocEntries = new ArrayList<DocEntry>();
		}
		currentDocEntries.add(docEntry);
		wordDocEntries.put(originalWord, currentDocEntries);
	}

	public DocEntry getDocOfUrl(String originalWord, String url) {

		ArrayList<DocEntry> targetDocs = wordDocEntries.get(originalWord);
		if (targetDocs != null) {
			for (int i = 0; i < targetDocs.size(); i++) {
				if (targetDocs.get(i).url.equals(url)) {
					return targetDocs.get(i);
				}
			}
		}
		return null;
	}
	
	public ArrayList<DocEntry> getDocListOfUrl( String url) {

		ArrayList<DocEntry> targetDocs = new ArrayList<DocEntry>();
		for(String s: this.wordDocEntries.keySet()){
			ArrayList<DocEntry> currentDocs= wordDocEntries.get(s);
			for(int i=0;i<currentDocs.size();i++){
				if(url.equals(currentDocs.get(i).url)){
					targetDocs.add(currentDocs.get(i));
					break;
				}
			}
		}
		return targetDocs;
	}

}
