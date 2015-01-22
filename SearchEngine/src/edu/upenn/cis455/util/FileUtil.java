package edu.upenn.cis455.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class FileUtil {
	
	public static final String TAG = FileUtil.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	public static final int DEFAULT_BUFFER_SIZE = 8192;
	
	private static Map<String, String> mimeTypes = new HashMap<String, String>();
	
	static {
		mimeTypes.put("jpg", "image/jpeg");
		mimeTypes.put("gif", "image/gif");
		mimeTypes.put("png", "image/png");
		mimeTypes.put("txt", "text/plain");
		mimeTypes.put("html", "text/html");
		mimeTypes.put("htm", "text/html");
		mimeTypes.put("css", "text/css");
		mimeTypes.put("mp3", "audio/mpeg");
		mimeTypes.put("zip", "application/zip");
		mimeTypes.put("gz", "application/gzip");
		mimeTypes.put("exe", "application/exe");
		mimeTypes.put("pdf", "application/pdf");
//		mimeTypes.put("jsp", "application/jsp");
		mimeTypes.put("jsp", "text/html");
		mimeTypes.put("xml", "application/xml");
		mimeTypes.put("js", "application/javascript");
	}
	
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
		if(file == null || !file.exists()) {
			return null;
		}
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
	
	/**
	 * Merge with file1 and file2
	 * @param file1
	 * @param file2 After merging, file2 will be deleted
	 */
	public static void mergeFile(File file1, File file2) {
		if(file1 == null) {
			return;
		}
		if(file2 == null || !file2.exists()) {
			return;
		}
		if(!file1.exists()) {
			file2.renameTo(file1);
		} else {
			byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try {
				fis = new FileInputStream(file2);
				fos = new FileOutputStream(file1, true);
				int len;
				while((len = fis.read(buf)) != -1) {
					fos.write(buf, 0, len);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				closeObject(fis);
				closeObject(fos);
			}
			file2.delete();
		}
	}
	
	public static void mergeFiles(File dir, String mergedFileName) {
		File[] files = dir.listFiles();
		if(files == null || files.length == 0) {
			return;
		}
		FileInputStream fis = null;
		FileOutputStream fos = null;
		File firstFile = files[0];
		if(files.length > 1) {
			byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			try {
				fos = new FileOutputStream(firstFile, true);
				for(int i = 1; i < files.length; i++) {
					File file = files[i];
					try {
						fis = new FileInputStream(file);
						int len;
						while((len = fis.read(buf)) != -1) {
							fos.write(buf, 0, len);
						}
						fis.close();
						file.delete();
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
					}
				}
			} catch (FileNotFoundException e) {
			} finally {
				closeObject(fos);
			}
		}
		if(mergedFileName != null) {
			File newFile = new File(dir, mergedFileName);
			firstFile.renameTo(newFile);
		}
	}
	
	public static void deleteDir(File dir) {
		if(!dir.exists()) {
			return;
		}
		if(dir.isFile()) {
			dir.delete();
		} else if(dir.isDirectory()) {
			File[] files = dir.listFiles();
			for(File f : files) {
				deleteDir(f);
			}
			dir.delete();
		}
	}
	
	public static void clearDir(File dir) {
		deleteDir(dir);
		dir.mkdir();
	}
	
	public static String getMIMEType(String filename) {
		String type = null;
		int index = filename.lastIndexOf(".");
		String ext = null;
		if(index != -1 && index != 0 && index < filename.length()) {
			ext = filename.substring(index + 1);
		}
		if(ext != null) {
			Iterator<Entry<String, String>> iter = mimeTypes.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				if(entry.getKey().equalsIgnoreCase(ext)) {
					type = entry.getValue();
					break;
				}
			}
		}
		return type;
	}
	
	public static void copyFile(File src, File dest) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		if(!src.exists() || !src.isFile()) {
			return;
		}
		if(dest.exists() && !dest.isFile()) {
			return;
		}
		try {
			if(dest.exists()) {
				dest.delete();
			}
			dest.createNewFile();
			fis = new FileInputStream(src);
			fos = new FileOutputStream(dest);
			byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			int len;
			while((len = fis.read(buf)) != -1) {
				fos.write(buf, 0, len);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			closeObject(fis);
			closeObject(fos);
		}
	}
	
	private static void closeObject(Closeable obj) {
		if(obj != null) {
			try {
				obj.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
}
