package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WordResultImage {
	private String content;
	private double numerator;

	private PriorityQueue<SingleResultImage> singleResultList;

	private ArrayList<SingleResultImage> filterResultList;

	public WordResultImage(String content, double numerator,
			ArrayList<SingleResultImage> singleResultList) {
		this.content = content;
		this.numerator = numerator;
		this.singleResultList = new PriorityQueue<SingleResultImage>(
				singleResultList);
	}

	public void filter() {
		if (singleResultList.size() <= 100) {
			filterResultList = new ArrayList<SingleResultImage>(
					singleResultList);
		} else {
			filterResultList = new ArrayList<SingleResultImage>();
			int number = 0;
			for (SingleResultImage singleResult : singleResultList) {
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
		word.put("type", new Integer(1));
		word.put("keyword", content);
		word.put("numerator", new Double(numerator));

		JSONArray resultList = new JSONArray();
		for (SingleResultImage one : filterResultList) {
			resultList.add(one);
		}
		word.put("results", resultList);

		return word.toString();
	}
}
