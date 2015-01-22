package edu.upenn.cis455.webserver.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.upenn.cis455.webserver.exception.BadRequestException;
import edu.upenn.cis455.webserver.servlet.Request;

public class RequestReader {

	public static final String TAG = RequestReader.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
//	private static final int BUFFER_SIZE = 8192;
//	private static final char CR = '\r';
//	private static final char LF = '\n';
	private static final char SP = 32;
	private static final char HT = 9;
	
	private Request mRequest;
	private InputStream mInputStream = null;
	
	private boolean mDebug = false;
	
	public RequestReader(Request request) {
		this.mRequest = request;
	}
	
	public void setDebug(boolean debug) {
		this.mDebug = debug;
	}

	public void setInputStream(InputStream inputStream) {
		this.mInputStream = inputStream;
		mRequest.setStream(mInputStream);
	}
	
	public void readRequestLine() throws IOException, BadRequestException {
		String line = null;
		boolean blank = true;
		while(blank) {
			line = readLine();
			if(line == null) {
				return;
			}
			if(!line.isEmpty()) {
				blank = false;
				parseRequestLine(line);
			}
		}
	}
	
	public void readHeaders() throws IOException {
		List<String> lines = new ArrayList<String>();
		String line;
		while((line = readLine()) != null) {
			if(line.isEmpty()) {
				break;
			}
			lines.add(line);
		}
		
		List<String> headers = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		for(String l : lines) {
			char first = l.charAt(0);
			if(first == SP || first == HT) { // multi-line header
				l = " " + l.trim();
			} else {
				if(sb.length() > 0) {
					headers.add(sb.toString());
					sb = new StringBuilder();
				}
			}
			sb.append(l);
		}
		if(sb.length() > 0) {
			headers.add(sb.toString());
		}
		
		for(String header : headers) {
			parseHeader(header);
		}
	}
	
	public void nextRequest() {
		mRequest.recycle();
	}
	
	private String readLine() throws IOException {
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
			return builder.toString().replace("\r", "").replace("\n", "");
		} else {
			return null;
		}
	}

	private void parseRequestLine(String data) throws BadRequestException {
		if(data == null) {
			return;
		}
		data = data.trim();
		if(mDebug) {
			logger.debug("Received request: " + data);
		}
		
		int space1 = data.indexOf(' ');
		if(space1 != -1) {
			String method = data.substring(0, space1);
			mRequest.setMethod(method);
		} else {
			throw new BadRequestException();
		}
		
		int space2 = data.lastIndexOf(' ');
		if(space2 != -1) {
			String protocol = data.substring(space2 + 1);
			mRequest.setProtocol(protocol);
		} else {
			throw new BadRequestException();
		}
		
		if(space1 < space2) {
			String path = data.substring(space1 + 1, space2);
			processPath(path);
		} else {
			throw new BadRequestException();
		}
		
		// Check case
		if(!checkUpperCase(mRequest.getMethod())
		|| !checkUpperCase(mRequest.getProtocol())) {
			throw new BadRequestException();
		}
	}
	
	private void processPath(String path) {
		// Check absolute path
		int index = path.indexOf("://");
		if(index != -1) {
			path = path.substring(index + 3);
			int firstSlash = path.indexOf('/');
			if(firstSlash != -1) {
				path = path.substring(firstSlash);
			}
		}
		
		// Split uri and query string
		String uri, query;
		index = path.indexOf('?');
		if(index != -1) { // With query
			uri = path.substring(0, index);
			query = path.substring(index + 1);
			mRequest.setQueryString(query);
		} else {
			uri = path;
		}
		mRequest.setRequestUri(uri);
		
		// Check session id
		final String sessionTag = ";jsessionid=";
		int idIndex = uri.indexOf(sessionTag);
		if(idIndex != -1) {	// Session id on url
			String id = uri.substring(idIndex + sessionTag.length());
			mRequest.setRequestedSessionId(id);
			mRequest.setSessionFromURL(true);
		}
	}
	
	private void parseHeader(String header) {
		int index = header.indexOf(':');
		if(index == -1) {
			return;
		}
		String name = header.substring(0, index).trim();
		String value = header.substring(index + 1).trim();
		String[] values = new String[1];
		values[0] = value;
		mRequest.addHeader(name, values);
	}
	
	private boolean checkUpperCase(String data) {
		return data != null && data.equals(data.toUpperCase());
	}
}
