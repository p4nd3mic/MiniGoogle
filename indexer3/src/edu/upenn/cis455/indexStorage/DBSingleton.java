package edu.upenn.cis455.indexStorage;

import com.sleepycat.persist.EntityStore;


public class DBSingleton {
	private static DBSingleton instance = new DBSingleton();;
	private static String dbPath;
	private static EntityStore store;
	private static BDBstore db;
	private static DatabaseWrapper wrapper;

	private DBSingleton() {
		
	}

	public static synchronized DBSingleton getInstance() {
		if(wrapper != null){
			return instance;
		}
		if (dbPath != null) {
			db = new BDBstore(dbPath);
			store = db.getEntityStore();
			wrapper = new DatabaseWrapper(store);
			return instance;
		}else{
			return null;
		}
		
	}

	public static void setDbPath(String path) {
		dbPath = path;
	}

	public DatabaseWrapper getWrapper() {
		if (wrapper != null) {
			return wrapper;
		} else {
			return null;
		}
	}

	public void closeBDBstore() {
		if (db != null) {
			db.close();
		}
	}
}
