package edu.upenn.cis455.mapreduce.job;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.upenn.cis455.client.HttpUrl;
import edu.upenn.cis455.crawler.CrawlerArchive;
import edu.upenn.cis455.crawler.HttpCrawler;
import edu.upenn.cis455.crawler.HttpCrawler.CrawlResult;
import edu.upenn.cis455.crawler.VideoPageParser;
import edu.upenn.cis455.db.CrawlerDatabase;
import edu.upenn.cis455.db.CrawlingHistoryDB;
import edu.upenn.cis455.db.DocInfoDB;
import edu.upenn.cis455.db.data.DocInfo;
import edu.upenn.cis455.db.data.ImageInfo;
import edu.upenn.cis455.db.data.VideoInfo;
import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.MapReduceBase;
import edu.upenn.cis455.mapreduce.Mapper;
import edu.upenn.cis455.mapreduce.Reducer;
import edu.upenn.cis455.util.Digest;
import edu.upenn.cis455.util.StringUtil;

public class Crawler {
	
	public static final String TAG = Crawler.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private static final int CRAWLER_TIMEOUT = 5 * 1000;
	// Constants
	public static final String CRAWLED_COUNTER = "crawled_counter";
	public static final String PARAM_LIMIT = "limit";
	public static final String PARAM_MAX_SIZE = "maxsize";
	public static final String PARAM_SALT = "salt";
	
	public static final String PATH_CRAWLER_BASE = "crawler/";
	public static final String PATH_INDEXER_BASE = "indexer/";
	public static final String PATH_INPUT_DIR = PATH_CRAWLER_BASE + "input/";
	public static final String PATH_OUTPUT_DIR = PATH_CRAWLER_BASE + "output/";
	public static final String PATH_DB_DIR = PATH_CRAWLER_BASE + "db/";
	public static final String PATH_DOCUMENTS_DIR = "documents/";
	
	public static final String OUTPUT_HTML_INDEX = "html";
	public static final String OUTPUT_IMAGE_INDEX = "image";
	public static final String OUTPUT_VIDEO_INDEX = "video";
	public static final String OUTPUT_PAGERANK = "pagerank";
	
	public static final String JOB_CRAWLING = "Crawl";

	public static class CrawlerMapper extends MapReduceBase implements Mapper {
		
		private String salt;
		
		@Override
		public void setup(Map<String, String> params) {
			salt = params.get(PARAM_SALT);
		}

		@Override
		public void map(String key, String value, Context context) {
			HttpUrl url = HttpUrl.parseUrl(value);
			if(url != null) {
				if(!"http".equalsIgnoreCase(url.getScheme())) {
					logger.info("Unsupported protocol: "
							+ url.getCanonicalUrl() + ", skip mapping");
					return;
				}
				String host = url.getHost();
				String salted = host;
				if(salt != null) {
					salted = host + salt;
				}
				if(host != null) {
					context.write(Digest.md5(salted), url.getCanonicalUrl());
				}
			}
		}
	}
	
	public static class CrawlerReducer extends MapReduceBase implements Reducer {
		
		private CrawlerDatabase db = null;
		private CrawlerArchive archive = null;
		private int limit = 1;
		private int maxSize = -1;
		private boolean exceeds = false;
		
		@Override
		public void setup(Map<String, String> params) {
			String stoPath = getStorageDir();
			String limitStr = params.get(PARAM_LIMIT);
			String maxSizeStr = params.get(PARAM_MAX_SIZE);
			
			db = CrawlerDatabase.getInstance();
			
			File arcDir = new File(stoPath, PATH_DOCUMENTS_DIR);
			archive = new CrawlerArchive(arcDir.getAbsolutePath());
			if(limitStr != null) {
				limit = StringUtil.parseInt(params.get(PARAM_LIMIT), 1);
			}
			if(maxSizeStr != null) {
				maxSize = StringUtil.parseInt(maxSizeStr, -1);
			}
		}

