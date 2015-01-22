package edu.upenn.cis455.mapreduce.worker;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashDivider {

	private int divisions = 1;
	private BigInteger range;
	private BigInteger dRange;
	
	public HashDivider() {
		final int len = 21;
		byte[] maxBytes = new byte[len];
		maxBytes[0] = 1;
		range = new BigInteger(maxBytes);
		dRange = range;
	}
	
	public void setIntervals(int nIntervals) {
		if(nIntervals >= 1) {
			divisions = nIntervals;
			dRange = range.divide(new BigInteger(String.valueOf(divisions)));
		}
	}
	
	public int indexOf(byte[] input) {
		int index = -1;
		byte[] digest = digest(input);
		if(digest != null) {
			index = intvIndex(digest);
		}
		return index;
	}
	
	private byte[] digest(byte[] input) {
		if(input == null) {
			return null;
		}
		MessageDigest sha1 = null;
		try {
			sha1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e1) {
			// Impossible
		}
		byte[] result = sha1.digest(input);
		return result;
	}
	
	private int intvIndex(byte[] input) {
		if(input == null) {
			return -1;
		}
		if(divisions == 1) {
			return 0;
		}
		BigInteger inputBi = new BigInteger(1, input);
		BigInteger quotient = inputBi.divide(dRange);
		int result = Integer.parseInt(quotient.toString());
		return result;
	}
}