package edu.upenn.cis455.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileStorage {
	
	public static final String TAG = FileStorage.class.getSimpleName();
	
	private static final int DEFAULT_BUFFER_SIZE = 8192;
	
	public static void save(File file, byte[] content) throws IOException {
		FileOutputStream fos = null;
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			fos.write(content);
		} catch (FileNotFoundException e) {
		} finally {
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static byte[] get(File file) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			int len;
			while((len = fis.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
		} catch (FileNotFoundException e) {
		} finally {
			if(fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		return baos.toByteArray();
	}
}
