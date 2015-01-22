package edu.upenn.cis455.webserver.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import edu.upenn.cis455.webserver.Const;
import edu.upenn.cis455.webserver.servlet.Response;

public class ResponseSender {

	public static final String TAG = ResponseSender.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private static final String HTTP11_BYTES = "HTTP/1.1";
	private static final byte[] RESPONSE_CONTINUE_BYTES = "HTTP/1.1 100 Continue\r\n\r\n".getBytes();
	
	private Response mResponse;
	private StringBuilder mHeaderBuffer;
	private ByteArrayOutputStream mBuffer = null;
	private OutputStream mOutputStream = null;
	
	private boolean mCommitted = false;

	public ResponseSender(Response response) {
		this.mResponse = response;
		mBuffer = new ByteArrayOutputStream();
		mHeaderBuffer = new StringBuilder();
		mResponse.setSocketBuffer(mBuffer);
	}

	public void setOutputStream(OutputStream outputStream) {
		this.mOutputStream = outputStream;
	}
	
	public void nextRequest() {
		mResponse.recycle();
		mBuffer.reset();
		mHeaderBuffer = new StringBuilder();
	}
	
	public void writeStatus() {
//		String protocol = mResponse.getProtocol();
		mHeaderBuffer.append(HTTP11_BYTES);
		mHeaderBuffer.append(' ');
		
		int status = mResponse.getStatus();
		mHeaderBuffer.append(status);
		mHeaderBuffer.append(' ');
		
		String message = mResponse.getMessage();
		if(message != null) {
			mHeaderBuffer.append(message);
		} else {
			mHeaderBuffer.append(Const.getStatusMessage(status));
		}
		mHeaderBuffer.append(Const.CRLF);
	}
	
	public void writeHeader(String name, String[] values) {
		mHeaderBuffer.append(name);
		mHeaderBuffer.append(": ");
		for(int i = 0; i < values.length; i++) {
			String value = values[i];
			mHeaderBuffer.append(value);
			if(i != values.length - 1) {
				mHeaderBuffer.append(", ");
			}
		}
		mHeaderBuffer.append(Const.CRLF);
	}
	
	public void endHeaders() {
		mHeaderBuffer.append(Const.CRLF);
	}
	
	public void commit() throws IOException {
		mResponse.setCommitted(true);
		mCommitted = true;
		
		byte[] headerByte = mHeaderBuffer.toString().getBytes();
		mBuffer.write(headerByte);
	}

	public void finish() throws IOException {
		if(!mCommitted) {
			ActionListener listener = mResponse.getActionListener();
			if(listener != null) {
				listener.commit();
			}
		}
		
		if(mOutputStream != null) {
			mBuffer.writeTo(mOutputStream);
		}
	}
	
	public void sendContinue() throws IOException {
		mOutputStream.write(RESPONSE_CONTINUE_BYTES);
	}
}
