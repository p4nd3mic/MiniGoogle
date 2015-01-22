package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.PriorityQueue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WordResultDocument {
	private String content;
	private double numerator;
	private PriorityQueue<SingleResultDocument> singleResultList;
	private ArrayList<SingleResultDocument> filterResultList;

	public WordResultDocument(String content, double numerator,
			ArrayList<SingleResultDocument> singleResultList) {
		this.content = content;
		this.numerator = numerator;
		this.singleResultList = new PriorityQueue<SingleResultDocument>(
				singleResultList);
	}

	public void filter() {
		if (singleResultList.size() <= 300) {
			filterResultList = new ArrayList<SingleResultDocument>(singleResultList);
		} else {
			filterResultList = new ArrayList<SingleResultDocument>();
			int number = 0;
			for (SingleResultDocument singleResult : singleResultList) {
				number++;
				filterResultList.add(singleResult);
				if (number >= 300) {
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
		word.put("numerator", new Double(numerator));
		JSONArray resultList = new JSONArray();
		for (SingleResultDocument one : filterResultList) {
			resultList.add(one);
		}
		word.put("results", resultList);
		return word.toString();
	}
}