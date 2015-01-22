package edu.upenn.cis455.indexer;

import java.util.HashSet;


// TODO: Maybe need to change into Singleton(db)?
public class KeyWordSearch {
	String keyword;
	DatabaseWrapper wrapper =  DBSingleton.getInstance().getWrapper();
	boolean isLocation;
	
	
	public KeyWordSearch() {
	}
	
	public KeyWordSearch(String keyword) {
		this.keyword = keyword;
		this.isLocation = false;
	}
	
	public KeyWordSearch(String keyword, boolean isLocation) {
		this.keyword = keyword;
		this.isLocation = isLocation;
	}
	
	
	public String getResultSet(){
		if(keyword == null || !wrapper.isExistWordEntity(keyword)){
			return null;
		}
		StringBuilder result = new StringBuilder();
		if(isLocation){
			result.append("<L>");
		}
		result.append(wrapper.getAllInfo(keyword));
		return result.toString();
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	public static void main(String[] args) {
		DBSingleton.setDbPath("/home/cloudera/Desktop/DB");
		
		
		
		String keyword = "engine";
		
		Stemmer stemmer = null;
		char chs[] = keyword.toCharArray();
		stemmer = new Stemmer();
		stemmer.add(chs, chs.length);
		stemmer.stem();

		String modifiedWord = stemmer.toString();

		System.out.println("Original word: " + keyword + " : " + "Modified word: "
				+ modifiedWord);

		HashSet<String> candidates = new HashSet<String>();
		candidates.add(keyword);
		candidates.add(modifiedWord);
		
		for (String candidateWord : candidates) {
			KeyWordSearch search = new KeyWordSearch(candidateWord);
			String result = search.getResultSet();
			if(result != null){
				System.out.println(result);
			}
		}
	}
}
