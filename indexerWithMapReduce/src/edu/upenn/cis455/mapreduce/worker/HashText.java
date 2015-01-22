package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashText {
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

	public static int whichOneThread(String input, String num) {
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

	public static int whichOneWorker(String input, String numWorkers) {
		BigInteger toDec = null;
		try {
			toDec = new BigInteger(sha1(input), 16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		BigInteger whichOne = toDec.mod(new BigInteger(numWorkers));
		int i = whichOne.intValue();
		return i;
	}

	public static void ReadAllFilesInFolder(final File folder,
			TaskQueue mapTaskQueue) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				ReadAllFilesInFolder(fileEntry, mapTaskQueue);
			} else {
				//System.out.println("InputDir: " + fileEntry.getAbsolutePath());
				if(fileEntry.getName().startsWith(".")){
					//System.out.println("getName(): " + fileEntry.getName());
					continue;
				}
//				FileInputStream fis = null;
//				 
//				try {
//					fis = new FileInputStream(fileEntry);
//					BufferedReader in = new BufferedReader(
//							   new InputStreamReader(
//									   fis, "UTF8"));
//					String content;
//					while ((content = in.readLine()) != null) {
//						// convert to char and display it
//						System.out.println(content);
//					}
//		 
//				} catch (IOException e) {
//					e.printStackTrace();
//				} finally {
//					try {
//						if (fis != null)
//							fis.close();
//					} catch (IOException ex) {
//						ex.printStackTrace();
//					}
//				}
				BufferedReader br = null;
				try {
					String sCurrentLine;
					br = new BufferedReader(new FileReader(
							fileEntry.getAbsolutePath()));
					while ((sCurrentLine = br.readLine()) != null) {
						//System.out.println(sCurrentLine);
						mapTaskQueue.addLine(sCurrentLine);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					try {
						if (br != null)
							br.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	
	
	
	// public static void main(String[] args) throws NoSuchAlgorithmException {
	// System.out.println(whichOneThread("dafdfa", "5"));
	// System.out.println(whichOneWorker("dafdfa", "5"));
	// }
}
