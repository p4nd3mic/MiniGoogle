package edu.upenn.cis455.webserver.servlet;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class BufferdOutputStream extends ServletOutputStream {

	public static final String TAG = BufferdOutputStream.class.getSimpleName();
	
	private boolean mError = false;
	private OutputBuffer mBuffer;
	public BufferdOutputStream(OutputBuffer buffer) {
		this.mBuffer = buffer;
	}
	
	@Override
	public void flush() throws IOException {
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
	public void close() throws IOException {
		try {
			mBuffer.close();
		} catch (IOException e) {
		}
		mError = false;
	}


	@Override
	public void write(int b) throws IOException {
		if(mError) {
			return;
		}
		mBuffer.write(b);
	}
}
