package edu.upenn.cis455.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LineReader {

	public static final String TAG = LineReader.class.getSimpleName();
	
	private InputStream mInputStream;
	private int mReadLength = 0;
	
	public LineReader() {
	}

	public LineReader(InputStream input) {
		this.mInputStream = input;
	}
	
	public InputStream getInputStream() {
		return mInputStream;
	}

	public void setInputStream(InputStream input) {
		mReadLength = 0;
		this.mInputStream = input;
	}
	
	public int getReadLength() {
		return mReadLength;
	}

	public String readLine() throws IOException {
		if(mInputStream == null) {
			throw new NullPointerException("Input stream is null");
		}
		StringBuilder builder = new StringBuilder();
		int n;
		while((n = mInputStream.read()) != -1) {
			mReadLength++;
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
	
	public byte[] readBytes() throws IOException {
		if(mInputStream == null) {
			throw new NullPointerException("Input stream is null");
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int n;
		while((n = mInputStream.read()) != -1) {
			mReadLength++;
			if('\r' == n) {
				break;
			}
			baos.write(n);
			
		}
		return baos.toByteArray();
	}
}
