package edu.upenn.cis455.indexer;
import java.util.ArrayList;
import java.util.PriorityQueue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WordResultDocument {
	private String content;
	private double IDF;
	private ArrayList<SingleResultDocument> singleResultList;
	private ArrayList<SingleResultDocument> filterResultList;
	
	public WordResultDocument(String content, double IDF, ArrayList<SingleResultDocument> singleResultList){
		this.content = content;
		this.IDF = IDF;
		this.singleResultList = new ArrayList<SingleResultDocument>(singleResultList);
	}
	
	public void filter(){
		if(singleResultList.size() <= 300){
			PriorityQueue<SingleResultDocument> queue = new PriorityQueue<SingleResultDocument>(singleResultList);
			filterResultList = new ArrayList<SingleResultDocument>(queue);
		}else{
			filterResultList = new ArrayList<SingleResultDocument>();
			PriorityQueue<SingleResultDocument> queue = new PriorityQueue<SingleResultDocument>(singleResultList);
			int number = 0;
			for (SingleResultDocument singleResult : queue) {
				number++;
				filterResultList.add(singleResult);
				if(number >= 300){
					break;
				}
			}
		}
	}
	
	@Override
	public String toString() {
		JSONObject word = new JSONObject();
		word.put("type", new Integer(0));
		word.put("keyword", content);
		word.put("idf", new Double(IDF));
		JSONArray resultList = new JSONArray();
		for (SingleResultDocument one : filterResultList) {
			resultList.add(one);
		}
		word.put("results", resultList);
		return word.toString();
	}
}