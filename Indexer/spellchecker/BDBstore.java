package edu.upenn.cis455.spellchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

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
		StoreConfig storeConfig = new StoreConfig();
		configuration.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		configuration.setTransactional(true);

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

	// Test
	public static void main(String[] args) throws Exception {
		

		// Generate sequence
		// store.setSequenceConfig("testSequence",
		// SequenceConfig.DEFAULT.setAllowCreate(true));
		// Sequence seq = store.getSequence("testSequence");
		// System.out.println("seq: " + seq.get(null, 1));
		// System.out.println("seq: " + seq.get(null, 1));
	}
}
