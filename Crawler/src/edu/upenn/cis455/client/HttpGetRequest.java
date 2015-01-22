package edu.upenn.cis455.client;

public class HttpGetRequest extends HttpRequest {
	
	public HttpGetRequest(HttpUrl url) {
		super("GET", url);
	}
	
	public HttpGetRequest(String url) {
		super("GET", url);
	}

}
