package edu.upenn.cis455.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.upenn.cis455.client.HttpResponse;
import edu.upenn.cis455.client.ResponseReader;
import edu.upenn.cis455.concurrency.ThreadPool;
import edu.upenn.cis455.concurrency.ThreadPoolFactory;
import edu.upenn.cis455.indexer.Indexer;
import edu.upenn.cis455.mapreduce.Mapper;
import edu.upenn.cis455.mapreduce.Reducer;
import edu.upenn.cis455.mapreduce.WorkerStatus;
import edu.upenn.cis455.mapreduce.job.Crawler;
import edu.upenn.cis455.mapreduce.job.Diff1;
import edu.upenn.cis455.mapreduce.job.Diff2;
import edu.upenn.cis455.mapreduce.job.Finish;
import edu.upenn.cis455.mapreduce.job.FormatFile;
import edu.upenn.cis455.mapreduce.job.IndexerDocument;
import edu.upenn.cis455.mapreduce.job.IndexerImage;
import edu.upenn.cis455.mapreduce.job.IndexerVideo;
import edu.upenn.cis455.mapreduce.job.Init;
import edu.upenn.cis455.mapreduce.job.Iter;
import edu.upenn.cis455.mapreduce.master.JobInfo;
import edu.upenn.cis455.mapreduce.master.MasterServlet;
import edu.upenn.cis455.mapreduce.master.WorkerInfo;
import edu.upenn.cis455.pagerank.Pagerank;
import edu.upenn.cis455.util.Digest;
import edu.upenn.cis455.util.StringUtil;
import edu.upenn.cis455.util.URLCodec;

public class CrawlerMasterServlet extends MasterServlet {

	private static final String TAG = CrawlerMasterServlet.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

	private static final long serialVersionUID = 1L;
	private static final NumberFormat NUMBER_FORMAT =
			NumberFormat.getIntegerInstance(Locale.US);
	
	private static final String STATUS_IDLE = "Idle";
	private static final String STATUS_FINISHED = "Finished";
	private static final String STATUS_CRAWLING = "Crawling";
	private static final String STATUS_INDEXING = "Indexing";
	private static final String STATUS_PANAGREKING = "PageRanking";
	
	private DateFormat mDateTimeFormat =
			SimpleDateFormat.getDateTimeInstance();
	private String mDialog = null; // Dialog message at start up
	private String mStatus = STATUS_IDLE;
	private int mCrawledCount = 0;
	private JobInfo mLastJob = null;
	private long mStartTime = 0;
	private long mCompletionTime = 0;
	private int mCrawlLimit = 0;
	private ThreadPool mThreadPool = ThreadPoolFactory.newSingleThreadPool();
	private ResponseReader mReader = new ResponseReader();

