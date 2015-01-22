package edu.upenn.cis455.storage.data;


public class DocInfo {

//	public static final String TYPE_XML = "xml";
//	public static final String TYPE_HTML = "html";
	
	public static final String DEFAULT_CHARSET = "DEFAULT";
	
	private final String url;
	private String type;
	private String charsetName = DEFAULT_CHARSET;
	private long lastCheckDate;
//	private byte[] content = null;
	
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
//	public byte[] getContent() {
//		return content;
//	}
//	public void setContent(byte[] content) {
//		this.content = content;
//	}
	public String getCharsetName() {
		return charsetName;
	}
	public void setCharsetName(String charsetName) {
		if(charsetName != null) {
			this.charsetName = charsetName;
		}
	}
//	public String getContentString() {
//		String str;
//		if(DEFAULT_CHARSET.equals(charsetName)) {
//			str = new String();
//		} else {
//			str = new String(charsetName);
//		}
//		return str;
//	}
	public long getLastCheckDate() {
		return lastCheckDate;
	}
	public void setLastCheckDate(long lashCheckDate) {
		this.lastCheckDate = lashCheckDate;
	}
}
