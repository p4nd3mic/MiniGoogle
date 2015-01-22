package edu.upenn.cis455.client.post;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DataBody implements PostBody {

	private ByteArrayInputStream content;
	private long contentLength;
	private String contentType = null;
	
	public DataBody(byte[] data) {
		content = new ByteArrayInputStream(data);
		contentLength = data.length;
	}

	@Override
	public InputStream getContent() {
		return content;
	}

	@Override
	public long getContentLength() {
		return contentLength;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
