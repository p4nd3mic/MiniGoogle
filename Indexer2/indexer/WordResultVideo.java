package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WordResultVideo {
	private String content;
	private double IDF;
	
	private ArrayList<SingleResultVideo> singleResultList;
	
	private ArrayList<SingleResultVideo> filterResultList;
	
	public WordResultVideo(String content, double IDF, ArrayList<SingleResultVideo> singleResultList){
		this.content = content;
		this.IDF = IDF;
		this.singleResultList = new ArrayList<SingleResultVideo>(singleResultList);
	}
	
	public void filter(){
		if(singleResultList.size() <= 100){
			PriorityQueue<SingleResultVideo> queue = new PriorityQueue<SingleResultVideo>(singleResultList);
			filterResultList = new ArrayList<SingleResultVideo>(queue);
		}else{
			filterResultList = new ArrayList<SingleResultVideo>();
			PriorityQueue<SingleResultVideo> queue = new PriorityQueue<SingleResultVideo>(singleResultList);
			int number = 0;
			for (SingleResultVideo singleResult : queue) {
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
		word.put("type", new Integer(2));
		word.put("keyword", content);
		word.put("idf", new Double(IDF));

		JSONArray resultList = new JSONArray();
		for (SingleResultVideo one : filterResultList) {
			resultList.add(one);
		}
		word.put("results", resultList);
		
		return word.toString();
	}
}
