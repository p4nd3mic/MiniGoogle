package edu.upenn.cis455.client;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Parameters {

	private Map<String, String[]> params = new HashMap<String, String[]>();
	
	public String[] getParameterValues(String name) {
		Set<Entry<String, String[]>> entries = params.entrySet();
		for(Entry<String, String[]> entry : entries) {
			if(entry.getKey().equalsIgnoreCase(name)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	public String getParameter(String name) {
		String value = null;
		String[] values = getParameterValues(name);
		if(values != null) {
			value = values[0];
		}
		return value;
	}
	
	public Map<String, String[]> getParameterMap() {
		return params;
	}
	
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(params.keySet());
	}
	
	public void addParameter(String name, String[] values) {
		String[] newValues;
		if(params.containsKey(name)) {
			String[] oldValues = getParameterValues(name);
			newValues = new String[oldValues.length + values.length];
			for(int i = 0; i < oldValues.length; i++) {
				newValues[i] = oldValues[i];
			}
			for(int i = 0; i < values.length; i++) {
				newValues[oldValues.length + i] = values[i];
			}
		} else {
			newValues = values;
		}
		params.put(name, newValues);
	}
	
	public void addParameter(String name, String value) {
		String[] newValues;
		if(params.containsKey(name)) {
			String[] oldValues = getParameterValues(name);
			newValues = new String[oldValues.length + 1];
			for(int i = 0; i < oldValues.length; i++) {
				newValues[i] = oldValues[i];
			}
			newValues[oldValues.length] = value;
		} else {
			newValues = new String[1];
			newValues[0] = value;
		}
		params.put(name, newValues);
	}
	
	public void setParameter(String name, String[] values) {
		params.put(name, values);
	}
	
	public void setParameter(String name, String value) {
		String[] values = new String[1];
		values[0] = value;
		params.put(name, values);
	}
	
	public boolean hasName(String name) {
		return params.containsKey(name);
	}
	
	public void handleQueryString(String query) {
		if(query == null) {
			return;
		}
		String[] pairs = query.split("&");
		for(String pair : pairs) {
			int index = pair.indexOf('=');
			if(index == -1) {
				continue;
			}
			String name = pair.substring(0, index);
			String value = pair.substring(index + 1);
			addParameter(name, value);
		}
	}
	
	public void clear() {
		params.clear();
	}
}
