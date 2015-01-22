package edu.upenn.cis455.indexer;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashHelper {
	public static String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return sb.toString();
	}

	public static int generateDocId(String input) {
		BigInteger toDec = null;
		String maxInteger = String.valueOf(Integer.MAX_VALUE);
		try {
			toDec = new BigInteger(sha1(input), 16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		BigInteger whichOne = toDec.mod(new BigInteger(maxInteger));
		int i = whichOne.intValue();
		return i;
	}

	public static String md5(String input) throws NoSuchAlgorithmException {
		MessageDigest md = null;
		md = MessageDigest.getInstance("MD5");
		md.update(input.getBytes());
		byte byteData[] = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		// System.out.println("Digest(in hex format):: " + sb.toString());
		return sb.toString();
	}

	public static int whichOneBarrel(String input, String num) {
		BigInteger toDec = null;
		try {
			toDec = new BigInteger(md5(input), 16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		BigInteger whichOne = toDec.mod(new BigInteger(num));
		int i = whichOne.intValue();
		return i;
	}

	// Test
	public static void main(String[] args) throws NoSuchAlgorithmException {
		System.out.println(generateDocId("sequence"));
		System.out.println(generateDocId("university"));
		System.out.println(generateDocId("Berkeley"));
		System.out.println(generateDocId("BerkeleyDB"));
	}
}
