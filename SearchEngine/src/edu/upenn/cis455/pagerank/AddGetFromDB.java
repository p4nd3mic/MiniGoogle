package edu.upenn.cis455.pagerank;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;


public class AddGetFromDB {

	private Environment env;
	private EntityStore store;
	private DataAccessor sda;
//	private File envHome;
	
	private static AddGetFromDB instance = null;
	
	public static void setup(File envHome) {
		instance = new AddGetFromDB(envHome);
	}
	
	public static AddGetFromDB getInstance() {
		return instance;
	}
	
	//Open the indices
	private AddGetFromDB(File envHome) throws DatabaseException {
//		this.envHome = envHome;
		init(envHome);
	}

	public void addRankToDB(String url, String rank) throws DatabaseException {
//		setup();
		
		sda = new DataAccessor(store);
		
		Ranks newRank = new Ranks();
		
		newRank.setUrl(url);
		newRank.setRank(rank);
		
		sda.RanksPIdx.put(newRank);
		
//		shutdown();
	}
	
	public void addSinkToDB(String url) throws DatabaseException {
//		setup();
		
		sda = new DataAccessor(store);
		
		Ranks newRank = new Ranks();
		
		newRank.setUrl(url);
		newRank.setRank("null");
		
		sda.SinksPIdx.put(newRank);
		
//		shutdown();
	}
	public Ranks getSinksObject(String key) throws DatabaseException {
//		setup();
		
		sda = new DataAccessor(store);
		
		Ranks user = sda.SinksPIdx.get(key);
//		if (user == null) System.out.println("its null");
		//System.out.println("key = " + doc.getdoc().toString());
		
//		shutdown();
		
		return user;
	}
	public Ranks getRanksObject(String key) throws DatabaseException {
//		setup();
		
		sda = new DataAccessor(store);
		
		Ranks user = sda.RanksPIdx.get(key);
//		if (user == null) System.out.println("its null");
		//System.out.println("key = " + doc.getdoc().toString());
		
//		shutdown();
		
		return user;
	}

	public void shutdown() throws DatabaseException {
		store.close();
		env.close();
	}
	
	public void sync() {
		env.sync();
	}

	private void init(File envHome) throws DatabaseException {
		if(!envHome.exists()) {
			envHome.mkdirs();
		}
		
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		
		envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		if (!envHome.exists()) {
			envHome.mkdir();
		}
		env = new Environment(envHome,envConfig);
		store = new EntityStore(env, "EntityStore", storeConfig);
	}	
}