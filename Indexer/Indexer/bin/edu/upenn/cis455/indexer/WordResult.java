package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.TreeSet;

public class WordResult {
	private String content;
	private double IDF;
	
	private ArrayList<SingleResult> singleResultList;
	
	private ArrayList<SingleResult> filterResultList;
	
	public WordResult(String content, double IDF, ArrayList<SingleResult> singleResultList){
		this.content = content;
		this.IDF = IDF;
		this.singleResultList = new ArrayList<SingleResult>(singleResultList);
	}
	
	public void filter(){
		if(singleResultList.size() <= 100){
			TreeSet<SingleResult> treeTemp = new TreeSet<SingleResult>(singleResultList);
			filterResultList = new ArrayList<SingleResult>(treeTemp);
		}else{
			TreeSet<SingleResult> treeTemp = new TreeSet<SingleResult>(singleResultList);
			int number = 0;
			for (SingleResult singleResult : treeTemp) {
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
		StringBuilder sb = new StringBuilder();
		sb.append(content + "<|>");
		sb.append(String.valueOf(IDF) + "<|>");
		for (SingleResult one : filterResultList) {
			sb.append(one.toString() + "<*>");
		}
		sb.delete(sb.length() - 3, sb.length());
		return sb.toString();
	}
}
