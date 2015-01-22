package edu.upenn.cis455.client;

import java.util.Enumeration;

public class HttpRequest {

	private final String method;
	private HttpUrl url;
	private Parameters headers = new Parameters();
	
	public HttpRequest(String method, String url) {
		this(method, HttpUrl.parseUrl(url));
	}

	public HttpRequest(String method, HttpUrl url) {
		this.method = method;
		this.url = url;
	}
	
	public void addHeader(String name, String value) {
		headers.setParameter(name, value);
	}
	
	public String getHeader(String name) {
		return headers.getParameter(name);
	}
	
	public boolean hasHeader(String name) {
		return headers.hasName(name);
	}
	
	public Enumeration<String> getHeaderNames() {
		return headers.getParameterNames();
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getUrl() {
		return url.getUrl();
	}

	public String getScheme() {
		return url.getScheme();
	}

	public String getHost() {
		return url.getHost();
	}

	public int getPort() {
		return url.getPort();
	}

	public String getPath() {
		return url.getPath();
	}
}
