package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters.Counter;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleOutputs;
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;

import edu.upenn.cis455.storage.CrawlerStorageManager;

public class MapReduceCrawler extends Configured implements Tool {
	
	public static final String TAG = MapReduceCrawler.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	public static final String CONF_HDFS_ROOT = "crawler.conf.hdfs";
	public static final String CONF_LOCAL_ROOT = "crawler.conf.local";
	public static final String CONF_SEED = "crawler.conf.seed";
	public static final String CONF_INITIAL = "crawler.conf.initial";
	public static final String CONF_LIMIT = "crawler.conf.limit";
	public static final String CONF_MAPPERS = "crawler.conf.mappers";
	public static final String CONF_REDUCERS = "crawler.conf.reducers";
	
	private static final String INPUT_DIR = "input";
	private static final String OUTPUT_DIR = "output";
	private static final String DOC_DB_DIR = "doc_db";
	private static final String DOCUMENT_DIR = "documents";
//	private static final String CRAWLER_DIR = "crawler";
	private static final String INDEXER_DIR = "indexer";
	
	static final String INDEXER_HTML_INPUT = "html";
	static final String INDEXER_IMAGE_INPUT = "image";
	static final String INDEXER_VIDEO_INPUT = "video";
	static final String PAGERANK_DIR = "pagerank";
	
	private int mNumCrawled = 0;
	private int mMaxNum = 1;
	
	private Path mInputPath;
	private Path mOutputPath;
	private File mIndexerHtmlDir;
	private File mIndexerImageDir;
	private File mIndexerVideoDir;
	private File mPagerankDir;
	
	private FileSystem fs;
	
	static enum CrawlerCounter {
		CRAWLED_COUNT;
	}
	
	private static PathFilter hiddenPathFilter = new PathFilter() {
		@Override
		public boolean accept(Path path) {
			return !path.getName().startsWith(".");
		}
	};
	
