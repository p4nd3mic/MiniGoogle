package edu.upenn.cis455.crawler;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import edu.upenn.cis455.crawler.HttpCrawler.DocumentArchive;
import edu.upenn.cis455.util.Digest;
import edu.upenn.cis455.util.FileUtil;

public class CrawlerArchive implements DocumentArchive {
	
	public static final String TAG = CrawlerArchive.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private File arcPath;
	
	public CrawlerArchive(String path) {
		arcPath = new File(path);
		if(!arcPath.exists()) {
			arcPath.mkdirs();
		}
	}

	@Override
	public void saveHtml(String url, byte[] content) {
		String filename = hashFile(url);
		File file = new File(arcPath, filename);
		try {
			FileUtil.save(file, content);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public byte[] getHtml(String url) {
		String filename = hashFile(url);
		File file = new File(arcPath, filename);
		if(!file.exists()) {
			return null;
		}
		byte[] content = null;
		try {
			content = FileUtil.get(file);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return content;
	}
	
	public static String hashFile(String url) {
		return Digest.sha1(url);
	}
}
