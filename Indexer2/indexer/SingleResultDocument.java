package edu.upenn.cis455.indexer;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.upenn.cis455.hit.FancyHit;
import edu.upenn.cis455.hit.Hit;

public class SingleResultDocument implements Comparable<SingleResultDocument>{
	private ArrayList<Hit> hitList;
	private double TF;

	private String url;
	private String title;
	private ArrayList<String> excerpt;

	private String location;
	private String originalWord;
	@Deprecated
	public SingleResultDocument(String url, String title, ArrayList<String> excerpt, double TF,
			ArrayList<Hit> hitList, String location) {
		this.url = url;
		this.title = title;
		this.excerpt = new ArrayList<String>(excerpt);
		this.TF = TF;
		this.hitList = new ArrayList<Hit>(hitList);
		this.location = location;
	}

	public SingleResultDocument(String originalWord, String url, String title, ArrayList<String> excerpt, double TF,
			ArrayList<Hit> hitList, String location) {
		this.originalWord = originalWord;
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
		result.put("originalWord", originalWord);
		result.put("url", url);
		result.put("title", title);
		result.put("tf", new Double(TF));
		result.put("location", location);
		
		JSONArray positionList = new JSONArray();
		
		int restrict = 0;
		boolean soFreq = false;
		if(hitList.size() >= 20){
			soFreq = true;
		}
//		System.out.println(hitList.size());
		for (Hit hit : hitList) {
			int position = hit.getPosition();
			int type = hit.getType();
			
			JSONObject positionInfo = new JSONObject();
			positionInfo.put("position", position);
			positionInfo.put("type", type);
			
			
			if(soFreq){
				if(type == 0){
					StringBuilder subExcerpt = new StringBuilder();
					subExcerpt.append("...");
					int excerptLen = excerpt.size() - 1;
					
					int beginIndex = Math.max(0, position - 5);
					int endIndex = Math.min(excerptLen, position + 5);
					
					for (int i = beginIndex; i <= endIndex; i++) {
						subExcerpt.append(excerpt.get(i) + " ");
					}
					
					subExcerpt.append("...");
					
					positionInfo.put("excerpt", subExcerpt.toString());
					positionList.add(positionInfo);
					
				}
			}else{
				if(type == 0){
					StringBuilder subExcerpt = new StringBuilder();
					subExcerpt.append("...");
					int excerptLen = excerpt.size() - 1;
					
					int beginIndex = Math.max(0, position - 13);
					int endIndex = Math.min(excerptLen, position + 13);
					
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
			restrict++;
			if(soFreq && restrict >= 15){
				break;
			}
		}
		result.put("positions", positionList);
		
		return result.toString();
	}
	
	// IMPORTANT: Descend
	@Override
	public int compareTo(SingleResultDocument o) {
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