		@Override
		public void reduce(String key, String[] values, Context context) {
			// One crawler for a single host, so create a new crawler
			HttpCrawler crawler = new HttpCrawler();
			crawler.setDocumentArchive(archive);
			crawler.setConnectionTimeout(CRAWLER_TIMEOUT);
			if(maxSize > 0) {
				crawler.setMaxSize(maxSize);
			}
			for(String value : values) {
				if(exceeds) {
					context.write(value, null);
				} else {
					boolean succeed = false;
					try {
						succeed = doCrawl(crawler, value, context);
					} catch (Throwable t) {
						logger.error(t.getMessage(), t);
					}
					int count;
					if(succeed) { // Increment counter
						count = context.incrementCounter(CRAWLED_COUNTER, 1);
					} else {	// Update counter
						count = context.getCounter(CRAWLED_COUNTER);
					}
					if(count >= limit) {
						exceeds = true;
					}
				}
			}
		}

		private boolean doCrawl(HttpCrawler crawler,
				String value, Context context) {
			CrawlingHistoryDB crawlingSto = db.getCrawlingHistoryDB();
			
			HttpUrl url = HttpUrl.parseUrl(value);
			CrawlResult result = crawler.crawl(url);
			boolean redrOutside = false;
			int loops = 0;
			while(result != null && result.redirection != null) {
				if(++loops > 5) { // Exceed max loop
					logger.info("Too many redirections, break");
					break;
				}
				HttpUrl redrUrl = result.redirection;
				if(redrUrl.getCanonicalUrl().equals(url.getCanonicalUrl())) { // Trap
					logger.info("Trap detected: " + url.getCanonicalUrl());
					break;
				}
				
				// Handle redirection
				if(url.getHost().equals(redrUrl.getHost())) {
					url = redrUrl;
					result = crawler.crawl(url);
				} else {
					redrOutside = true;
					context.write(result.redirection.getCanonicalUrl(), null);
					break;
				}
			}
			if(redrOutside) {
				return false;
			}
			if(result != null) {
				DocInfo docInfo = result.info;
				if(docInfo == null) {
					return false;
				}
				
				String docUrl = docInfo.getUrl();
				
				// Content-seen test
				if(crawlingSto.isContentSeen(result.content)) {
					logger.info(docUrl + ": Content seen before");
					return false;
				}
				
				// Write indexer input
				JSONObject json = new JSONObject();
				json.put("filename", CrawlerArchive.hashFile(docUrl));
				if(docInfo.getType() != null) {
					json.put("type", docInfo.getType());
				}
				context.getMultipleContext(OUTPUT_HTML_INDEX)
				.write(docUrl, json.toString());
				
				// Extract links
				String content = getStringContent(result);
				if(content == null) {
					return false;
				}
				Document jsoupDoc = Jsoup.parse(content);
				JSONArray pagerankLinks = new JSONArray();
				Set<HttpUrl> links = extractLinks(jsoupDoc, url);
				String host = url.getHost();
				for(HttpUrl link : links) {
					// Only outside links should be calculated in pagerank
					if(!host.equals(link.getHost())) {
						pagerankLinks.put(link.getCanonicalUrl());
					}
					
					if(!crawlingSto.isUrlSeen(link)) { // Do url seen test
						// Only emit http links
						if("http".equalsIgnoreCase(link.getScheme())) {
							context.write(link.getCanonicalUrl(), null);
						}
					}
//					else {
//						logger.info(link.getCanonicalUrl() + ": URL seen");
//					}
				}
				if(pagerankLinks.length() > 0) {
					context.getMultipleContext(OUTPUT_PAGERANK)
					.write(url.getCanonicalUrl(), pagerankLinks.toString());
				}
				
				DocInfoDB docInfoDb = db.getDocInfoDB();
				// Extract img tags
				Set<ImgTag> imgTags = extractImgTags(jsoupDoc, url);
				for(ImgTag tag : imgTags) {
					HttpUrl srcUrl = tag.src;
					if(crawlingSto.isUrlSeen(srcUrl)) {
						continue;
					}
					
//					HeadResult headRes = crawler.sendHeadRequest(srcUrl);
//					while(headRes != null && headRes.redirection != null) {
//						srcUrl = HttpUrl.parseUrl(headRes.redirection);
//						headRes = crawler.sendHeadRequest(srcUrl);
//					}
//					if(headRes != null) {
					String contentType = "image"/*headRes.contentType*/;
					String srcUrlStr = srcUrl.getCanonicalUrl();
					// Save image info to db
					ImageInfo imageInfo = new ImageInfo(srcUrlStr);
					imageInfo.setDescription(tag.alt);
					imageInfo.setType(contentType);
					imageInfo.setPageUrl(docUrl);
					docInfoDb.saveImage(imageInfo);
					// Output for indexer
					if(contentType != null && contentType.startsWith("image")) {
						JSONObject imgJson = new JSONObject();
						imgJson.put("type", contentType);
						imgJson.put("description", tag.alt);
						imgJson.put("page", docUrl);
						context.getMultipleContext(OUTPUT_IMAGE_INDEX)
						.write(srcUrlStr, imgJson.toString());
					}
//					}
				}
				
				// Extract video page
				VideoInfo videoInfo = VideoPageParser.parse(docUrl, jsoupDoc);
				if(videoInfo != null) { // Is a video page
//					logger.debug(String.format("Video page: %s, title: %s, Desc: %s",
//							docUrl, videoInfo.getTitle(), videoInfo.getDescription()));
					// Save video info in db
					docInfoDb.saveVideo(videoInfo);
					// Output for indexer
					JSONObject videoJson = new JSONObject();
					String videoTitle = videoInfo.getTitle();
					String videoDesc = videoInfo.getDescription();
					String videoSite = videoInfo.getVideoSite();
					String videoId = videoInfo.getVideoId();
					videoJson.put("title", videoTitle != null ? videoTitle : "");
					videoJson.put("description", videoDesc != null ? videoDesc : "");
					videoJson.put("site", videoSite != null ? videoSite : "");
					videoJson.put("id", videoId != null ? videoId : "");
//					videoJson.put("type", "video");
					context.getMultipleContext(OUTPUT_VIDEO_INDEX)
					.write(docUrl, videoJson.toString());
				}
			}
			return true;
		}
	}
	
