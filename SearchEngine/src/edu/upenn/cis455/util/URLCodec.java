package edu.upenn.cis455.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class URLCodec {

	public static final String TAG = URLCodec.class.getSimpleName();
	
	public static String encode(String s) {
		if(s == null) {
			return null;
		}
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
	
	public static String decode(String s) {
		if(s == null) {
			return null;
		}
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
}
