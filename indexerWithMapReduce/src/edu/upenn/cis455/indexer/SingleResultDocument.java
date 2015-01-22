package edu.upenn.cis455.indexer;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.upenn.cis455.hit.FancyHit;
import edu.upenn.cis455.hit.Hit;
import edu.upenn.cis455.hit.PlainHit;

public class SingleResultDocument implements Comparable<SingleResultDocument> {
	private ArrayList<Hit> hitList;
	private double TF;
	private String url;
	private String title;
	private String location;
	private String originalWord;

	private final int maxNUM = 25;

	public SingleResultDocument(String originalWord, String url, String title,
			double TF, ArrayList<Hit> hitList, String location) {
		this.originalWord = originalWord;
		this.url = url;
		this.title = title;
		this.TF = TF;
		this.hitList = new ArrayList<Hit>(hitList);
		this.location = location;
	}

	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		result.put("originalWord", originalWord);
		result.put("url", url);
		result.put("title", title);
		result.put("tf", new Double(TF));
		result.put("location", location);

		JSONArray positionList = new JSONArray();

		int restrict = 0;
		// System.out.println(hitList.size());
		for (Hit hit : hitList) {
			int position = hit.getPosition();
			int type = hit.getType();

			JSONObject positionInfo = new JSONObject();
			positionInfo.put("position", position);
			positionInfo.put("type", type);

			if (type == 0) {
				positionInfo.put("excerpt", ((PlainHit) hit).getExcerpt());
				positionList.add(positionInfo);
			} else {
				if (type == 1) {
					int subtype = ((FancyHit) hit).getSubType();
					positionInfo.put("subtype", subtype);
				} else {
					positionList.add(positionInfo);
				}
			}
			restrict++;
			if (restrict >= maxNUM) {
				break;
			}
		}
		result.put("positions", positionList);

		return result.toString();
	}

	// IMPORTANT: Descend
	@Override
	public int compareTo(SingleResultDocument o) {
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
