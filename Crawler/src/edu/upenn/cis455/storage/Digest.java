package edu.upenn.cis455.storage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digest {
	
	private static MessageDigest md5;
	private static MessageDigest sha1;
	
	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
			sha1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
		}
	}
	
	public static String md5(String input) {
		return md5(input.getBytes());
	}

	public static String md5(byte[] input) {
		if (input == null) {
			return null;
		}
		String result = null;
		try {
			md5.update(input);
			result = byteArrayToHexString(md5.digest());
		} catch (Exception e) {
		}
		return result;
	}
	
	public static String sha1(String input) {
		return sha1(input.getBytes());
	}
	
	public static String sha1(byte[] input) {
		if (input == null) {
			return null;
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
