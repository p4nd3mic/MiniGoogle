package edu.upenn.cis455.webserver.servlet;

import java.io.IOException;
import java.io.PrintWriter;

public class BufferedWriter extends PrintWriter {

	private boolean mError = false;
	private OutputBuffer mBuffer;
	
	public BufferedWriter(OutputBuffer buffer) {
		super(buffer);
		mBuffer = buffer;
	}

	@Override
	public void flush() {
		if(mError) {
			return;
		}
		try {
			mBuffer.flush();
		} catch (IOException e) {
			mError = true;
		}
	}

	@Override
	public void close() {
		try {
			mBuffer.close();
		} catch (IOException e) {
		}
		mError = false;
	}

	@Override
	public void write(char[] buf, int off, int len) {
		if(mError) {
			return;
		}
		try {
			mBuffer.write(buf, off, len);
		} catch (IOException e) {
			mError = true;
		}
	}
}
