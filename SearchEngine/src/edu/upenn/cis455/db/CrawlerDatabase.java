package edu.upenn.cis455.db;

import java.io.File;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class CrawlerDatabase {

	public static final String TAG = CrawlerDatabase.class.getSimpleName();
//	public static final String CONTEXT_PARAM_NAME = "BDBstore";
	
	private static CrawlerDatabase mInstance = null;
	
	// Database environment
	private Environment mEnv;
	// Storages
	private DocInfoDB mDocInfoDB;
	private RobotRulesDB mRobotRulesDB;
	private CrawlingHistoryDB mCrawlingHistoryDB;

	private CrawlerDatabase(File dbDir, boolean readOnly) {
		mEnv = createEnvironment(dbDir, readOnly);
		DatabaseConfig config = new DatabaseConfig();
		config.setReadOnly(readOnly);
		config.setAllowCreate(!readOnly); 
//		config.setTransactional(true);
		
		mDocInfoDB = new DocInfoDB(mEnv, config);
		mRobotRulesDB = new RobotRulesDB(mEnv, config);
		mCrawlingHistoryDB = new CrawlingHistoryDB(mEnv, config);
	}
	
	public static boolean isSetup() {
		return mInstance != null;
	}
	
	public static CrawlerDatabase getInstance() {
//		if(mInstance == null) {
//			throw new IllegalStateException("Setup not called");
//		}
		return mInstance;
	}
	
	public static void setup(String dbHomePath) {
		mInstance = newInstance(dbHomePath);
	}
	
	private static CrawlerDatabase newInstance(String dbEnvPath) {
		return newInstance(dbEnvPath, false);
	}
	
	private static CrawlerDatabase newInstance(String rootPath, boolean readOnly) {
		File dir = new File(rootPath);
		if(!dir.exists()) {
			if(!dir.mkdirs()) {
				return null;
			}
		} else if(!dir.isDirectory()) {
			return null;
		}
		return new CrawlerDatabase(dir, readOnly);
	}

	private static Environment createEnvironment(File envHome, boolean readOnly) {
			EnvironmentConfig config = new EnvironmentConfig();
			config.setReadOnly(readOnly);
			config.setAllowCreate(!readOnly);
	//		config.setTransactional(true);
			config.setLocking(false);
			Environment env = new Environment(envHome, config);
			return env;
		}

	public DocInfoDB getDocInfoDB() {
		return mDocInfoDB;
	}

	public RobotRulesDB getRobotRulesDB() {
		return mRobotRulesDB;
	}
	
	public CrawlingHistoryDB getCrawlingHistoryDB() {
		return mCrawlingHistoryDB;
	}
	
	public void sync() {
		mEnv.sync();
	}

	public void close() {
		// Close databases
		if(mDocInfoDB != null) {
			mDocInfoDB.close();
		}
		if(mRobotRulesDB != null) {
			mRobotRulesDB.close();
		}
		if(mCrawlingHistoryDB != null) {
			mCrawlingHistoryDB.close();
		}
		// Close environment
		if(mEnv != null) {
			mEnv.cleanLog();
			mEnv.close();
		}
	}
}
