package edu.upenn.cis455.storage.data;


public class Channel {

	private final String name;
	private String[] xpaths;
	private String xslUrl;
	private String username;
	
	public Channel(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public String[] getXpaths() {
		return xpaths;
	}
	public void setXpaths(String[] xpaths) {
		this.xpaths = xpaths;
	}
	public String getXslUrl() {
		return xslUrl;
	}
	public void setXslUrl(String xslUrl) {
		this.xslUrl = xslUrl;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
}
