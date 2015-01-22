package edu.upenn.cis455.client.post;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

public class UrlEncodedBody implements PostBody {
	
	private InputStream content = null;
	private long length = 0;

	public UrlEncodedBody(Map<String, String> params) {
		byte[] bytes = getUrlEncodedPostBody(params).getBytes();
		length = bytes.length;
		content = new ByteArrayInputStream(bytes);
	}

	@Override
	public InputStream getContent() {
		return content;
	}

	@Override
	public long getContentLength() {
		return length;
	}

	@Override
	public String getContentType() {
		return "application/x-www-form-urlencoded";
	}

	private String getUrlEncodedPostBody(Map<String, String> params) {
		if(params != null) {
			StringBuilder sb = new StringBuilder();
			for(Entry<String, String> entry : params.entrySet()) {
				String name = urlEncode(entry.getKey());
				String value = urlEncode(entry.getValue());
				sb.append(name).append('=')
				.append(value).append('&');
			}
			return sb.substring(0, sb.length() - 1);
		} else {
			return null;
		}
	}
	
	private static String urlEncode(String s) {
		if(s == null) {
			return null;
		}
		try {
			return URLEncoder.encode(s, "UTF-8")
			.replace("%2F", "/").replace("%5C", "\\").replace("+", " ");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
}