	// Extract href links from document
	private static Set<HttpUrl> extractLinks(Document doc,
			HttpUrl currentUrl) {
		Set<HttpUrl> links = new HashSet<HttpUrl>();
		Elements arefs = doc.select("a");
		for(int i = 0; i < arefs.size(); i++) {
			Element aref = arefs.get(i);
			String href = aref.attr("href").replaceAll("\n", "");
			if(href.startsWith("#")) {
				continue; // Anchor, ignore
			}
			HttpUrl url = parseUrlFromLink(href, currentUrl);
			if(url != null) {
				// Only output http links
				links.add(url);
			}
		}
		return links;
	}
	
	// Parse href into http url
	private static HttpUrl parseUrlFromLink(String href, HttpUrl currentUrl) {
		if(href == null || href.isEmpty()) {
			return null;
		}
		HttpUrl url = null;
		int scheme = href.indexOf(HttpUrl.SCHEME_DELIM);
		if(scheme >= 0) { // Absolute link
			url = HttpUrl.parseUrl(href);
		} else if(href.startsWith("//")) {	// Absolute
			url = HttpUrl.parseUrl(currentUrl.getScheme() + ":" + href);
		} else {
			url = new HttpUrl(currentUrl);
			if(href.charAt(0) == '/') { // From root
				url.setPath(href);
			} else { // Relative
				String path = url.getPath();
				if(!path.endsWith("/")) { // Current directory
					int slash = path.lastIndexOf('/');
					if(slash >= 0) {
						path = path.substring(0, slash + 1);
					}
				}
				path = path + href;
				url.setPath(path);
			}
		}
		return url;
	}
	
	private static String getStringContent(CrawlResult result) {
		if(result == null) {
			return null;
		}
		String str = null;
		String charset = null;
		if(result.info != null) {
			charset = result.info.getCharsetName();
		}
		if(result.content != null) {
			if(charset != null && !charset.equals(DocInfo.DEFAULT_CHARSET)) {
				str = new String(result.content, Charset.forName(charset));
			} else {
				str = new String(result.content);
			}
		}
		return str;
	}
	
	private static Set<ImgTag> extractImgTags(Document doc,
			HttpUrl currentUrl) {
		Set<ImgTag> list = new HashSet<ImgTag>();
		Elements imgs = doc.select("img");
		for(int i = 0; i < imgs.size(); i++) {
			Element img = imgs.get(i);
			String src = img.attr("src");
			String alt = img.attr("alt");
			if(alt != null && !alt.isEmpty()
				&& src != null && !src.isEmpty()) {
				ImgTag tag = new ImgTag();
				tag.src = parseUrlFromLink(src, currentUrl);
				tag.alt = alt;
				list.add(tag);
			}
		}
		return list;
	}
	
	static class ImgTag {
		HttpUrl src;
		String alt;
	}
}
