
package edu.upenn.cis455.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import edu.upenn.cis455.db.CrawlerDatabase;
import edu.upenn.cis455.db.DocInfoDB;
import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;
import edu.upenn.cis455.indexer.Indexer;
import edu.upenn.cis455.indexer.KeyWordSearchDocument;
import edu.upenn.cis455.indexer.KeyWordSearchImage;
import edu.upenn.cis455.indexer.KeyWordSearchVideo;
import edu.upenn.cis455.mapreduce.job.Crawler;
import edu.upenn.cis455.mapreduce.worker.WorkerServlet;
import edu.upenn.cis455.pagerank.AddGetFromDB;
import edu.upenn.cis455.pagerank.Pagerank;
import edu.upenn.cis455.pagerank.Ranks;
import edu.upenn.cis455.util.FileUtil;
import edu.upenn.cis455.util.StringUtil;
import edu.upenn.cis455.utility.addLocToDB;

public class CrawlerNodeServlet extends WorkerServlet {
	
	public static final String TAG = CrawlerNodeServlet.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

	private static final long serialVersionUID = 1L;

	public static final String PARAM_DOCUMENT_COUNT = "docs";
	public static final String PARAM_IMAGE_COUNT = "images";
	public static final String PARAM_VIDEO_COUNT = "videos";
	
	private static final String TEMP_DIR = "temp";
	private static final String INPUT_FILE = "input";
	private static final String LOCATION_FILE = "US.txt";
	
	private File mInputDir;
	private File mOutputDir;
	private File mHtmlIndexDir;
	private File mImageIndexDir;
	private File mVideoIndexDir;
	private File mPagerankInputDir;
	
	// Different document counts
	private int mDocumentCount = 0;
	private int mImageCount = 0;
	private int mVideoCount = 0;
	
