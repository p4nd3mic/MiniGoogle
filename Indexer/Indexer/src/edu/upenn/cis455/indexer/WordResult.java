package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WordResult {
	private String content;
	private double IDF;
	
	private ArrayList<SingleResult> singleResultList;
	
	private ArrayList<SingleResult> filterResultList;
	
	public WordResult(String content, double IDF, ArrayList<SingleResult> singleResultList){
		this.content = content;
		this.IDF = IDF;
		this.singleResultList = new ArrayList<SingleResult>(singleResultList);
	}
	
	public void filter(){
		if(singleResultList.size() <= 100){
			TreeSet<SingleResult> treeTemp = new TreeSet<SingleResult>(singleResultList);
			filterResultList = new ArrayList<SingleResult>(treeTemp);
		}else{
			filterResultList = new ArrayList<SingleResult>();
			TreeSet<SingleResult> treeTemp = new TreeSet<SingleResult>(singleResultList);
			int number = 0;
			for (SingleResult singleResult : treeTemp) {
				number++;
				filterResultList.add(singleResult);
				if(number >= 100){
					break;
				}
			}
		}
	}
	
	@Override
	public String toString() {
		
		JSONObject word = new JSONObject();
		word.put("keyword", content);
		word.put("idf", new Double(IDF));

		JSONArray resultList = new JSONArray();
		for (SingleResult one : filterResultList) {
			resultList.add(one);
		}
		word.put("results", resultList);
		
		return word.toString();
	}
}
