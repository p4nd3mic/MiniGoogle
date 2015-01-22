package edu.upenn.cis455.storage;

public interface HtmlCache {
	
	byte[] getHtml(String url);
	
	void saveHtml(String url, byte[] content);
}