	private static FileFilter hiddenFileFilter = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return !pathname.isHidden();
		}
	};
	
	@Override
	protected void onInit() {
		String homeDir = System.getProperty("user.home");
		// Initialize paths
		String storageDir = getStorageDir();
		mInputDir = new File(storageDir, Crawler.PATH_INPUT_DIR);
		mOutputDir = new File(storageDir, Crawler.PATH_OUTPUT_DIR);
		mHtmlIndexDir = new File(storageDir, Indexer.PATH_HTML_DIR);
		mImageIndexDir = new File(storageDir, Indexer.PATH_IMAGE_DIR);
		mVideoIndexDir = new File(storageDir, Indexer.PATH_VIDEO_DIR);
		mPagerankInputDir = new File(storageDir, Pagerank.PATH_FORMAT);
		
		// Initialize DB
		File crawlerDbDir = new File(storageDir, Crawler.PATH_DB_DIR);
		File indexerDbDir = new File(storageDir, Indexer.PATH_DB_DIR);
		File pagerankDbDir = new File(storageDir, Pagerank.PATH_DB);
		File locationFile = new File(homeDir, LOCATION_FILE);
		CrawlerDatabase.setup(crawlerDbDir.getAbsolutePath());
		DBSingleton.setDbPath(indexerDbDir.getAbsolutePath());
		AddGetFromDB.setup(pagerankDbDir);
		
		// Add location data
		if(locationFile.exists()) {
			logger.info("Found US.txt in " + locationFile.getAbsolutePath()
					+ ", adding into db");
			DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();
			try {
				addLocToDB.addLocToDb(wrapper, locationFile);
				logger.info("Adding locations finished");
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			// Delete after adding
			locationFile.delete();
		}
		// Init document counts
		updateDocumentCounts();
	}

//	@Override
//	public void doGet(HttpServletRequest request, HttpServletResponse response)
//			throws IOException {
//		String path = request.getPathInfo();
//		if("/shutdown".equals(path)) {
//			handleShutdown(request, response);
//		} else {
//			super.doGet(request, response);
//		}
//	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();
		if("/clearhistory".equals(path)) {
			handleClearHistory(request, response);
		} else if("/mergeoutput".equals(path)) {
			handleMergeOutput(request, response);
		} else if("/diff".equals(path)) {
			handleGetDiff(request, response);
		} else if("/deletedb".equals(path)) {
			handleCreateDB();
		} else if("/getranks".equals(path)) {
			handleGetPageRanks(request, response);
		} else if("/gettfidf".equals(path)) {
			handleGetTfIdf(request, response);
		} else {
			super.doPost(request, response);
		}
	}

	@Override
	public void destroy() {
		logger.info("Closing databases...");
		super.destroy();
		CrawlerDatabase db = CrawlerDatabase.getInstance();
		if(db != null) {
			db.close();
		}
		DBSingleton indexerDb = DBSingleton.getInstance();
		if(indexerDb != null) {
			indexerDb.closeBDBstore();
		}
		AddGetFromDB pagerankDb = AddGetFromDB.getInstance();
		if(pagerankDb != null) {
			pagerankDb.shutdown();
		}
	}

	@Override
	public void onReduceFinished() {
		String jobName = getJobName();
		if(jobName != null) {
			if(jobName.startsWith("Crawl")) {
				CrawlerDatabase.getInstance().sync(); // Sync crawler db
				updateDocumentCounts(); // Update counts
				moveCrawlerOutput();
			} else if(jobName.equals(Indexer.JOB_INDEXING_HTML)) {
				DBSingleton.getInstance().sync(); // Sync indexer db
				FileUtil.clearDir(mHtmlIndexDir);
			} else if(jobName.equals(Indexer.JOB_INDEXING_IMAGE)) {
				DBSingleton.getInstance().sync();
				FileUtil.clearDir(mImageIndexDir);
			} else if(jobName.equals(Indexer.JOB_INDEXING_VIDEO)) {
				DBSingleton.getInstance().sync();
				FileUtil.clearDir(mVideoIndexDir);
			} else if(jobName.equals(Pagerank.JOB_ITER2)) {
				moveDirectories();
			} else if(jobName.equals(Pagerank.JOB_FINISH)) {
				AddGetFromDB.getInstance().sync(); // Sync pagerank db
				// Delete pagerank outputs
				// Not deleting format input, leave it appending
//				FileUtil.deleteDir(new File(getStorageDir(), Pagerank.PATH_FORMAT));
				FileUtil.deleteDir(new File(getStorageDir(), Pagerank.PATH_INPUT));
				FileUtil.deleteDir(new File(getStorageDir(), Pagerank.PATH_INTERIM1));
				FileUtil.deleteDir(new File(getStorageDir(), Pagerank.PATH_INTERIM2));
				FileUtil.deleteDir(new File(getStorageDir(), Pagerank.PATH_DIFF));
				FileUtil.deleteDir(new File(getStorageDir(), Pagerank.PATH_DIFF_INTERIM));
				FileUtil.deleteDir(new File(getStorageDir(), Pagerank.PATH_DIFF_OUTPUT));
			}
		}
		super.onReduceFinished();
	}

	/**
	 * Send document counts with heartbeat
	 */
	@Override
	protected void addHeartBeatParameters(Map<String, String> params) {
		params.put(PARAM_DOCUMENT_COUNT, String.valueOf(mDocumentCount));
		params.put(PARAM_IMAGE_COUNT, String.valueOf(mImageCount));
		params.put(PARAM_VIDEO_COUNT, String.valueOf(mVideoCount));
	}

	private void moveCrawlerOutput() {
		// Clear input data
		FileUtil.clearDir(mInputDir);
		
		// Move output data to their folder
		File[] outputs = mOutputDir.listFiles();
		if(outputs == null) {
			return;
		}
		for(int i = 0; i < outputs.length; i++) {
			File output = outputs[i];
			String filename = output.getName();
			File destDir = null;
			boolean isOutput = false;
			if(filename.startsWith(Crawler.OUTPUT_HTML_INDEX)) { // HTML index
				destDir = new File(getStorageDir(), Indexer.PATH_HTML_DIR);
			} else if(filename.startsWith(Crawler.OUTPUT_IMAGE_INDEX)) { // Image index
				destDir = new File(getStorageDir(), Indexer.PATH_IMAGE_DIR);
			} else if(filename.startsWith(Crawler.OUTPUT_VIDEO_INDEX)) { // Video index
				destDir = new File(getStorageDir(), Indexer.PATH_VIDEO_DIR);
			} else if(filename.startsWith(Crawler.OUTPUT_PAGERANK)) { // Pagerank
				destDir = new File(getStorageDir(), Pagerank.PATH_FORMAT);
			} else { // Output file
				isOutput = true;
				destDir = mInputDir;
			}
			if(destDir != null) {
				if(!isOutput) {
					destDir = new File(destDir, TEMP_DIR);
				}
				if(!destDir.exists()) {
					destDir.mkdirs();
				}
				int number = getIndexNumber(destDir);
				File newFile = new File(destDir, String.valueOf(number));
				output.renameTo(newFile);
			}
		}
		FileUtil.deleteDir(mOutputDir);
	}

	private void handleClearHistory(HttpServletRequest request,
			HttpServletResponse response) {
		CrawlerDatabase db = CrawlerDatabase.getInstance();
		if(db != null) {
			db.getCrawlingHistoryDB().clear();
		}
	}
	private void handleCreateDB() {
		String storageDir = getStorageDir();
		File pagerankDbDir = new File(storageDir, Pagerank.PATH_DB);
		if (pagerankDbDir.exists()) {
			deleteDir(pagerankDbDir);
			//create db.
			AddGetFromDB.setup(pagerankDbDir);
		} 
	}


	private void handleMergeOutput(HttpServletRequest request,
			HttpServletResponse response) {
		mergeTempFiles(mHtmlIndexDir);
		mergeTempFiles(mImageIndexDir);
		mergeTempFiles(mVideoIndexDir);
		mergeTempFiles(mPagerankInputDir);
	}

	private void handleGetDiff(HttpServletRequest request, HttpServletResponse response)
			 throws IOException {
		 File diffOutput = null;
		 File dir = new File(getStorageDir(), Pagerank.PATH_DIFF_OUTPUT);
		 File[] files = dir.listFiles();
		 if(files != null) {
			 diffOutput = files[0];
		 }
		 
		 if (diffOutput != null) {
			 String diff;
			BufferedReader br = new BufferedReader(new FileReader(diffOutput));
			diff = br.readLine();
			br.close();
			if(diff != null) {
				PrintWriter pw = response.getWriter();
				//return diff to master.
				pw.print(diff);
			} else {
				response.sendError(404);
			}
		 } else {
			 response.sendError(404);
		 }
	 }

	private void handleGetTfIdf(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String keyword = request.getParameter("keyword");
		String type = request.getParameter("type");
		
		String result = null;
		if("document".equals(type)) {
			KeyWordSearchDocument search = new KeyWordSearchDocument(keyword);
			result = search.getResultSet();
		} else if("image".equals(type)) {
			KeyWordSearchImage search = new KeyWordSearchImage(keyword);
			result = search.getResultSet();
		} else if("video".equals(type)) {
			KeyWordSearchVideo search = new KeyWordSearchVideo(keyword);
			result = search.getResultSet();
		}
		
		if(!StringUtil.isEmpty(result)) {
			PrintWriter writer = response.getWriter();
			writer.print(result);
//			ByteArrayInputStream bais = new ByteArrayInputStream(result.getBytes());
//			OutputStream os = response.getOutputStream();
//			byte[] buf = new byte[8192];
//			int len;
//			while((len = bais.read(buf)) != -1) {
//				os.write(buf, 0, len);
////				logger.debug("Write " + len + " bytes to output");
//			}
		} else {
			response.sendError(404);
		}
	}

	private void handleGetPageRanks(HttpServletRequest request, HttpServletResponse response)
				 throws IOException {
//		File storedRanks = new File(getStorageDir(), Pagerank.PATH_DB);
//		Gson gson = new Gson();
		
		Type listType = new TypeToken<List<String>>() {}.getType();
		int length = request.getContentLength();
		InputStream input = request.getInputStream();
		String content = readString(input, length);
//		BufferedReader br;
		//read the json that was sent from master as array or urls.
//		String json = br.readLine();
		//change json string to list of string urls.
		List<String> list = new Gson().fromJson(content, listType);
		//storage location
//			AddGetFromDB ranks = new AddGetFromDB(storedRanks);
		AddGetFromDB ranks = AddGetFromDB.getInstance();
		Ranks r;
		JsonObject root = new JsonObject();
		//loop through each url.
		for (String url : list) {
			//retrieve rank
			r = ranks.getRanksObject(url);
			if(r != null) {
				//add them to json object that will be sent back
				root.addProperty(url, r.getRank());
			} else {
				logger.warn("Missing pagerank for " + url);
				root.addProperty(url, "0");
			}
		}
		//now convert to json
		String returnJson = new Gson().toJson(root);
		PrintWriter out = response.getWriter();
		//returns json url->rank to master.
		out.print(returnJson);
	}
	
