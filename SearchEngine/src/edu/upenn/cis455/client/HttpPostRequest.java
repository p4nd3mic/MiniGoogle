package edu.upenn.cis455.client;

import edu.upenn.cis455.client.post.PostBody;


public class HttpPostRequest extends HttpRequest {
	
	private PostBody postBody;
	
	public HttpPostRequest(HttpUrl url) {
		super("POST", url);
	}
	
	public HttpPostRequest(String url) {
		super("POST", url);
	}

	public PostBody getPostBody() {
		return postBody;
	}

	public void setPostBody(PostBody postBody) {
		this.postBody = postBody;
	}
}
