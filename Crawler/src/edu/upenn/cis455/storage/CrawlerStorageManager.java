package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class CrawlerStorageManager {

	public static final String TAG = CrawlerStorageManager.class.getSimpleName();
//	public static final String CONTEXT_PARAM_NAME = "BDBstore";
	
	private static CrawlerStorageManager mInstance = null;
	
	// Database environment
	private Environment mEnv;
	// Storages
//	private UserStorage mUserStorage;
//	private ChannelStorage mChannelStorage;
	private DocumentStorage mDocumentStorage;
	private RobotStorage mRobotStorage;
	private CrawlingStorage mCrawlingStorage;

	private CrawlerStorageManager(File dbDir, boolean readOnly) {
		mEnv = createEnvironment(dbDir, readOnly);
		DatabaseConfig config = new DatabaseConfig();
		config.setReadOnly(readOnly);
		config.setAllowCreate(!readOnly); 
//		config.setTransactional(true);
		
//		mUserStorage = new UserStorage(mEnv, config);
//		mChannelStorage = new ChannelStorage(mEnv, config);
		mDocumentStorage = new DocumentStorage(mEnv, config);
		mRobotStorage = new RobotStorage(mEnv, config);
		mCrawlingStorage = new CrawlingStorage(mEnv, config);
	}
	
	public static CrawlerStorageManager getInstance() {
		if(mInstance == null) {
			throw new IllegalStateException("Setup not called");
		}
		return mInstance;
	}
	
	public static void setup(String dbHomePath) {
		mInstance = newInstance(dbHomePath);
	}
	
	private static CrawlerStorageManager newInstance(String dbEnvPath) {
		return newInstance(dbEnvPath, false);
	}
	
	private static CrawlerStorageManager newInstance(String rootPath, boolean readOnly) {
		File dir = new File(rootPath);
		if(!dir.exists()) {
			if(!dir.mkdirs()) {
				return null;
			}
		} else if(!dir.isDirectory()) {
			return null;
		}
		return new CrawlerStorageManager(dir, readOnly);
	}
	
//	public UserStorage getUserStorage() {
//		return mUserStorage;
//	}
//	
//	public ChannelStorage getChannelStorage() {
//		return mChannelStorage;
//	}

	public DocumentStorage getDocumentStorage() {
		return mDocumentStorage;
	}

	public RobotStorage getRobotStorage() {
		return mRobotStorage;
	}
	
	public CrawlingStorage getCrawlingStorage() {
		return mCrawlingStorage;
	}

	public void close() {
		// Close databases
//		if(mUserStorage != null) {
//			mUserStorage.close();
//		}
//		if(mChannelStorage != null) {
//			mChannelStorage.close();
//		}
		if(mDocumentStorage != null) {
			mDocumentStorage.close();
		}
		if(mRobotStorage != null) {
			mRobotStorage.close();
		}
		if(mCrawlingStorage != null) {
			mCrawlingStorage.close();
		}
		// Close environment
		if(mEnv != null) {
			mEnv.cleanLog();
			mEnv.close();
		}
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
}
