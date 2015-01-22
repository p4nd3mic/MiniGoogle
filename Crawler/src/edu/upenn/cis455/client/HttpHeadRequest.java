package edu.upenn.cis455.client;

public class HttpHeadRequest extends HttpRequest {

	public HttpHeadRequest(HttpUrl url) {
		super("HEAD", url);
	}
	
	public HttpHeadRequest(String url) {
		super("HEAD", url);
	}

}
