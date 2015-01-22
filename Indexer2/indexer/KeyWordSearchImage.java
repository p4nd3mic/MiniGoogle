package edu.upenn.cis455.indexer;

import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;


public class KeyWordSearchImage {
	String original;
	String keyword;
	DatabaseWrapper wrapper =  DBSingleton.getInstance().getWrapper();

	public KeyWordSearchImage() {
	}
	
	public KeyWordSearchImage(String original, String keyword) {
		this.original = original;
		this.keyword = keyword;
	}
	
	public String getResultSet(){
		if(keyword == null || !wrapper.isExistWordEntityForImage(keyword)){
			return null;
		}
		StringBuilder result = new StringBuilder();
		result.append(wrapper.getAllInfoForImage(original, keyword));
		return result.toString();
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
