package edu.upenn.cis455.indexer;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SingleResult implements Comparable<SingleResult>{
	private ArrayList<Hit> hitList;
	private double TF;

	private String url;
	private String title;
	private ArrayList<String> excerpt;

	private String location;
	
	@Deprecated
	public SingleResult(int docId, String url, String title, double TF,
			ArrayList<Hit> hitList) {
		this.url = url;
		this.title = title;
		this.TF = TF;
		this.hitList = new ArrayList<Hit>(hitList);
	}
	
	public SingleResult(String url, String title, ArrayList<String> excerpt, double TF,
			ArrayList<Hit> hitList, String location) {
		this.url = url;
		this.title = title;
		this.excerpt = new ArrayList<String>(excerpt);
		this.TF = TF;
		this.hitList = new ArrayList<Hit>(hitList);
		this.location = location;
	}


	
	@Override
	public String toString() {
		JSONObject result = new JSONObject();
		result.put("url", url);
		result.put("title", title);
		result.put("tf", new Double(TF));
		result.put("location", location);

		JSONArray positionList = new JSONArray();
		
		for (Hit hit : hitList) {
			int position = hit.getPosition();
			int type = hit.getType();
			
			JSONObject positionInfo = new JSONObject();
			positionInfo.put("position", position);
			positionInfo.put("type", type);
			
			
			if(type == 0){
				StringBuilder subExcerpt = new StringBuilder();
				subExcerpt.append("...");
				int excerptLen = excerpt.size() - 1;
				
				int beginIndex = Math.max(0, position - 30);
				int endIndex = Math.min(excerptLen, position + 30);
				
				for (int i = beginIndex; i <= endIndex; i++) {
					subExcerpt.append(excerpt.get(i) + " ");
				}
				
				subExcerpt.append("...");
				
				positionInfo.put("excerpt", subExcerpt.toString());
				positionList.add(positionInfo);
				
			}else{
				if(type == 1){
					int subtype = ((FancyHit)hit).getSubType();
					positionInfo.put("subtype", subtype);
				}
				
				
				positionList.add(positionInfo);
			}
		}
		result.put("positions", positionList);
		
		return result.toString();
	}
	
	// IMPORTANT: Descend
	@Override
	public int compareTo(SingleResult o) {
		if(getTF() > o.getTF()){
			return -1;
		}else if(getTF() < o.getTF()){
			return 1;
		}else{
			return 0;
		}
	}

	public ArrayList<Hit> getHitList() {
		return hitList;
	}

	public void setHitList(ArrayList<Hit> hitList) {
		this.hitList = hitList;
	}

	public double getTF() {
		return TF;
	}

	public void setTF(double tF) {
		TF = tF;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ArrayList<String> getExcerpt() {
		return new ArrayList<String>(excerpt);
	}

	public void setExcerpt(ArrayList<String> excerpt) {
		this.excerpt = new ArrayList<String>(excerpt);
	}

}
