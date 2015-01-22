package edu.upenn.cis455.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class HttpResponseImpl implements HttpResponse {
	
	private DateFormat[] mDateFormats = new DateFormat[] {
		new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US),
		new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss z", Locale.US),
		new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US)
	};

	private int statusCode = 200;
	private Parameters headers = new Parameters();
	private InputStream content = null;
	private Socket socket = null;
	private boolean closed = false;
	
	@Override
	public int getStatusCode() {
		return statusCode;
	}
	
	@Override
	public InputStream getContent() {
		if(closed) {
			throw new IllegalStateException("Response has been closed");
		}
		return content;
	}
	
	@Override
	public Enumeration<String> getHeaderNames() {
		return headers.getParameterNames();
	}

	@Override
	public Map<String, String> getHeaderMap() {
		Map<String, String> map = new HashMap<String, String>();
		for(Entry<String, String[]> entry : headers.getParameterMap().entrySet()) {
			map.put(entry.getKey(), entry.getValue()[0]);
		}
		return map;
	}

	@Override
	public String getHeader(String name) {
		return headers.getParameter(name);
	}

	@Override
	public long getDateHeader(String name) {
		String dateStr = headers.getParameter(name);
		if(dateStr == null) {
			return -1;
		}
		Date date = parseDate(dateStr);
		if(date == null) {
			throw new IllegalArgumentException("Header " + name + ": " +
					dateStr + " cannot be parsed into date");
		}
		return date.getTime();
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public void setContent(InputStream content) {
		this.content = content;
	}
	
	public void addHeader(String name, String value) {
		headers.addParameter(name, value);
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void close() throws IOException {
		closed = true;
		if(socket != null) {
			socket.close();
		}
	}
	
	private Date parseDate(String str) {
		if(str == null) {
			throw new NullPointerException("Date string cannot be null");
		}
		Date date = null;
		for(DateFormat format : mDateFormats) {
			date = tryParseDate(str, format);
			if(date != null) {
				break;
			}
		}
		return date;
	}
	
	private Date tryParseDate(String str, DateFormat format) {
		try {
			return format.parse(str);
		} catch (ParseException e) {
		}
		return null;
	}
}
