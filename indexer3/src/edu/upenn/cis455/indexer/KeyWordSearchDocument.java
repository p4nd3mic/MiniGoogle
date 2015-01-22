package edu.upenn.cis455.indexer;

import java.util.HashSet;

import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;
import edu.upenn.cis455.utility.Stemmer;


public class KeyWordSearchDocument {
	String original;
	String keyword;
	DatabaseWrapper wrapper =  DBSingleton.getInstance().getWrapper();
	boolean isLocation;
	
	
	public KeyWordSearchDocument() {
	}
	
	public KeyWordSearchDocument(String original, String keyword) {
		this.original = original;
		this.keyword = keyword;
		this.isLocation = false;
	}
	
	public KeyWordSearchDocument(String keyword, boolean isLocation) {
		this.keyword = keyword;
		this.isLocation = isLocation;
	}
	
	
	public String getResultSet(){
		if(keyword == null || !wrapper.isExistWordEntity(keyword)){
			return null;
		}
		StringBuilder result = new StringBuilder();
		result.append(wrapper.getAllInfo(original, keyword));
		return result.toString();
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
