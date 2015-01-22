package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import edu.upenn.cis455.client.HttpClient;
import edu.upenn.cis455.client.HttpGetRequest;
import edu.upenn.cis455.client.HttpHeadRequest;
import edu.upenn.cis455.client.HttpRequest;
import edu.upenn.cis455.client.HttpResponse;
import edu.upenn.cis455.client.HttpUrl;
import edu.upenn.cis455.client.ResponseReader;
import edu.upenn.cis455.db.CrawlerDatabase;
import edu.upenn.cis455.db.DocInfoDB;
import edu.upenn.cis455.db.RobotRulesDB;
import edu.upenn.cis455.db.data.DocInfo;
import edu.upenn.cis455.db.data.RobotExclusion;
import edu.upenn.cis455.db.data.RobotExclusion.Rule;
import edu.upenn.cis455.util.StringUtil;

public class HttpCrawler {

	public static final String TAG = HttpCrawler.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	// For test purpose
	private static final String USER_AGENT = "cis455crawler";
	private static final int DEFAULT_TIMEOUT = 15 * 1000;
	
	private static int maxDelayTime = 500;
//	private static Pattern mHrefPtn;
	private static Rule defaultRule = new Rule(); // Default crawling rule: no disallows, 0 delay

	private CrawlerDatabase mStorage;
//	private Queue<HttpUrl> mUrlQueue = new LinkedList<HttpUrl>();
	private HttpClient mHttp = new HttpClient();
	private ResponseReader mReader = new ResponseReader();
	
	private int maxSize = -1;
	private HttpUrl mWorkingUrl = null;
	private Rule mRule = defaultRule; // Current crawling rule
	
//	static {
//		mHrefPtn = Pattern.compile("href\\s*=\\s*(?:\\\"([^\"]*)\\\"|'([^']*)'|([^'\">\\s]+))",
//				 Pattern.CASE_INSENSITIVE);
//	}
	
	public interface DocumentArchive {
		byte[] getHtml(String url);
		void saveHtml(String url, byte[] content);
	}
	private DocumentArchive mDocumentArchive = null;
	
	public HttpCrawler() {
		mStorage = CrawlerDatabase.getInstance();
		mHttp.setTimeout(DEFAULT_TIMEOUT);
	}

//	public String getWorkingHostPort() {
//		return mWorkingUrl != null ? mWorkingUrl.getHostPort() : null;
//	}
	
	/**
	 * Set the maximum size, in megabytes, of a document to be retrieved from a Web server
	 * @param maxSize
	 */
	public void setMaxSize(int maxSizeMb) {
		this.maxSize = maxSizeMb * 1024 * 1024;
	}

	public CrawlResult crawl(String url) {
		return crawl(HttpUrl.parseUrl(url));
	}

	public void setDocumentArchive(DocumentArchive docArchive) {
		this.mDocumentArchive = docArchive;
	}

	public void setConnectionTimeout(int toMilliSec) {
		if(toMilliSec >= 0) {
			mHttp.setConnectionTimeout(toMilliSec);
			mHttp.setTimeout(toMilliSec);
		}
	}
	
