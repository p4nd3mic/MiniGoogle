package edu.upenn.cis455.storage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import edu.upenn.cis455.storage.data.RobotExclusion;
import edu.upenn.cis455.storage.data.RobotExclusionBinding;
import edu.upenn.cis455.storage.data.RobotExclusion.Rule;

/**
 * Storage for Robots Exclusion Protocol Rules
 * @author Belmen
 *
 */
public class RobotStorage implements Closeable {

	public static final String TAG = RobotStorage.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	private static final String DB_ROBOT = "robot_db";
	public static final String UTF_8 = "UTF-8";
	
	private Map<String, RobotExclusion> mRobotMap = new HashMap<String, RobotExclusion>();
	private Database mRobotDb;
	private RobotExclusionBinding mBinding = new RobotExclusionBinding();
	
	public RobotStorage(Environment env, DatabaseConfig config) {
		mRobotDb = env.openDatabase(null, DB_ROBOT, config);
		// Load all rules into hash map
		loadRobots();
	}
	
	/**
	 * Get the Robots Exclusion Rules for a host name
	 * @param host
	 * @return
	 */
	public RobotExclusion getRobotExclusion(String host) {
		return mRobotMap.get(host);
	}
	
	/**
	 * Save robot exclusion rules into database
	 * @param robot
	 * @return
	 */
	public boolean saveRobotExclusion(RobotExclusion robot) {
		String host = robot.getHost();
		mRobotMap.put(host, robot);
		boolean result = false;
		try {
			DatabaseEntry key = new DatabaseEntry(host.getBytes(UTF_8));
			DatabaseEntry value = new DatabaseEntry();
			mBinding.objectToEntry(robot, value);
			if(mRobotDb.put(null, key, value) == OperationStatus.SUCCESS) {
				result = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
	
	/**
	 * Save the status that robot.txt for this host does not exist
	 * @param host
	 * @return
	 */
	public boolean saveNotExistRobot(String host) {
		mRobotMap.put(host, null);
		boolean result = false;
		try {
			DatabaseEntry key = new DatabaseEntry(host.getBytes(UTF_8));
			DatabaseEntry value = new DatabaseEntry(new byte[0]);
			if(mRobotDb.put(null, key, value) == OperationStatus.SUCCESS) {
				result = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
	
	/**
	 * Get the status that whether robots.txt for this host does not exist
	 * @param host
	 * @return
	 */
	public boolean isNotExistRobot(String host) {
		return mRobotMap.containsKey(host) && mRobotMap.get(host) == null;
	}
	
	/**
	 * Parse the downloaded robots.txt file and return robot exclusion rules
	 * @param host host name
	 * @param textContent content of robots.txt
	 * @return
	 */
	public RobotExclusion parseRobotTxt(String host, String textContent) {
		RobotExclusion robot = new RobotExclusion(host);
		Rule rule = null;
		List<String> disallowList = null;
		ByteArrayInputStream input = new ByteArrayInputStream(textContent.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		try {
			String line = null;
			while((line = reader.readLine()) != null) {
				if(line.isEmpty()) { // Empty line
					continue;
				}
				if(line.startsWith("#")) { // Comment
					continue;
				}
				int colon = line.indexOf(':');
				if(colon >= 0) {
					String key = line.substring(0, colon).trim();
					String value = line.substring(colon + 1).trim();
					if("User-agent".equalsIgnoreCase(key)) { // Rule for an agent
						if(rule != null) { // Add last rule
							addRule(robot, rule, disallowList);
						}
						rule = new Rule();
						rule.setUserAgent(value);
						disallowList = new ArrayList<String>();
					} else if("Disallow".equalsIgnoreCase(key)) {
						if(disallowList != null) {
//							String disallowed = value;
//							if(disallowed.length() > 1 && disallowed.endsWith("/")) {
//								disallowed = disallowed.substring(0, disallowed.length() - 1);
//							}
//							disallowList.add(disallowed);
							disallowList.add(value);
						}
					} else if("Crawl-delay".equalsIgnoreCase(key)) {
						if(rule != null) {
							rule.setCrawlDelay(Integer.parseInt(value));
						}
					}
				}
			}
			if(rule != null) { // Add last rule
				addRule(robot, rule, disallowList);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		if(robot != null) { // Save into database
			saveRobotExclusion(robot);
		}
		return robot;
	}
	
	/**
	 * Close this storage
	 */
	@Override
	public void close() {
		mRobotDb.close();
	}
	
	/**
	 * Load all robot rules in database to local hash map
	 */
	private void loadRobots() {
		Cursor cur = mRobotDb.openCursor(null, null);
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		try {
			while(cur.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				String host = new String(key.getData(), UTF_8);
				RobotExclusion robot = null;
				byte[] bytes = data.getData();
				if(bytes != null && bytes.length > 0) {
					robot = mBinding.entryToObject(data);
				}
				mRobotMap.put(host, robot);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			cur.close();
		}
	}

	private static void addRule(RobotExclusion robot, Rule rule, List<String> disallowList) {
		String[] disallow = null;
		if(disallowList != null) {
			disallow = new String[disallowList.size()];
			disallowList.toArray(disallow);
		}
		rule.setDisallow(disallow);
		robot.addRule(rule);
	}
}
