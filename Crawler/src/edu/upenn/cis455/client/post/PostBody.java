package edu.upenn.cis455.client.post;

import java.io.InputStream;


public interface PostBody {

	InputStream getContent();
	
	long getContentLength();
	
	String getContentType();
}
