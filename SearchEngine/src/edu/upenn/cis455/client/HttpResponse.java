package edu.upenn.cis455.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;

public interface HttpResponse {

	int getStatusCode();
	
	InputStream getContent();
	
	Enumeration<String> getHeaderNames();
	
	Map<String, String> getHeaderMap();
	
	String getHeader(String name);
	
	long getDateHeader(String name);
	
	void close() throws IOException;
}
