package edu.upenn.cis455.db;

import java.io.Closeable;

import org.apache.log4j.Logger;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import edu.upenn.cis455.client.HttpUrl;
import edu.upenn.cis455.util.Digest;

public class CrawlingHistoryDB implements Closeable {
	
	public static final String TAG = CrawlingHistoryDB.class.getSimpleName();
	public static final String UTF_8 = "UTF-8";
	private static Logger logger = Logger.getLogger(TAG);

	private static final String DB_CONTENT_FP = "content_fp_db";
	private static final String DB_URL = "url_db";
	
	private Environment mEnv;
	private Database mDocFpDb;
	private Database mUrlDb;
	
	private DatabaseConfig mConfig;
	
	public CrawlingHistoryDB(Environment env, DatabaseConfig config) {
		mEnv = env;
		mConfig = config;
		mDocFpDb = mEnv.openDatabase(null, DB_CONTENT_FP, mConfig);
		mUrlDb = mEnv.openDatabase(null, DB_URL, mConfig);
	}
	
	/**
	 * Do content seen test
	 * @param content
	 * @return
	 */
	public boolean isContentSeen(byte[] content) {
		boolean seen = false;
		String fp = Digest.sha1(content); // Generate finger print
		try {
			DatabaseEntry key = new DatabaseEntry(fp.getBytes(UTF_8));
			DatabaseEntry value = new DatabaseEntry();
			if(mDocFpDb.get(null, key, value, LockMode.READ_UNCOMMITTED)
					== OperationStatus.SUCCESS) {
				seen = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if(!seen) {
			addFingerPrint(fp);
		}
//		if(mFingerPrints.contains(fp)) { // Search in memory
//			seen = true;
//		} else if(mMerged && findFingerPrint(fp)) { // Search in file
//			seen = true;
//		} else { // Add to finger print database
//			addFingerPrint(fp);
//		}
		return seen;
	}
	
	public boolean isUrlSeen(HttpUrl url) {
		boolean seen = false;
		String curl = url.getCanonicalUrl();
		try {
			DatabaseEntry key = new DatabaseEntry(curl.getBytes(UTF_8));
			DatabaseEntry value = new DatabaseEntry();
			if(mUrlDb.get(null, key, value, LockMode.DEFAULT)
					== OperationStatus.SUCCESS) {
				seen = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if(!seen) {
			addUrl(curl);
		}
		return seen;
	}
	
	public void clear() {
		close();
		mEnv.removeDatabase(null, DB_CONTENT_FP);
		mEnv.removeDatabase(null, DB_URL);
		mDocFpDb = mEnv.openDatabase(null, DB_CONTENT_FP, mConfig);
		mUrlDb = mEnv.openDatabase(null, DB_URL, mConfig);
	}
	
	@Override
	public void close() {
		mDocFpDb.close();
		mUrlDb.close();
	}

	// Add a finger print into table, merge into file if necessary
	private void addFingerPrint(String fp) {
		try {
			DatabaseEntry key = new DatabaseEntry(fp.getBytes(UTF_8));
			DatabaseEntry value = new DatabaseEntry(new byte[] {});
			mDocFpDb.put(null, key, value);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void addUrl(String url) {
		try {
			DatabaseEntry key = new DatabaseEntry(url.getBytes(UTF_8));
			DatabaseEntry value = new DatabaseEntry(new byte[] {});
			mUrlDb.put(null, key, value);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
