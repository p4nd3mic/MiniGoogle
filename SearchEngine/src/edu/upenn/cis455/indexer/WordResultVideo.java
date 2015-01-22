package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WordResultVideo {
	private String content;
	private double numerator;

	private PriorityQueue<SingleResultVideo> singleResultList;

	private ArrayList<SingleResultVideo> filterResultList;

	public WordResultVideo(String content, double numerator,
			ArrayList<SingleResultVideo> singleResultList) {
		this.content = content;
		this.numerator = numerator;
		this.singleResultList = new PriorityQueue<SingleResultVideo>(
				singleResultList);
	}

	public void filter() {
		if (singleResultList.size() <= 100) {
			filterResultList = new ArrayList<SingleResultVideo>(
					singleResultList);
		} else {
			filterResultList = new ArrayList<SingleResultVideo>();
			int number = 0;
			for (SingleResultVideo singleResult : singleResultList) {
				number++;
				filterResultList.add(singleResult);
				if (number >= 100) {
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
		word.put("numerator", new Double(numerator));

		JSONArray resultList = new JSONArray();
		for (SingleResultVideo one : filterResultList) {
			resultList.add(one);
		}
		word.put("results", resultList);

		return word.toString();
	}
}
