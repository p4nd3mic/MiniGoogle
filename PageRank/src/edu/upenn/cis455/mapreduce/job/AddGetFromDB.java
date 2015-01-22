package edu.upenn.cis455.mapreduce.job;



import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.w3c.dom.Document;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;


public class AddGetFromDB {

	private Environment env;
	private EntityStore store;
	private DataAccessor sda;
	private File envHome;
	//Open the indices
	public AddGetFromDB(File envHome) throws DatabaseException {
		this.envHome = envHome;		
	}

	
	public void setup() throws DatabaseException {
		System.out.println(envHome);
		EnvironmentConfig envConfig = new EnvironmentConfig();
		StoreConfig storeConfig = new StoreConfig();
		
		envConfig.setAllowCreate(true);
		storeConfig.setAllowCreate(true);
		if (!envHome.exists()) {
			envHome.mkdir();
		}
		env = new Environment(envHome,envConfig);
		store = new EntityStore(env, "EntityStore",storeConfig);
	}

	public void shutdown() throws DatabaseException {
		store.close();
		env.close();
	}
	
	public void addRankToDB(String url, String rank) throws DatabaseException {
		setup();
		
		sda = new DataAccessor(store);
		
		Ranks newRank = new Ranks();
		
		newRank.setUrl(url);
		newRank.setRank(rank);
		
		sda.RanksPIdx.put(newRank);
		
		shutdown();
}
	public Ranks getRanksObject(String key) throws DatabaseException {
		setup();
		
		sda = new DataAccessor(store);
		
		Ranks user = sda.RanksPIdx.get(key);
		if (user == null) System.out.println("its null");
		//System.out.println("key = " + doc.getdoc().toString());
		
		shutdown();
		
		return user;
	}	
}


