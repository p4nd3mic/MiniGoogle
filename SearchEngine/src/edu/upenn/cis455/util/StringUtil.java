package edu.upenn.cis455.util;

import org.apache.log4j.Logger;

import edu.upenn.cis455.utility.Stemmer;

public class StringUtil {
	
	public static final String TAG = StringUtil.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

	public static int parseInt(String s, int defValue) {
		int value = defValue;
		if (s != null) {
			try {
				value = Integer.parseInt(s);
			} catch (NumberFormatException e) {
//				logger.error("Content cannot be parsed to integer: " + s);
			}
		}
		return value;
	}
	
	public static double parseDouble(String s, double defValue) {
		double value = defValue;
		if(s != null) {
			try {
				value = Double.parseDouble(s);
			} catch (NumberFormatException e) {
//				logger.error("Content cannot be parsed to double: " + s, e);
			}
		}
		return value;
	}

	public static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	public static String getExtension(String fileName) {
		String ext = null;
		if (fileName != null) {
			int dot = fileName.lastIndexOf('.');
			if (dot >= 0) {
				ext = fileName.substring(dot + 1);
			}
		}
		return ext;
	}

	public static String stem(String s) {
		Stemmer stemmer = null;
		char chs[] = s.toCharArray();
		stemmer = new Stemmer();
		stemmer.add(chs, chs.length);
		stemmer.stem();
		return stemmer.toString();
	}
}
