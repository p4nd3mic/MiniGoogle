package edu.upenn.cis455.webserver.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

public class WrappedServletInputStream extends ServletInputStream {

	private InputStream input;
	
	public WrappedServletInputStream(InputStream input) {
		this.input = input;
	}

	@Override
	public int read() throws IOException {
		return input.read();
	}

}
