package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WordResultImage {
	private String content;
	private double IDF;
	
	private ArrayList<SingleResultImage> singleResultList;
	
	private ArrayList<SingleResultImage> filterResultList;
	
	public WordResultImage(String content, double IDF, ArrayList<SingleResultImage> singleResultList){
		this.content = content;
		this.IDF = IDF;
		this.singleResultList = new ArrayList<SingleResultImage>(singleResultList);
	}
	
	public void filter(){
		if(singleResultList.size() <= 100){
			PriorityQueue<SingleResultImage> queue = new PriorityQueue<SingleResultImage>(singleResultList);
			filterResultList = new ArrayList<SingleResultImage>(queue);
		}else{
			filterResultList = new ArrayList<SingleResultImage>();
			PriorityQueue<SingleResultImage> queue = new PriorityQueue<SingleResultImage>(singleResultList);
			int number = 0;
			for (SingleResultImage singleResult : queue) {
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
		word.put("type", new Integer(1));
		word.put("keyword", content);
		word.put("idf", new Double(IDF));

		JSONArray resultList = new JSONArray();
		for (SingleResultImage one : filterResultList) {
			resultList.add(one);
		}
		word.put("results", resultList);
		
		return word.toString();
	}
}
