package edu.upenn.cis455.webserver.servlet;

import java.io.IOException;
import java.io.InputStream;

public class LineReader {

	public static final String TAG = LineReader.class.getSimpleName();
	
	private InputStream mInputStream;
	
	public LineReader(InputStream input) {
		this.mInputStream = input;
	}
	
	public String readLine() throws IOException {
		StringBuilder builder = new StringBuilder();
		int n;
		while((n = mInputStream.read()) != -1) {
			char c = (char) n;
			builder.append(c);
			if('\n' == c) {
				break;
			}
		}
		if(builder.length() > 0) {
			return builder.toString().trim();
		} else {
			return null;
		}
	}
}
