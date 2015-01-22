package edu.upenn.cis455.crawler;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import edu.upenn.cis455.storage.Digest;
import edu.upenn.cis455.storage.FileStorage;
import edu.upenn.cis455.storage.HtmlCache;

public class CrawlerCache implements HtmlCache {
	
	public static final String TAG = CrawlerCache.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private File cachePath;
	
	public CrawlerCache(File cachePath) {
		this.cachePath = cachePath;
	}

	@Override
	public void saveHtml(String url, byte[] content) {
		String filename = hashFile(url);
		File file = new File(cachePath, filename);
		try {
			FileStorage.save(file, content);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public byte[] getHtml(String url) {
		String filename = hashFile(url);
		File file = new File(cachePath, filename);
		byte[] content = null;
		try {
			content = FileStorage.get(file);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return content;
	}
	
	public static String hashFile(String url) {
		return Digest.sha1(url);
	}
}