	public CrawlResult crawl(HttpUrl url) {
		if(url == null) {
			return null;
		}
		String cononicalUrl = url.getCanonicalUrl(); // Used as key to storage
		if(!"http".equalsIgnoreCase(url.getScheme())) {
			logger.info("Unsupported protocol for " + cononicalUrl);
			return null;
		}
		String hostPort = url.getHostPort();
		// Set the host addr and retrieve crawling rules
		if(mWorkingUrl == null || !hostPort.equals(mWorkingUrl.getHostPort())) {
			printMessage("Processing robots.txt on host: " + hostPort);
			// Get crawling rules via downloading robots.txt
			setRobotsExclusionRules(hostPort);
		} else {
			// The crawler is working on the same host
			int wait = mRule.getCrawlDelay() * 1000;
			if(wait > 0) { 
				if(maxDelayTime >= 0 && wait > maxDelayTime) {
					wait = maxDelayTime;
				}
				try {
					Thread.sleep(wait);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		mWorkingUrl = url;
		
		// Examine the rule to see whether this url is not allowed to crawl
		if(isDisallowed(url)) {
			printMessage(cononicalUrl + ": Disallowed by robots rules");
			return null;
		}
		
		// Send HEAD request to get header info by this url
		HeadResult info = sendHeadRequest(url);
		if(info == null) {
			// Cannot get info
			printMessage(cononicalUrl + ": Failed to get document info");
			return null;
		}
		if(info.redirection != null) { // Redirected
			HttpUrl redrUrl = HttpUrl.parseUrl(info.redirection);
			CrawlResult result = new CrawlResult();
			result.redirection = redrUrl;
			return result;
		}
		if(info.statusCode != 200) {
			printMessage(cononicalUrl + ": Request failed, response status code: " + info.statusCode);
			return null;
		}
		
		String contentType = info.contentType;
		boolean interested = false;
		// Determine by content type
		if(contentType != null && "text/html".equalsIgnoreCase(contentType)) {
			interested = true;
		}
		// Determine by url
		if(!interested) {
			String path = url.getPath();
			if(path != null && (path.endsWith(".html") || path.endsWith(".htm"))) {
				interested = true;
			}
		}
		if(!interested) {
			printMessage(cononicalUrl + ": Not html document");
			return null;
		}
		// Attempt to filter non-english page
		if(info.contentLanguage != null // Detect language
		&& !info.contentLanguage.contains("en")) {
			printMessage(cononicalUrl + ": Non-english document");
			return null;
		}
		
		int contentLength = info.contentLength;
		if(maxSize >= 0 && contentLength >= 0 && contentLength > maxSize) {
			printMessage(cononicalUrl + ": Content larger than maximum size");
			return null; // Content size exceeds maximum
		}
		
		DocInfo document = null;
		DocInfoDB docStorage = null;
		if(mStorage != null) {
			docStorage = mStorage.getDocInfoDB();
			document = docStorage.getDocument(cononicalUrl);
		}
		
		byte[] content = null;
		if(document != null) {
			if(mDocumentArchive != null) {
				content = mDocumentArchive.getHtml(document.getUrl());
			}
		}
		// Whether should we download this document
		boolean shouldDownload = (content == null || content.length == 0);
		if(!shouldDownload) {
			long lastModified = info.lastModified;
			if(document.getLastCheckDate() < lastModified) {
				printMessage(cononicalUrl + ": Updated.");
				shouldDownload = true; // Document is updated and should download
			} else { // Not modified
				printMessage(cononicalUrl + ": Not modified.");
			}
		}
		if(shouldDownload) { // Download document
			printMessage(cononicalUrl + ": Downloading...");
			content = download(url);
			if(content != null) {
				if(document == null) {
					document = new DocInfo(cononicalUrl);
				}
				if(info.charset != null) {
					document.setCharsetName(info.charset);
				}
				document.setType(contentType);
//				document.setType(isXml ? Document.TYPE_XML : Document.TYPE_HTML);
//				document.setContent(content);
				// Store content
				if(mDocumentArchive != null) {
					mDocumentArchive.saveHtml(document.getUrl(), content);
				}
			}
		}
		
		if(document == null) {
			// Cannot get document
			printMessage(cononicalUrl + ": Failed to get document");
			return null;
		}
		
		// Update last check date
		document.setLastCheckDate(System.currentTimeMillis());
		if(docStorage != null) {
			docStorage.saveDocument(document);
		}
		
		CrawlResult result = new CrawlResult();
		result.info = document;
		result.content = content;
		result.updated = shouldDownload;
		
		return result;
	}
	
	public HeadResult sendHeadRequest(HttpUrl url) {
		return sendHeadRequest(url, false);
	}

	public HeadResult sendHeadRequest(HttpUrl url, boolean get) {
			if(url == null || !"http".equalsIgnoreCase(url.getScheme())) {
				return null;
			}
			HeadResult info = new HeadResult();
			HttpRequest request;
			if(get) {
				request = new HttpGetRequest(url);
			} else {
				request = new HttpHeadRequest(url);
			}
			addHeaders(request);
			HttpResponse response = null;
			try {
				response = mHttp.execute(request);
				int statusCode = response.getStatusCode();
				if(statusCode == 405 && !get) { // HEAD not allowed
					return sendHeadRequest(url, true);
				}
				info.statusCode = statusCode;
				// Check content type
				String contentType = response.getHeader("Content-Type");
				info.charset = ResponseReader.getCharset(contentType);
				if(contentType != null) {
					int semicolon = contentType.indexOf(';');
					if(semicolon >= 0) {
						contentType = contentType.substring(0, semicolon);
					}
				} else {
					// Judge from extension
					String ext = StringUtil.getExtension(url.getPath());
					if("html".equalsIgnoreCase(ext)
					|| "htm".equalsIgnoreCase(ext)) {
						contentType = "text/html";
					} else if("xml".equalsIgnoreCase(ext)) {
						contentType = "application/xml";
					}
				}
				info.contentType = contentType;
				
				// Check content length
				int contentLength = StringUtil.parseInt(response.getHeader("Content-Length"), -1);
				info.contentLength = contentLength;
				
				// Check content language
				info.contentLanguage = response.getHeader("Content-Language");
				
				// Check last modified
				long modTime = response.getDateHeader("Last-Modified");
				info.lastModified = modTime;
				
				if(statusCode >= 301 && statusCode <= 303) { // Redirection
					info.redirection = response.getHeader("Location");
				}
			} catch (Throwable t) {
				logger.error(t.getMessage(), t);
			} finally {
				closeResponse(response);
			}
			return info;
		}

	/*
	 * Get crawling rules for this host
	 * First search local database, if not downloaded,
	 * download it and parse it into rules. If robots.txt
	 * does not exist, use default rule.
	 */
	private void setRobotsExclusionRules(String host) {
		RobotExclusion robot = null;
		RobotRulesDB robotStorage = null;
		if(mStorage != null) {
			robotStorage = mStorage.getRobotRulesDB();
			robot = robotStorage.getRobotExclusion(host);
		}
		if(robotStorage != null && robot == null) {
			if(robotStorage.isNotExistRobot(host)) { // robots.txt not exist
				mRule = defaultRule; // Set to default rule
			} else { // Not yet downloaded
				// Download robots.txt from url and process it
				String url = host + "/robots.txt";
				robot = downloadRobotsRules(robotStorage, HttpUrl.parseUrl(url));
				if(robot != null) {
					mRule = getTheRule(robot);
				} else { // robots.txt not exist
					mRule = defaultRule;
					robotStorage.saveNotExistRobot(host);
				}
			}
		} else {
			mRule = getTheRule(robot);
		}
	}
	
	private RobotExclusion downloadRobotsRules(RobotRulesDB robotStorage, HttpUrl url) {
		if(url == null || !"http".equalsIgnoreCase(url.getScheme())) {
			return null;
		}
		RobotExclusion robot = null;
		HttpRequest request = new HttpGetRequest(url);
		HttpResponse response = null;
		addHeaders(request);
		try {
			response = mHttp.execute(request);
			int statusCode = response.getStatusCode();
			if(statusCode == 200) { // OK
				String content = mReader.readText(response);
				robot = robotStorage.parseRobotTxt(url.getHost(), content);
			} else if(statusCode >= 301 && statusCode <= 303) { // Redirection
				String location = response.getHeader("Location");
				if(location != null) {
					robot = downloadRobotsRules(robotStorage, HttpUrl.parseUrl(location));
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		} finally {
			closeResponse(response);
		}
		return robot;
	}
	
	/*
	 * Get the rule that this crawler should apply to
	 */
	private Rule getTheRule(RobotExclusion robot) {
		if(robot == null) {
			return defaultRule;
		}
		List<Rule> rules = robot.getRules();
		Rule theRule = defaultRule;
		for(Rule rule : rules) {
			String userAgent = rule.getUserAgent();
			if("*".equals(userAgent)) { // Default rule
				theRule = rule;
			} else if(USER_AGENT.equals(userAgent)) {
				theRule = rule;
				break;
			}
		}
		return theRule;
	}
	
	private byte[] download(HttpUrl url) {
		if(url == null || !"http".equalsIgnoreCase(url.getScheme())) {
			return null;
		}
		byte[] content = null;
		HttpRequest request = new HttpGetRequest(url);
		HttpResponse response = null;
		addHeaders(request);
		try {
			response = mHttp.execute(request);
			int statusCode = response.getStatusCode();
			if(statusCode == 200) { // OK
				// Check content length again
				int length = StringUtil.parseInt(response.getHeader("Content-Length"), -1);
				if(maxSize >= 0 && length >= 0 && length > maxSize) {
					logger.info(url.getCanonicalUrl() + ": Content larger than max size, abort");
					return null;
				}
				content = mReader.readContent(response);
			}/* else if(statusCode >= 301 && statusCode <= 303) { // Redirection
				String location = response.getHeader("Location");
				if(location != null) {
					logger.info("Redirected to " + location);
					content = download(HttpUrl.parseUrl(location));
				}
			}*/
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		} finally {
			closeResponse(response);
		}
		return content;
	}
	
	private boolean isDisallowed(HttpUrl url) {
		String path = url.getPath();
		return mRule.isDisallowed(path);
	}
	
	private static void addHeaders(HttpRequest request) {
		request.addHeader("User-Agent", USER_AGENT);
		request.addHeader("Accept-Language", "en-US,en;q=0.5");
	}
	
	private static void closeResponse(HttpResponse response) {
		if(response != null) {
			try {
				response.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private void printMessage(String message) {
		logger.info(message);
//		System.out.println(/*"[CrawlerWorker #" + getId() + "] " + */message);
	}
	
	public static class HeadResult {
		public int statusCode = 0;
		public String contentType = null;
		public String contentLanguage = null;
		public String charset = null;
		public int contentLength = -1;
		public long lastModified = -1;
		public String redirection = null;
	}
	
	public static class CrawlResult {
		public DocInfo info;
		public HttpUrl redirection;
		public byte[] content;
		public boolean updated;
	}
}
