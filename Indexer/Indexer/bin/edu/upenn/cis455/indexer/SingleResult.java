package edu.upenn.cis455.indexer;

import java.util.ArrayList;

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
		StringBuilder sb = new StringBuilder();
		sb.append(url + "<:>");
		sb.append(title + "<:>");
		sb.append(String.valueOf(TF) + "<:>");
		sb.append(location + "<:>");
		for (Hit hit : hitList) {
			int position = hit.getPosition();
			int type = hit.getType();
			if(type == 0){
				sb.append(hit.toString() + "<&>");
				StringBuilder subExcerpt = new StringBuilder();
				subExcerpt.append("...");
				int excerptLen = excerpt.size() - 1;
				
				int beginIndex = Math.max(0, position - 30);
				int endIndex = Math.min(excerptLen, position + 30);
				
				for (int i = beginIndex; i <= endIndex; i++) {
					subExcerpt.append(excerpt.get(i) + " ");
				}
				
				subExcerpt.append("...");
				sb.append(subExcerpt + "<;>");
			}else{
				sb.append(hit.toString() + "<;>");
			}
		}
		sb.delete(sb.length() - 3, sb.length());
		return sb.toString();
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
