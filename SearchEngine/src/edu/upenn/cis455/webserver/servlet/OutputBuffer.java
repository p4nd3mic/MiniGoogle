package edu.upenn.cis455.webserver.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;

import edu.upenn.cis455.webserver.Const;

public class OutputBuffer extends Writer {

	private static final byte[] CRLF_BYTES = "\r\n".getBytes();
	private static final byte[] CHUNK_END = "0\r\n\r\n".getBytes();
	
	private boolean mClosed = false;
	private boolean mInitial = true;
	private Response mResponse = null;
	private ByteArrayOutputStream mBuffer = new ByteArrayOutputStream();
	private ByteArrayOutputStream mSocketBuffer = null;
	
	public OutputBuffer(ByteArrayOutputStream socketBuffer) {
		this.mSocketBuffer = socketBuffer;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		char[] carray = new char[len - off];
		System.arraycopy(cbuf, off, carray, 0, len);
		String str = new String(carray);
		String encoding = mResponse.getCharacterEncoding();
		byte[] bytes;
		if(encoding != null) {
			bytes = str.getBytes(encoding);
		} else {
			bytes = str.getBytes();
		}
		mBuffer.write(bytes);
	}

	public void write(byte[] bytes) throws IOException {
		mBuffer.write(bytes);
	}

	@Override
	public void write(int c) throws IOException {
		mBuffer.write(c);
	}

	@Override
	public void flush() throws IOException {
		doFlush();
	}

	@Override
	public void close() throws IOException {
		if(mClosed) {
			return;
		}
		if(!mResponse.isCommitted() && mResponse.getContentLength() == -1) {
			mResponse.setContentLength(mBuffer.size());
		}
		
		doFlush();
		mClosed = true;
	}

	public Response getResponse() {
		return mResponse;
	}

	public void setResponse(Response response) {
		this.mResponse = response;
	}
	
	public void setSocketBuffer(ByteArrayOutputStream socketBuffer) {
		this.mSocketBuffer = socketBuffer;
	}

	public void recycle() {
		mInitial = true;
		mClosed = false;
		mBuffer.reset();
	}
	
	private void doFlush() throws IOException {
		if(mInitial) {
			mResponse.sendHeaders();
			mInitial = false;
		}
		if(mBuffer.size() > 0 && mSocketBuffer != null) {
			String transfer = mResponse.getHeaders().getHeader(Const.HEADER_TRANSFER_ENCODING);
			if("chunked".equalsIgnoreCase(transfer)) { // Chunked tranfering
				writeChunkedData();
			} else {
				mBuffer.writeTo(mSocketBuffer);
				mBuffer.reset();
			}
		}
	}
	
	private void writeChunkedData() throws IOException {
		while(mBuffer.size() > 0) {
			byte[] data = mBuffer.toByteArray();
			mBuffer.reset();
			String length = Integer.toHexString(data.length);
			mSocketBuffer.write(length.getBytes());
			mSocketBuffer.write(CRLF_BYTES);
			mSocketBuffer.write(data);
			mSocketBuffer.write(CRLF_BYTES);
		}
		mSocketBuffer.write(CHUNK_END);
	}
}
