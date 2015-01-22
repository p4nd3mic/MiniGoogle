package edu.upenn.cis455.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digest {
	
	public static final String TAG = Digest.class.getSimpleName();
	
	public static String md5(String input) {
		return md5(input.getBytes());
	}

	public static String md5(byte[] input) {
		if (input == null) {
			return null;
		}
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
		}
		md5.update(input);
		return byteArrayToHexString(md5.digest());
	}
	
	public static String sha1(String input) {
		return sha1(input.getBytes());
	}
	
	public static String sha1(byte[] input) {
		if (input == null) {
			return null;
		}
		MessageDigest sha1 = null;
		try {
			sha1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) { // Not possible
		}
		return byteArrayToHexString(sha1.digest(input));
	}
	
	private static String byteArrayToHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(Integer.toString(b >>> 4 & 0xF, 16)).append(
					Integer.toString(b & 0xF, 16));
		}
		return sb.toString();
	}
}
