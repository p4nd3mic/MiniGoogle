package edu.upenn.cis455.indexer;

import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;
import edu.upenn.cis455.util.StringUtil;

public class KeyWordSearchDocument {
	String original;
	String keyword;
	DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();

	public KeyWordSearchDocument() {
	}

	public KeyWordSearchDocument(String original) {
		this.original = original.toLowerCase();
		this.keyword = StringUtil.stem(original);
	}

	public String getResultSet() {
		if (keyword == null || !wrapper.isExistWordEntity(keyword)) {
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
