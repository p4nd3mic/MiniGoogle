package edu.upenn.cis455.webserver.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Headers {

	private List<Header> mHeaders = new ArrayList<Header>();
	
	public List<Header> getList() {
		return mHeaders;
	}
	
	public String getHeader(String name) {
		for(Header h : mHeaders) {
			if(h.getName().equalsIgnoreCase(name)) {
				return h.getValue();
			}
		}
		return null;
	}
	
	public String[] getHeaderValues(String name) {
		for(Header h : mHeaders) {
			if(h.getName().equalsIgnoreCase(name)) {
				return h.getValues();
			}
		}
		return null;
	}
	
	public void setHeader(String name, String value) {
		Iterator<Header> iter = mHeaders.iterator();
		while(iter.hasNext()) {
			Header header = iter.next();
			if(header.getName().equalsIgnoreCase(name)) {
				iter.remove();
			}
		}
		addHeader(name, value);
	}
	
	public void addHeader(String name, String value) {
		Header header = new Header();
		header.setName(name);
		header.setValue(value);
		mHeaders.add(header);
	}
	
	public void appendHeader(String name, String value) {
		Header header = null;
		for(Header h : mHeaders) {
			if(h.getName().equalsIgnoreCase(name)) {
				header = h;
			}
		}
		if(header != null) {
			header.appendValue(value);
		} else {
			addHeader(name, value);
		}
	}
	
	public void clear() {
		mHeaders.clear();
	}
	
	public boolean hasHeader(String name) {
		for(Header h : mHeaders) {
			if(h.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	public static class Header {
		
		private String name;
		private String[] values;
		
		public Header(String name, String[] values) {
			this.name = name;
			this.values = values;
		}

		public Header() {
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String[] getValues() {
			return values;
		}

		public void setValues(String[] values) {
			this.values = values;
		}
		
		public String getValue() {
			if(values.length > 0) {
				return values[0];
			}
			return null;
		}
		
		public void setValue(String value) {
			String[] newValue = new String[1];
			newValue[0] = value;
			this.values = newValue;
		}
		
		public void appendValue(String value) {
			String[] oldValues = this.values;
			String[] newValues = new String[values.length + 1];
			for(int i = 0; i < oldValues.length; i++) {
				newValues[i] = oldValues[i];
			}
			newValues[oldValues.length] = value;
			this.values = newValues;
		}
	}
}