//	private void handleShutdown(HttpServletRequest request,
//			HttpServletResponse response) throws IOException {
//		response.addHeader("Connection", "close");
//		response.setContentType("text/html");
//		PrintWriter pw = response.getWriter();
//		pw.print("<html><body>Shutting down node...</body></html>");
//		pw.flush();
//		ServerInstance server = ServerManager.getCurrentServer();
//		if(server != null) {
//			logger.info("Node shutting down");
//			server.shutdown();
//		}
//	}

	private void moveDirectories() {
			File folder = new File(getStorageDir(), Pagerank.PATH_INTERIM1);
			ArrayList<File> files = new ArrayList<File>();
			for (File file : folder.listFiles()) {
				files.add(file);
			}
			folder = new File(getStorageDir(), Pagerank.PATH_INTERIM2);
			for (File file : folder.listFiles()) {
				files.add(file);
			}
			
			File newD = new File(getStorageDir(), Pagerank.PATH_DIFF);
			if(!newD.exists()) {
				newD.mkdir();
			}
			
			int count = 0;
			for (File file : files) {
				File newFile = new File(newD, file.getName() + count);
				FileUtil.copyFile(file, newFile);
				count++;
			}
	 }

	/**
	 * Initialize counts on this node
	 */
	private void updateDocumentCounts() {
		CrawlerDatabase db = CrawlerDatabase.getInstance();
		DocInfoDB docDb = db.getDocInfoDB();
//		File docDir = new File(getStorageDir(), Crawler.PATH_DOCUMENTS);
//		File[] docs = docDir.listFiles();
//		if(docs != null) {
//			mDocumentCount = docs.length;
//		}
		mDocumentCount = docDb.getDocumentCount();
		mImageCount = docDb.getImageCount();
		mVideoCount = docDb.getVideoCount();
	}

	private static int getIndexNumber(File dir) {
		if(!dir.exists()) {
			return -1;
		}
		File[] files = dir.listFiles(hiddenFileFilter);
		if(files == null) {
			return -1;
		}
		return files.length;
	}
	
	private static void mergeTempFiles(File dir) {
		File tempDir = new File(dir, TEMP_DIR);
		if(!tempDir.exists()) {
			return;
		}
		FileUtil.mergeFiles(tempDir, INPUT_FILE);
		File inputFile = new File(dir, INPUT_FILE);
		File tempInputFile = new File(tempDir, INPUT_FILE);
		FileUtil.mergeFile(inputFile, tempInputFile);
		FileUtil.deleteDir(tempDir);
	}
	
	public void deleteDir(File directory) {
		if (directory.isDirectory()) {
			if (directory.list().length == 0) {
				directory.delete();
			} else {
				// list all contents
				String files[] = directory.list();

				for (String temp : files) {
					File fileDelete = new File(directory, temp);
					deleteDir(fileDelete);
				}
				if (directory.list().length == 0) {
					directory.delete();
				}
			}
		} else {
			// if file then delete file
			directory.delete();
		}

	}
}
