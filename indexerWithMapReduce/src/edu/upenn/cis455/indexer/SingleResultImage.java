package edu.upenn.cis455.indexer;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.upenn.cis455.hit.Hit;

public class SingleResultImage implements Comparable<SingleResultImage> {
	private String url;
	private String description;
	private String pageUrl;
	private String type;
	private double TF;
	private ArrayList<Hit> hitList;
	private String originalWord;
	
	public SingleResultImage(String originalWord, String url, String description, String pageUrl,
			String type, double TF, ArrayList<Hit> hitList) {
		this.originalWord = originalWord;
		this.url = url;
		this.description = description;
		this.pageUrl = pageUrl;
		this.type = type;
		this.TF = TF;
		this.hitList = new ArrayList<Hit>(hitList);
	}

	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		result.put("originalWord", originalWord);
		result.put("url", url);
		result.put("description", description);
		result.put("pageUrl", pageUrl);
		result.put("type", type);
		result.put("tf", new Double(TF));

		JSONArray positionList = new JSONArray();

		for (Hit hit : hitList) {
			int position = hit.getPosition();
			int type = hit.getType();

			JSONObject positionInfo = new JSONObject();
			positionInfo.put("position", position);
			positionInfo.put("type", type);

			positionList.add(positionInfo);
		}
		result.put("positions", positionList);

		return result.toString();
	}

	// IMPORTANT: Descend
	@Override
	public int compareTo(SingleResultImage o) {
		if (getTF() > o.getTF()) {
			return -1;
		} else if (getTF() < o.getTF()) {
			return 1;
		} else {
			return 0;
		}
	}

	public double getTF() {
		return TF;
	}
}
