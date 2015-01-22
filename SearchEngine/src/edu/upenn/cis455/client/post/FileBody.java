package edu.upenn.cis455.client.post;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileBody implements PostBody {
	
	private InputStream content;
	private long length = 0;
	private String contentType = null;

	public FileBody(File file) throws FileNotFoundException {
		content = new FileInputStream(file);
		length = file.length();
	}
	
	@Override
	public InputStream getContent() {
		return content;
	}

	@Override
	public long getContentLength() {
		return length;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
