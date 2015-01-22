package edu.upenn.cis455.crawler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.MultipleOutputs;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.upenn.cis455.client.HttpUrl;
import edu.upenn.cis455.crawler.CrawlerWorker.CrawlResult;
import edu.upenn.cis455.crawler.CrawlerWorker.HeadResult;
import edu.upenn.cis455.crawler.MapReduceCrawler.CrawlerCounter;
import edu.upenn.cis455.storage.CrawlerStorageManager;
import edu.upenn.cis455.storage.CrawlingStorage;
import edu.upenn.cis455.storage.data.DocInfo;

public class CrawlerReduce extends MapReduceBase
implements Reducer<Text, Text, Text, NullWritable> {
	
	public static final String TAG = CrawlerReduce.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private Text out = new Text();
	private CrawlerCache cache = null;
	private MultipleOutputs mo = null;
	private int remaining;

	@Override
	public void configure(JobConf job) {
		mo = new MultipleOutputs(job);
		String docStorage = job.get("crawler.docstorage");
		if(docStorage != null) {
			File docStoDir = new File(docStorage);
			cache = new CrawlerCache(docStoDir);
		}
		remaining = job.getInt("crawler.remaining", 1);
	}

	@Override
	public void close() throws IOException {
		mo.close();
	}

	@Override
	public void reduce(Text key, Iterator<Text> values,
			OutputCollector<Text, NullWritable> output, Reporter reporter)
			throws IOException {
		CrawlingStorage crawlingSto = CrawlerStorageManager.getInstance().getCrawlingStorage();
		CrawlerWorker crawler = new CrawlerWorker();
		crawler.setHtmlCache(cache);
		while(values.hasNext()) {
			String value = values.next().toString();
			NullWritable nw = NullWritable.get();
			long ncrawled = reporter.getCounter(CrawlerCounter.CRAWLED_COUNT).getCounter();
			if(ncrawled >= remaining) { // Exceeds limit
				out.set(value);
				output.collect(out, nw);
				continue;
			}
			
			HttpUrl url = HttpUrl.parseUrl(value);
			CrawlResult result = crawler.crawl(url);
			boolean redrOutside = false;
			while(result != null && result.redirection != null) {
				HttpUrl redrUrl = result.redirection;
				if(redrUrl.getPath().equals(url.getPath())) { // Trap
					System.out.println("Trap detected: " + url.getCanonicalUrl());
					break;
				}
				
				// Handle redirection
				if(url.getHost().equals(redrUrl.getHost())) {
					url = redrUrl;
					result = crawler.crawl(url);
				} else {
					redrOutside = true;
					out.set(result.redirection.getCanonicalUrl());
					output.collect(out, nw);
					break;
				}
			}
			if(redrOutside) {
				continue;
			}
			if(result != null) {
				DocInfo docInfo = result.info;
				if(docInfo == null) {
					continue;
				}
				
				String docUrl = docInfo.getUrl();
				
				// Content-seen test
				if(crawlingSto.isContentSeen(result.content)) {
					logger.info(docUrl + ": Content seen before");
					continue;
				}
				
				// Increment counter
				reporter.incrCounter(CrawlerCounter.CRAWLED_COUNT, 1);

				// Write indexer input
				JSONObject json = new JSONObject();
				json.put("filename", CrawlerCache.hashFile(docUrl));
				if(docInfo.getType() != null) {
					json.put("type", docInfo.getType());
				}
				mo.getCollector(MapReduceCrawler.INDEXER_HTML_INPUT, reporter)
				.collect(docUrl, json.toJSONString());
				
				// Extract links
				String content = getStringContent(result);
				Document jsoupDoc = Jsoup.parse(content);
				if(content == null) {
					continue;
				}
				JSONArray jsonLinks = new JSONArray();
				List<HttpUrl> links = extractLinks(jsoupDoc, url);
				for(HttpUrl link : links) {
					jsonLinks.add(link.getCanonicalUrl());
					
					if(!crawlingSto.isUrlSeen(link)) { // Do url seen test
						out.set(link.getCanonicalUrl());
						output.collect(out, nw);
					}
//					else {
//						logger.info(link.getCanonicalUrl() + ": URL seen");
//					}
				}
				if(!jsonLinks.isEmpty()) {
					mo.getCollector(MapReduceCrawler.PAGERANK_DIR, reporter)
					.collect(url.getCanonicalUrl(), jsonLinks.toString());
				}
				
				// Extract img tags
				List<ImgTag> imgTags = extractImgTags(jsoupDoc, url);
				for(ImgTag tag : imgTags) {
					HttpUrl srcUrl = tag.src;
					if(crawlingSto.isUrlSeen(srcUrl)) {
						continue;
					}
					
					HeadResult headRes = crawler.sendHeadRequest(srcUrl);
					while(headRes != null && headRes.redirection != null) {
						srcUrl = HttpUrl.parseUrl(headRes.redirection);
						headRes = crawler.sendHeadRequest(srcUrl);
					}
					if(headRes != null) {
						String contentType = headRes.contentType;
						if(contentType != null && contentType.startsWith("image")) {
							JSONObject imgJson = new JSONObject();
							imgJson.put("type", contentType);
							imgJson.put("description", tag.alt);
							imgJson.put("page", docUrl);
							
							mo.getCollector(MapReduceCrawler.INDEXER_IMAGE_INPUT, reporter)
							.collect(srcUrl.getCanonicalUrl(), imgJson.toJSONString());
						}
					}
				}
				
				// Extract video page
				// Youtube
				HttpUrl vUrl = HttpUrl.parseUrl(docUrl);
				if(vUrl.getHost().equals("www.youtube.com")
					&& vUrl.getPath().startsWith("/watch")) {
					Element titleElem = null;
					Elements titles = jsoupDoc.select("#eow-title");
					if(titles.size() > 0) {
						titleElem = titles.get(0);
					}
					if(titleElem != null) {
						String title = titleElem.attr("title");
						JSONObject videoJson = new JSONObject();
						videoJson.put("description", title);
						videoJson.put("type", "video");
						
						mo.getCollector(MapReduceCrawler.INDEXER_VIDEO_INPUT, reporter)
						.collect(docUrl, videoJson.toJSONString());
					}
				}
			}
		}
	}
	
	// Extract href links from document
	private static List<HttpUrl> extractLinks(Document doc,
			HttpUrl currentUrl) {
		List<HttpUrl> links = new LinkedList<HttpUrl>();
		Elements arefs = doc.select("a");
		for(int i = 0; i < arefs.size(); i++) {
			Element aref = arefs.get(i);
			String href = aref.attr("href").replaceAll("\n", "");
			if(href.startsWith("#")) {
				continue; // Anchor, ignore
			}
			HttpUrl url = parseUrlFromLink(href, currentUrl);
			if(url != null) {
				if(url.getCanonicalUrl().startsWith("http://www.youtube.com/watch?v=dl8Ae0j2uaU&list=PLrEnWoR732/li>")) {
					System.out.println("Something wrong");
				}
				links.add(url);
			}
		}
		
//			List<HttpUrl> links = new LinkedList<HttpUrl>();
//			Matcher m = mHrefPtn.matcher(content);
//			while(m.find()) {
//				int count = m.groupCount();
//				String href = null;
//				if(count > 1) {
//					for(int i = 1; i < count; i++) {
//						href = m.group(i);
//						if(href != null && !href.isEmpty()) {
//							break;
//						}
//					}
//				}
//				if(href != null && href.toLowerCase().startsWith("javascript:")) {
//					continue; // Ignore javascripts
//				}
//				HttpUrl url = parseUrlFromLink(href, currentUrl);
//				if(url != null) {
//					links.add(url);
//				}
//			}
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
	
	private static List<ImgTag> extractImgTags(Document doc,
			HttpUrl currentUrl) {
		List<ImgTag> list = new LinkedList<ImgTag>();
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