	private static FileFilter hiddenFileFilter = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return !pathname.isHidden();
		}
	};
	
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		String hdfsRootStr = conf.get(CONF_HDFS_ROOT);
		String localRootStr = conf.get(CONF_LOCAL_ROOT);
		
		if(hdfsRootStr == null) {
			System.err.println("HDFS root path not specified");
			return -1;
		}
		if(localRootStr == null) {
			System.err.println("Local root path not specified");
			return -1;
		}
		
		fs = FileSystem.get(conf);
		// Init hdfs paths
		Path hdfsRoot = new Path(hdfsRootStr);
		mInputPath = new Path(hdfsRoot, INPUT_DIR);
		mOutputPath = new Path(hdfsRoot, OUTPUT_DIR);
		if(!fs.exists(mInputPath)) {
			fs.mkdirs(mInputPath);
		}
		if(fs.exists(mOutputPath)) {
			fs.delete(mOutputPath, true);
		}
		
		// Init local paths
		File localRoot = new File(localRootStr);
		File dbDir = new File(localRoot, DOC_DB_DIR);
		File docDir = new File(localRoot, DOCUMENT_DIR);
		File indexerDir = new File(localRoot, INDEXER_DIR);
		mIndexerHtmlDir = new File(indexerDir, INDEXER_HTML_INPUT);
		mIndexerImageDir = new File(indexerDir, INDEXER_IMAGE_INPUT);
		mIndexerVideoDir = new File(indexerDir, INDEXER_VIDEO_INPUT);
		mPagerankDir = new File(localRoot, PAGERANK_DIR);
		
		if(!localRoot.exists()) {
			localRoot.mkdirs();
		}
		if(!docDir.exists()) {
			docDir.mkdir();
		}
		if(!indexerDir.exists()) {
			indexerDir.mkdir();
		}
		if(!mIndexerHtmlDir.exists()) {
			mIndexerHtmlDir.mkdir();
		}
		if(!mIndexerImageDir.exists()) {
			mIndexerImageDir.mkdir();
		}
		if(!mIndexerVideoDir.exists()) {
			mIndexerVideoDir.mkdir();
		}
		if(!mPagerankDir.exists()) {
			mPagerankDir.mkdir();
		}
		
		String seed = conf.get(CONF_SEED);
		int nMapper = conf.getInt(CONF_MAPPERS, -1);
		int nReducer = conf.getInt(CONF_REDUCERS, -1);
		mMaxNum = conf.getInt(CONF_LIMIT, 1);
		String initial = conf.get(CONF_INITIAL);
		File initialFile = null;
		if(initial != null) {
			initialFile = new File(initial);
			if(!initialFile.exists() || !initialFile.isFile()) {
				System.err.println("File " + initialFile.getAbsolutePath() + " does not exist.");
				initialFile = null;
			}
		}
		
		// Copy seed url into input
		Set<String> seedUrls = new HashSet<String>();
		if(seed != null) {
			seedUrls.add(seed);
		}
		if(initialFile != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(initialFile));
				String line;
				while((line = reader.readLine()) != null) {
					seedUrls.add(line);
				}
			} finally {
				if(reader != null) {
					reader.close();
				}
			}
		}
		
		if(!seedUrls.isEmpty()) {
			Path seedFile = new Path(mInputPath, "seed");
			clearInput();
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(new OutputStreamWriter(fs.create(seedFile)));
				for(String seedUrl : seedUrls) {
					writer.println(seedUrl);
				}
			} finally {
				if(writer != null) {
					writer.close();
				}
			}
			// Reset inputs
			clearDir(mIndexerHtmlDir);
			clearDir(mIndexerImageDir);
			clearDir(mIndexerVideoDir);
			clearDir(mPagerankDir);
		}
		
		CrawlerStorageManager.setup(dbDir.getAbsolutePath());
		
		mNumCrawled = 0;
		int i = 1;
		int remaining = mMaxNum;
		while(true) {
			if(fs.listStatus(mInputPath).length == 0) {
				System.out.println("Nothing more to crawl, exit on iteration " + i);
				break;
			}
			
			JobConf jobConf = new JobConf(conf, MapReduceCrawler.class);
			jobConf.setJobName("crawler");
//			conf.set("crawler.indexer.input", mIndexerHtmlFile.getAbsolutePath());
//			conf.set("crawler.pagerank.input", mPagerankFile.getAbsolutePath());
			jobConf.set("crawler.docstorage", docDir.getAbsolutePath());
			jobConf.set("crawler.remaining", String.valueOf(remaining));
			
			jobConf.setMapOutputKeyClass(Text.class);
			jobConf.setMapOutputValueClass(Text.class);
			jobConf.setOutputKeyClass(Text.class);
			jobConf.setOutputValueClass(NullWritable.class);
			
			jobConf.setMapperClass(CrawlerMap.class);
			jobConf.setReducerClass(CrawlerReduce.class);
			if(nMapper > 0) {
				jobConf.setNumMapTasks(nMapper);
			}
			if(nReducer > 0) {
				jobConf.setNumReduceTasks(nReducer);
			}
			
			jobConf.setInputFormat(TextInputFormat.class);
			jobConf.setOutputFormat(TextOutputFormat.class);
			
			FileInputFormat.setInputPaths(jobConf, mInputPath);
			FileOutputFormat.setOutputPath(jobConf, mOutputPath);
			MultipleOutputs.addNamedOutput(jobConf, INDEXER_HTML_INPUT,
					TextOutputFormat.class, Text.class, Text.class);
			MultipleOutputs.addNamedOutput(jobConf, INDEXER_IMAGE_INPUT,
					TextOutputFormat.class, Text.class, Text.class);
			MultipleOutputs.addNamedOutput(jobConf, INDEXER_VIDEO_INPUT,
					TextOutputFormat.class, Text.class, Text.class);
			MultipleOutputs.addNamedOutput(jobConf, PAGERANK_DIR,
					TextOutputFormat.class, Text.class, Text.class);
			
			System.out.println("Start crawl iteration " + i++);
			RunningJob job = JobClient.runJob(jobConf);
			
			// Transfer output back to input
			clearInput();
			moveOutput();
			
			Counter counter = job.getCounters().findCounter(CrawlerCounter.CRAWLED_COUNT);
			mNumCrawled += counter.getValue();
			remaining -= mNumCrawled;
			if(remaining <= 0) {
				System.out.println("Reaching crawling count limit, exit on iteration " + i);
				break;
			}
		}
		// Merge indexer and pagerank inputs
		System.out.println("Merging indexer and pagerank inputs...");
		mergeFiles(mIndexerHtmlDir);
		mergeFiles(mIndexerImageDir);
		mergeFiles(mIndexerVideoDir);
		mergeFiles(mPagerankDir);
		
		CrawlerStorageManager.getInstance().close();
		fs.close();
		
		System.out.println("Crawler finished. Total document crawled: " + mNumCrawled);
		return 0;
	}
	
	private void clearInput() {
		try {
			fs.delete(mInputPath, true);
			fs.mkdirs(mInputPath);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void clearDir(File dir) {
		deleteDir(dir);
		dir.mkdir();
	}
	
	private void deleteDir(File dir) {
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
	
	private void moveOutput() throws IOException {
		FileStatus[] files = fs.listStatus(mOutputPath);
		int index;
		Path destPath;
		File destDir;
		for(int i = 0; i < files.length; i++) {
			destPath = null;
			destDir = null;
			FileStatus status = files[i];
			Path file = status.getPath();
			String name = file.getName();
			if(name.startsWith("part") // Reducer output
					&& status.getLen() != 0) {
				destPath = mInputPath;
			} else if(name.startsWith(INDEXER_HTML_INPUT)) {
				destDir = mIndexerHtmlDir;
			} else if(name.startsWith(INDEXER_IMAGE_INPUT)) {
				destDir = mIndexerImageDir;
			} else if(name.startsWith(INDEXER_VIDEO_INPUT)) {
				destDir = mIndexerVideoDir;
			} else if(name.startsWith(PAGERANK_DIR)) {
				destDir = mPagerankDir;
			}
			if(destDir != null) {
				index = getIndexNumber(destDir);
				Path newFile = new Path(destDir.getAbsolutePath(), String.valueOf(index));
				fs.moveToLocalFile(file, newFile);
			} else if(destPath != null) {
				index = getIndexNumber(fs, destPath);
				Path newFile = new Path(destPath, String.valueOf(index));
				fs.rename(file, newFile);
			}
		}
		fs.delete(mOutputPath, true);
	}
	
	private static int getIndexNumber(FileSystem fs, Path path)
			throws IOException {
		if(!fs.exists(path)) {
			return -1;
		}
		FileStatus[] statuses = fs.listStatus(path, hiddenPathFilter);
		if(statuses == null) {
			return -1;
		}
		return statuses.length;
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
	
	private static void mergeFiles(File dir) {
		File[] fileArray = dir.listFiles();
		if(fileArray == null || fileArray.length <= 1) {
			return;
		}
		List<File> files = Arrays.asList(fileArray);
		Collections.sort(files);
		
		BufferedReader reader = null;
		PrintWriter writer = null;
		try {
			for(int i = 0; i < files.size(); i++) {
				File file = files.get(i);
				if(!file.isFile()) {
					continue;
				}
				if(file.isHidden()) {
					file.delete();
					continue;
				}
				if(writer == null) {
					writer = new PrintWriter(new FileWriter(file, true));
				} else {
					reader = new BufferedReader(new FileReader(file));
					String line;
					while((line = reader.readLine()) != null) {
						writer.println(line);
					}
					reader.close();
					file.delete();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if(writer != null) {
				writer.close();
			}
		}
	}
}