	public CrawlerMasterServlet() {
		TimeZone zone = TimeZone.getTimeZone("America/New_York");
		mDateTimeFormat.setTimeZone(zone);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String path = request.getPathInfo();
		if ("/crawler".equals(path)) {
			handleCrawler(request, response);
		} else {
			super.doGet(request, response);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();
		if ("/startcrawl".equals(path)) {
			handleStartCrawl(request, response);
		} else if("/startindex".equals(path)) {
			handleStartIndex(request, response);
		} else if("/startpagerank".equals(path)) {
			handleStartPagerank(request, response);
		} else {
			super.doPost(request, response);
		}
	}

	private void handleCrawler(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.print("<html><body");
		if (mDialog != null) { // Show message
			pw.print(" onload=\"alert(\'");
			pw.print(mDialog);
			pw.print("\');\" >");
			mDialog = null;
		} else {
			pw.print(">");
		}
		pw.print("<h1>Search Engine Control Panel</h1>");

		pw.print("<hr/>");
		printCralwerNodeStatus(pw);
		pw.print("<hr/>");
		pw.print("<h2>Job Status</h2>");
		pw.print("<div>Status: ");
		pw.print(mStatus);
		pw.print("</div>");
		if(mLastJob != null) {
			String jobName = mLastJob.getJobName();
			if(jobName.startsWith(Crawler.JOB_CRAWLING)) {
				printCrawlJobInfo(pw);
			} else if(jobName.startsWith(Indexer.JOB_INDEXING)) {
				printIndexJobInfo(pw);
			} else if(jobName.startsWith(Pagerank.JOB_PAGERANK)) {
				printPagerankJobInfo(pw);
			}
		}
		pw.print("<hr/>");
		pw.print("<table><tr><td style=\"vertical-align:top\">");
		printStartCrawlerForm(pw);
		pw.print("</td><td style=\"vertical-align:top\">");
		printStartIndexerForm(pw);
		pw.print("</td><td style=\"vertical-align:top\">");
		printStartPagerankerForm(pw);
		pw.print("</td></tr></table>");
		pw.print("</body></html>");
	}

	private void handleStartCrawl(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String mappersCount = URLCodec.decode(request.getParameter("mappers"));
		String reducersCount = URLCodec.decode(request.getParameter("reducers"));
		String limitStr = URLCodec.decode(request.getParameter("limit"));
		String maxSizeStr = URLCodec.decode(request.getParameter("maxsize"));
		boolean invalid = false;
		int mappers = StringUtil.parseInt(mappersCount, -1);
		int reducers = StringUtil.parseInt(reducersCount, -1);
		int limit = StringUtil.parseInt(limitStr, -1);
		int maxSize = -1;
		if (mappers < 0) {
			mDialog = "Invalid mappers count";
			invalid = true;
		} else if (reducers < 0) {
			mDialog = "Invalid reducers count";
			invalid = true;
		} else if (limit < 0) {
			mDialog = "Invalid crawling limit";
			invalid = true;
		} else if (!StringUtil.isEmpty(maxSizeStr)) {
			maxSize = StringUtil.parseInt(maxSizeStr, -1);
			if (maxSize == -1) {
				mDialog = "Invalid max page size";
				invalid = true;
			}
		}
		if (invalid) {
			response.sendRedirect("crawler");
			return;
		}
		List<WorkerInfo> workers = getActiveWorkers();
		if (workers.size() == 0) {
			mDialog = "Unable to start job. No active workers available";
			invalid = true;
		} else if (getRunningJob() != null) {
			mDialog = "A job is running, please wait until it completes";
			invalid = true;
		}
		if (invalid) {
			response.sendRedirect("crawler");
			return;
		}

		// Reset stats
		mCrawlLimit = limit;
		mCrawledCount = 0;
		mStartTime = System.currentTimeMillis();
		mCompletionTime = 0;
		// Start crawler thread
		mThreadPool.execute(new CrawlerTask(mappers, reducers, limit, maxSize));
		
		response.sendRedirect("crawler");
	}

	private void handleStartIndex(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String mappersCount = URLCodec.decode(request.getParameter("mappers"));
		String reducersCount = URLCodec.decode(request.getParameter("reducers"));
		String maxSizeStr = URLCodec.decode(request.getParameter("maxsize"));
		boolean invalid = false;
		int mappers = StringUtil.parseInt(mappersCount, -1);
		int reducers = StringUtil.parseInt(reducersCount, -1);
		int maxSize = -1;
		if (mappers < 0) {
			mDialog = "Invalid mappers count";
			invalid = true;
		} else if (reducers < 0) {
			mDialog = "Invalid reducers count";
			invalid = true;
		} else if(!StringUtil.isEmpty(maxSizeStr)) {
			maxSize = StringUtil.parseInt(maxSizeStr, -1);
			if(maxSize < 0) {
				mDialog = "Invalid max size";
				invalid = true;
			}
		}
		if (invalid) {
			response.sendRedirect("crawler");
			return;
		}
		List<WorkerInfo> workers = getActiveWorkers();
		if (workers.size() == 0) {
			mDialog = "Unable to start job. No active workers available";
			invalid = true;
		} else if (getRunningJob() != null) {
			mDialog = "A job is running, please wait until it completes";
			invalid = true;
		}
		if (invalid) {
			response.sendRedirect("crawler");
			return;
		}
		
		mStartTime = System.currentTimeMillis();
		mCompletionTime = 0;
		// Start indexers
		mThreadPool.execute(new IndexerHtmlTask(mappers, reducers, maxSize));
		mThreadPool.execute(new IndexerImageTask(mappers, reducers, maxSize));
		mThreadPool.execute(new IndexerVideoTask(mappers, reducers, maxSize));
		
		response.sendRedirect("crawler");
	}

	private void handleStartPagerank(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String mappersCount = URLCodec.decode(request.getParameter("mappers"));
		String reducersCount = URLCodec.decode(request.getParameter("reducers"));
		boolean invalid = false;
		int mappers = StringUtil.parseInt(mappersCount, -1);
		int reducers = StringUtil.parseInt(reducersCount, -1);
		if (mappers < 0) {
			mDialog = "Invalid mappers count";
			invalid = true;
		} else if (reducers < 0) {
			mDialog = "Invalid reducers count";
			invalid = true;
		}
		if (invalid) {
			response.sendRedirect("crawler");
			return;
		}
		List<WorkerInfo> workers = getActiveWorkers();
		if (workers.size() == 0) {
			mDialog = "Unable to start job. No active workers available";
			invalid = true;
		} else if (getRunningJob() != null) {
			mDialog = "A job is running, please wait until it completes";
			invalid = true;
		}
		if (invalid) {
			response.sendRedirect("crawler");
			return;
		}
		
		mStartTime = System.currentTimeMillis();
		mCompletionTime = 0;
		int totalDocument = collectDocumentCounts();
		if(totalDocument > 0) {
			// Start pagerank
			mThreadPool.execute(new PageRankThread(mappers, reducers, totalDocument));
		} else {
			mDialog = "Nothing has been crawled, cannot start pageranker";
		}
		response.sendRedirect("crawler");
	}

	private void printCralwerNodeStatus(PrintWriter pw) {
		pw.print("<h2>Crawler Node Statuses</h2>");
		List<WorkerInfo> workers = getActiveWorkers();
		if(workers.size() > 0) {
			int totalWebpages = 0;
			int totalImages = 0;
			int totalVideos = 0;
			int totalKeysToMap = 0;
			int totalKeysMapped = 0;
			int totalKeysToReduce = 0;
			int totalKeysReduced = 0;
			pw.print("<table border=\"1\">");
			pw.print("<tr><th>#</th><th>ID</th><th>IP:port</th><th>Status</th><th>Job</th>");
			pw.print("<th>Map</th><th>Reduce</th>");
			pw.print("<th>Webpages</th><th>Images</th><th>Videos</th></tr>");
			
			for(int i = 0; i < workers.size(); i++) {
				WorkerInfo info = workers.get(i);
				totalKeysToMap += info.keysRead;
				totalKeysMapped += info.keysMapped;
				totalKeysToReduce += info.keysToReduce;
				totalKeysReduced += info.keysReduced;
				
				int mapProgress = info.keysRead != 0 ?
					((int) (100.0 * info.keysMapped / info.keysRead)) : 0;
				int reduceProgress = info.keysToReduce != 0 ?
					((int) (100.0 * info.keysReduced / info.keysToReduce)) : 0;
				
				String webpages = info.getParameter(CrawlerNodeServlet.PARAM_DOCUMENT_COUNT);
				int webpageCount = StringUtil.parseInt(webpages, 0);
				totalWebpages += webpageCount;
				String images = info.getParameter(CrawlerNodeServlet.PARAM_IMAGE_COUNT);
				int imageCount = StringUtil.parseInt(images, 0);
				totalImages += imageCount;
				String videos = info.getParameter(CrawlerNodeServlet.PARAM_VIDEO_COUNT);
				int videoCount = StringUtil.parseInt(videos, 0);
				totalVideos += videoCount;
				
				pw.print("<tr><td width=\"20\" style=\"text-align:center\">");
				pw.print(i + 1);
				pw.print("</td><td>");
				pw.print(info.id);
				pw.print("</td><td width=\"100\">");
				String ipPort = info.host + ":" + info.port;
				pw.print("<a href=\"http://");
				pw.print(ipPort);
				pw.print("\" target=\"_blank\">");
				pw.print(ipPort);
				pw.print("</a>");
				pw.print("</td><td width=\"80\" style=\"text-align:center\">");
				String statusStr;
				WorkerStatus status = info.status;
				if(status != null) {
					statusStr = status.getName();
				} else {
					statusStr = "";
				}
				pw.print(statusStr);
				pw.print("</td><td width=\"200\">");
				pw.print(info.job);
				pw.print("</td><td width=\"80\" style=\"text-align:right\">");
				pw.print(mapProgress);
				pw.print("%");
				pw.print("</td><td width=\"80\" style=\"text-align:right\">");
				pw.print(reduceProgress);
				pw.print("%");
				pw.print("</td><td width=\"100\" style=\"text-align:right\">");
				pw.print(NUMBER_FORMAT.format(webpageCount));
				pw.print("</td><td width=\"100\" style=\"text-align:right\">");
				pw.print(NUMBER_FORMAT.format(imageCount));
				pw.print("</td><td width=\"100\" style=\"text-align:right\">");
				pw.print(NUMBER_FORMAT.format(videoCount));
				pw.print("</td></tr>");
			}
			int totalMapProgress = totalKeysToMap != 0 ?
				((int) (100.0 * totalKeysMapped / totalKeysToMap)) : 0;
			int totalReduceProgress = totalKeysToReduce != 0 ?
				((int) (100.0 * totalKeysReduced / totalKeysToReduce)) : 0;
					
			pw.print("<tr><td colspan=\"5\" style=\"text-align:right\"><b>Overall</b></td>");
			pw.print("<td style=\"text-align:right\"><b>");
			pw.print(totalMapProgress);
			pw.print("%</b></td>");
			pw.print("<td style=\"text-align:right\"><b>");
			pw.print(totalReduceProgress);
			pw.print("%</b></td>");
			pw.print("<td style=\"text-align:right\"><b>");
			pw.print(NUMBER_FORMAT.format(totalWebpages));
			pw.print("</b></td><td style=\"text-align:right\"><b>");
			pw.print(NUMBER_FORMAT.format(totalImages));
			pw.print("</b></td><td style=\"text-align:right\"><b>");
			pw.print(NUMBER_FORMAT.format(totalVideos));
			pw.print("</b></td></tr></table>");
		} else {
			pw.print("<p>No active crawlers currently</p>");
		}
	}
	
	private void printCrawlJobInfo(PrintWriter pw) {
		if(mLastJob == null) {
			return;
		}
		int mappers = mLastJob.getMappersCount();
		int reducers = mLastJob.getReducersCount();
		int limit = mCrawlLimit;
		int crawled = 0;
		int speed = 0;
		int seconds = 0;
		String maxSize = "";
		
		maxSize = mLastJob.getParameter(Crawler.PARAM_MAX_SIZE);
		if (maxSize == null) {
			maxSize = "No limit";
		} else {
			maxSize += " MB";
		}
		crawled = mCrawledCount;
		long endTime = mCompletionTime;
		if (mCompletionTime <= 0) { // Not completed
			crawled += getCounter(Crawler.CRAWLED_COUNTER);
			endTime = System.currentTimeMillis();
		}
		seconds = (int) ((endTime - mStartTime) / 1000);
		if (seconds != 0) {
			speed = 60 * crawled / seconds;
		}
		
		pw.print("<br/><div>Crawling Job Info:</div>");
		pw.print("<table border=\"1\">");
		pw.print("<tr><th>Mappers</th><th>Reducers</th><th>Crawl limit</th>");
		pw.print("<th>Max page size</th><th>Start Time</th><th>Completion Time</th></tr>");
		pw.print("<tr><td style=\"text-align:right\">");
		pw.print(mappers);
		pw.print("</td><td style=\"text-align:right\">");
		pw.print(reducers);
		pw.print("</td><td style=\"text-align:right\">");
		pw.print(NUMBER_FORMAT.format(limit));
		pw.print("</td><td style=\"text-align:right\">");
		pw.print(maxSize);
		pw.print("</td><td style=\"text-align:center\">");
		pw.print(mDateTimeFormat.format(new Date(mStartTime)));
		pw.print("</td><td style=\"text-align:center\">");
		if(mCompletionTime > 0) {
			pw.print(mDateTimeFormat.format(new Date(mCompletionTime)));
		} else {
			pw.print('-');
		}
		pw.print("</td></table>");

		int progress = mCrawlLimit != 0 ?
			((int) (100.0 * crawled / mCrawlLimit)) : 0;
		if(progress > 100) {
			progress = 100;
		}
		pw.print("<br/><div>Statistics:</div>");
		pw.print("<table border=\"1\">");
		pw.print("<tr><th>Progress</th><th>Page crawled</th>");
		pw.print("<th>Time elapsed</th><th>Avg. page/min</th></tr>");
		pw.print("<tr><td style=\"text-align:right\">");
		pw.print(progress);
		pw.print("%</td><td style=\"text-align:right\">");
		pw.print(NUMBER_FORMAT.format(crawled));
		pw.print("</td><td style=\"text-align:right\">");
		pw.print(NUMBER_FORMAT.format(seconds));
		pw.print("s</td><td style=\"text-align:right\">");
		pw.print(NUMBER_FORMAT.format(speed));
		pw.print("</td></tr></table>");
	}

	private void printIndexJobInfo(PrintWriter pw) {
		if(mLastJob == null) {
			return;
		}
		int mappers = mLastJob.getMappersCount();
		int reducers = mLastJob.getReducersCount();
		
		pw.print("<br/><div>Indexing Job Info:</div>");
		pw.print("<table border=\"1\">");
		pw.print("<tr><th>Mappers</th><th>Reducers</th><th>Start Time</th>");
		pw.print("<th>Completion Time</th></tr><tr><td style=\"text-align:right\">");
		pw.print(mappers);
		pw.print("</td><td style=\"text-align:right\">");
		pw.print(reducers);
		pw.print("</td><td style=\"text-align:center\">");
		pw.print(mDateTimeFormat.format(new Date(mStartTime)));
		pw.print("</td><td style=\"text-align:center\">");
		if(mCompletionTime > 0) {
			pw.print(mDateTimeFormat.format(new Date(mCompletionTime)));
		} else {
			pw.print('-');
		}
		pw.print("</td></tr></table>");
	}

	private void printPagerankJobInfo(PrintWriter pw) {
		if(mLastJob == null) {
			return;
		}
		int mappers = mLastJob.getMappersCount();
		int reducers = mLastJob.getReducersCount();
		
		pw.print("<br/><div>Pageranking Job Info:</div>");
		pw.print("<table border=\"1\">");
		pw.print("<tr><th>Mappers</th><th>Reducers</th><th>Start Time</th>");
		pw.print("<th>Completion Time</th></tr>");
		pw.print("<tr><td style=\"text-align:right\">");
		pw.print(mappers);
		pw.print("</td><td style=\"text-align:right\">");
		pw.print(reducers);
		pw.print("</td><td style=\"text-align:center\">");
		pw.print(mDateTimeFormat.format(new Date(mStartTime)));
		pw.print("</td><td style=\"text-align:center\">");
		if(mCompletionTime > 0) {
			pw.print(mDateTimeFormat.format(new Date(mCompletionTime)));
		} else {
			pw.print('-');
		}
		pw.print("</td></tr></table>");
	}
	
	private void printStartCrawlerForm(PrintWriter pw) {
		pw.print("<h2>Start Crawler</h2>");
		pw.print("<form method=\"post\" action=\"startcrawl\">");
		pw.print("<table><tr><td>Mappers count</td>");
		pw.print("<td><input style=\"text-align:right\" type=\"number\" name=\"mappers\"");
		pw.print(" min=\"1\" size=\"10\"/></td></tr>");
		pw.print("<tr><td>Reducers count</td>");
		pw.print("<td><input style=\"text-align:right\" type=\"number\" name=\"reducers\"");
		pw.print(" min=\"1\" size=\"10\"/></td></tr>");
		pw.print("<tr><td>Crawl limit</td>");
		pw.print("<td><input style=\"text-align:right\" type=\"number\" name=\"limit\"");
		pw.print(" min=\"1\" size=\"10\"/></td></tr>");
		pw.print("<tr><td>Max page size</td>");
		pw.print("<td><input style=\"text-align:right\" type=\"number\" name=\"maxsize\"");
		pw.print(" min=\"1\" size=\"10\" placeholder=\"optional, in MB\"/></td></tr>");
		pw.print("</table>");
		pw.print("<input type=\"submit\" value=\"Start\"/>");
		pw.print("</form>");
	}

	private void printStartIndexerForm(PrintWriter pw) {
		pw.print("<h2>Start Indexer</h2>");
		pw.print("<form method=\"post\" action=\"startindex\">");
		pw.print("<table><tr><td>Mappers count</td><td>");
		pw.print("<input style=\"text-align:right\" type=\"number\" name=\"mappers\"");
		pw.print(" min=\"1\" size=\"10\"/></td></tr>");
		pw.print("<tr><td>Reducers count</td><td>");
		pw.print("<input style=\"text-align:right\" type=\"number\" name=\"reducers\"");
		pw.print(" min=\"1\" size=\"10\"/></td></tr>");
		pw.print("<tr><td>Max document size</td><td>");
		pw.print("<input style=\"text-align:right\" type=\"number\" name=\"maxsize\"");
		pw.print(" min=\"1\" size=\"10\" placeholder=\"optional, in KB\"/></td></tr>");
		pw.print("</table>");
		pw.print("<input type=\"submit\" value=\"Start\"/>");
		pw.print("</form>");
	}

	private void printStartPagerankerForm(PrintWriter pw) {
		pw.print("<h2>Start Pageranker</h2>");
		pw.print("<form method=\"post\" action=\"startpagerank\">");
		pw.print("<table><tr><td>Mappers count</td>");
		pw.print("<td><input style=\"text-align:right\" type=\"number\" name=\"mappers\"");
		pw.print(" min=\"1\" size=\"10\"/></td></tr>");
		pw.print("<tr><td>Reducers count</td>");
		pw.print("<td><input style=\"text-align:right\" type=\"number\" name=\"reducers\"");
		pw.print(" min=\"1\" size=\"10\"/></td></tr>");
		pw.print("</table>");
		pw.print("<input type=\"submit\" value=\"Start\"/>");
		pw.print("</form>");
	}
	
	private int collectDocumentCounts() {
		int sum = 0;
		List<WorkerInfo> workers = getActiveWorkers();
		for(WorkerInfo worker : workers) {
			String countStr = worker.getParameter(CrawlerNodeServlet.PARAM_DOCUMENT_COUNT);
			int count = StringUtil.parseInt(countStr, 0);
			sum += count;
		}
		return sum;
	}

	class CrawlerTask implements Runnable {

		private int mappers;
		private int reducers;
		private int limit;
		private int maxSize;

		public CrawlerTask(int mappers, int reducers, int limit, int maxSize) {
			this.mappers = mappers;
			this.reducers = reducers;
			this.limit = limit;
			this.maxSize = maxSize;
		}

		@Override
		public void run() {
			mStatus = STATUS_CRAWLING;
			
			int remaining = limit;
			int iteration = 1;
			
			requestToAllWorkers("/clearhistory", null);

			while (true) {
				JobInfo job = new JobInfo();
				job.setJobName(Crawler.JOB_CRAWLING + " Iteration " + iteration);
				job.setMapperClass(Crawler.CrawlerMapper.class);
				job.setReducerClass(Crawler.CrawlerReducer.class);
				job.setInputDir(Crawler.PATH_INPUT_DIR);
				job.setOutputDir(Crawler.PATH_OUTPUT_DIR);
				job.setMappersCount(mappers);
				job.setReducersCount(reducers);
				job.addParameter(Crawler.PARAM_LIMIT, String.valueOf(remaining));
				if (maxSize > 0) {
					job.addParameter(Crawler.PARAM_MAX_SIZE,
							String.valueOf(maxSize));
				}
				// Add salt
				String salt = Digest.md5(String.valueOf(new Random().nextInt()));
				job.addParameter(Crawler.PARAM_SALT, salt);

				mLastJob = job;
				waitForCompletion(job);

				// requestToAllWorkers("/moveoutput", null);

				int crawled = getCounter(Crawler.CRAWLED_COUNTER);
				remaining -= crawled;
				mCrawledCount += crawled;

				if (crawled == 0) {
					logger.info("Nothing more to crawl, exit on iteration "
							+ iteration);
					break;
				} else if (remaining <= 0) {
					logger.info("Reaching crawling count limit, exit on iteration "
							+ iteration);
					break;
				}
				iteration++;
			}
			requestToAllWorkers("/mergeoutput", null);
			requestToAllWorkers("/clearhistory", null);

			mCompletionTime = System.currentTimeMillis();
			mStatus = STATUS_FINISHED;
			logger.info("Crawler is done, page crawled: " + mCrawledCount);
		}
	}

	class IndexerHtmlTask implements Runnable {
		private int mappers;
		private int reducers;
		private int maxSize;

		public IndexerHtmlTask(int mappers, int reducers, int maxSize) {
			this.mappers = mappers;
			this.reducers = reducers;
			this.maxSize = maxSize;
		}

		@Override
		public void run() {
			mStatus = STATUS_INDEXING;
			
			JobInfo job = new JobInfo();
			job.setJobName(Indexer.JOB_INDEXING_HTML);
			job.setMapperClass(IndexerDocument.class);
			job.setReducerClass(IndexerDocument.class);
			job.setInputDir(Indexer.PATH_HTML_DIR);
			job.setOutputDir(Indexer.PATH_HTML_DIR + "output/");
			job.setMappersCount(mappers);
			job.setReducersCount(reducers);
			if(maxSize > 0) {
				job.addParameter(Indexer.PARAM_MAX_SIZE, String.valueOf(maxSize));
			}

			mLastJob = job;
			waitForCompletion(job);

			logger.info("Indexing webpage done");
		}
	}

	class IndexerImageTask implements Runnable {
		private int mappers;
		private int reducers;
		private int maxSize;

		public IndexerImageTask(int mappers, int reducers, int maxSize) {
			this.mappers = mappers;
			this.reducers = reducers;
			this.maxSize = maxSize;
		}

		@Override
		public void run() {
			mStatus = STATUS_INDEXING;
			
			JobInfo job = new JobInfo();
			job.setJobName(Indexer.JOB_INDEXING_IMAGE);
			job.setMapperClass(IndexerImage.class);
			job.setReducerClass(IndexerImage.class);
			job.setInputDir(Indexer.PATH_IMAGE_DIR);
			job.setOutputDir(Indexer.PATH_IMAGE_DIR + "output/");
			job.setMappersCount(mappers);
			job.setReducersCount(reducers);
			if(maxSize > 0) {
				job.addParameter(Indexer.PARAM_MAX_SIZE, String.valueOf(maxSize));
			}

			mLastJob = job;
			waitForCompletion(job);

			logger.info("Indexing image done");
		}
	}

	class IndexerVideoTask implements Runnable {
		private int mappers;
		private int reducers;
		private int maxSize;

		public IndexerVideoTask(int mappers, int reducers, int maxSize) {
			this.mappers = mappers;
			this.reducers = reducers;
			this.maxSize = maxSize;
		}

		@Override
		public void run() {
			mStatus = STATUS_INDEXING;
			JobInfo job = new JobInfo();
			job.setJobName(Indexer.JOB_INDEXING_VIDEO);
			job.setMapperClass(IndexerVideo.class);
			job.setReducerClass(IndexerVideo.class);
			job.setInputDir(Indexer.PATH_VIDEO_DIR);
			job.setOutputDir(Indexer.PATH_VIDEO_DIR + "output/");
			job.setMappersCount(mappers);
			job.setReducersCount(reducers);
			if(maxSize > 0) {
				job.addParameter(Indexer.PARAM_MAX_SIZE, String.valueOf(maxSize));
			}

			mLastJob = job;
			waitForCompletion(job);

			mCompletionTime = System.currentTimeMillis();
			mStatus = STATUS_FINISHED;
			logger.info("Indexing video done");
		}
	}

	class PageRankThread implements Runnable {

		private int mappers;
		private int reducers;
		private int totalDocument;
		boolean diffComposite;

		public PageRankThread(int mappers, int reducers, int totalDocument) {
			this.mappers = mappers;
			this.reducers = reducers;
			this.totalDocument = totalDocument;
		}

		@Override
		public void run() {
			mStatus = STATUS_PANAGREKING;
			
			diffComposite = true;
			try {
//				deleteDB();
				logger.debug("Starting format");
				Class<?> jobClass = FormatFile.class;
				runJob(Pagerank.JOB_FORMAT, jobClass,
						Pagerank.PATH_FORMAT, Pagerank.PATH_INPUT, reducers, mappers);
				
				logger.debug("Starting init");
				jobClass = Init.class;
				runJob(Pagerank.JOB_INIT, jobClass,
						Pagerank.PATH_INPUT, Pagerank.PATH_INTERIM1, reducers, mappers);
				
				logger.debug("starting itering");
				// large initial diff
				double minDiff = 1.0 * totalDocument / 100000;
				if(minDiff > 30.0) {
					minDiff = 30.0;
				} else if(minDiff <= 0.001) {
					minDiff = 0.001;
				}
				double diff = 1000.0;
				// alternating number
				int evenOdd = 0;
				boolean fail = false;
				while (diff > minDiff) {
					// alternate running iter between the two files.
					if (evenOdd % 2 == 0) {
						jobClass = Iter.class;
						runJob(Pagerank.JOB_ITER1, jobClass,
								Pagerank.PATH_INTERIM1, Pagerank.PATH_INTERIM2, reducers, mappers);
					} else {
						jobClass = Iter.class;
						runJob(Pagerank.JOB_ITER2, jobClass,
								Pagerank.PATH_INTERIM2, Pagerank.PATH_INTERIM1, reducers, mappers);
						
//						requestToAllWorkers("/moveDirectories", null);
						
						jobClass = Diff1.class;
						runJob(Pagerank.JOB_DIFF1, jobClass,
								Pagerank.PATH_DIFF, Pagerank.PATH_DIFF_INTERIM,
								reducers, mappers);
						jobClass = Diff2.class;
						runJob(Pagerank.JOB_DIFF2, jobClass,
								Pagerank.PATH_DIFF_INTERIM, Pagerank.PATH_DIFF_OUTPUT, reducers, mappers);
						// requestToAllWorkers("/diff",null);
						
						diff = getDiff();
						logger.info("Getting diff: " + diff);
						if(diff < 0) {
							logger.error("Failed to get diff from any worker");
							fail = true;
							break;
						}
					}
					evenOdd++;
				}
				
				if(!fail) {
					// depending on where it finished use appropriate folder.
					jobClass = Finish.class;
					String inputPath;
					if (evenOdd % 2 == 0) {
						inputPath = Pagerank.PATH_INTERIM1;
					} else {
						inputPath = Pagerank.PATH_INTERIM2;
					}
					runJob(Pagerank.JOB_FINISH, jobClass, inputPath, Pagerank.PATH_OUTPUT, 1, 1);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			mCompletionTime = System.currentTimeMillis();
			mStatus = STATUS_FINISHED;
		}
	}

	private void runJob(String name, Class<?> jobClass, String input,
			String output, int reducers, int mappers) throws Exception {
		/* delete output directory if it exists */
		/* create new job */
		// set up file.
		JobInfo job = new JobInfo();
		job.setJobName(name);
		job.setMapperClass((Class<? extends Mapper>) jobClass);
		job.setReducerClass((Class<? extends Reducer>) jobClass);
		job.setInputDir(input);
		job.setOutputDir(output);
		job.setMappersCount(mappers);
		job.setReducersCount(reducers);
		
		mLastJob = job;
		waitForCompletion(job);
	}

	private double getDiff() {
		List<WorkerInfo> workers = getActiveWorkers();
		HttpResponse response = null;
//		int sum = 0;
		double diff = -1;
		for (WorkerInfo worker : workers) {
			try {
				response = sendPostToWorker(worker, "/diff", null);
				if(response.getStatusCode() == 200) {
					String content = mReader.readText(response);
					if (content != null && !content.equals("")) {
						diff = StringUtil.parseDouble(content, -1);
						break;
						// logger.info("Failed to retrieve document count from " +
						// worker.toString());
					}/* else {
						System.out.println("no response worker");
						// sum += count;
					}*/
				}
				response.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return diff;
	}
//	private double deleteDB() {
//		List<WorkerInfo> workers = getActiveWorkers();
//		HttpResponse response = null;
////		int sum = 0;
//		double diff = -1;
//		for (WorkerInfo worker : workers) {
//			try {
//				response = sendPostToWorker(worker, "/deletedb", null);
//				if(response.getStatusCode() == 200) {
//					logger.info("delete success");
//				}
//				response.close();
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//			}
//		}
//		return diff;
//	}

}