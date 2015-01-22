package edu.upenn.cis455.spellchecker;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class BDBstore {
	private Environment env;
	private EntityStore store;

	public BDBstore(String location) {
		File envHome = new File(location);
		// create new BerkeleyDB
		envHome.mkdirs();

		EnvironmentConfig configuration = new EnvironmentConfig();
		configuration.setAllowCreate(true);
//		configuration.setTransactional(true);
		configuration.setLocking(false);
		StoreConfig storeConfig = new StoreConfig();
		storeConfig.setAllowCreate(true);

		env = new Environment(envHome, configuration);
		store = new EntityStore(env, "EntityStore", storeConfig);
	}

	public EntityStore getEntityStore() {
		return store;
	}

	public Environment getEnv() {
		return env;
	}

	public void close() {
		if (store != null) {
			try {
				store.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing store: " + dbe.toString());
				System.exit(-1);
			}
		}

		if (env != null) {
			try {
				env.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing MyDbEnv: " + dbe.toString());
				System.exit(-1);
			}
		}
	}

	public void sync() {
		if(env != null) {
			env.sync();
		}
	}
}
