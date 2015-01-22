package edu.upenn.cis455.db.data;


public class DocInfo {
	
	public static final String DEFAULT_CHARSET = "DEFAULT";
	
	private final String url;
	private String type;
	private String charsetName = DEFAULT_CHARSET;
	private long lastCheckDate;
	
	public DocInfo(String url) {
		this.url = url;
	}
	public String getUrl() {
		return url;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCharsetName() {
		return charsetName;
	}
	public void setCharsetName(String charsetName) {
		if(charsetName != null) {
			this.charsetName = charsetName;
		}
	}
	public long getLastCheckDate() {
		return lastCheckDate;
	}
	public void setLastCheckDate(long lashCheckDate) {
		this.lastCheckDate = lashCheckDate;
	}
}
