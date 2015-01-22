package edu.upenn.cis455.webserver.servlet.app;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import edu.upenn.cis455.webserver.servlet.Context;
import edu.upenn.cis455.webserver.servlet.Session;

public class SessionManager {

	public static final String TAG = SessionManager.class.getSimpleName();
	private static MessageDigest md5;
	
	private Map<String, Session> mSessionStore = new HashMap<String, Session>();
	private Context mContext;
	private int mSessionTimeout = 60 * 60;
	
	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
		}
	}
	
	public SessionManager(Context context) {
		this.mContext = context;
	}

	public Context getContext() {
		return mContext;
	}
	
	public Session getSession(String id) {
		return mSessionStore.get(id);
	}
	
	public void add(Session session) {
		mSessionStore.put(session.getId(), session);
	}
	
	public void remove(Session session) {
		mSessionStore.remove(session.getId());
	}

	public int getSessionTimeout() {
		return mSessionTimeout / 60;
	}

	/**
	 * Set the default session timeout in minutes
	 * @param sessionTimeout
	 */
	public void setSessionTimeout(int sessionTimeout) {
		this.mSessionTimeout = sessionTimeout * 60;
	}
	
	public Session createSession() {
		return createSession(null);
	}
	
	public Session createSession(String sessionId) {
		Session session = new Session(this);
		session.setNew(true);
		session.setValid(true);
		session.setCreationTime(System.currentTimeMillis());
		session.setMaxInactiveInterval(mSessionTimeout);
		if(sessionId == null) {
			sessionId = generateSessionId();
		}
		session.setId(sessionId);
		return session;
	}
	
	private String generateSessionId() {
		return md5Str(UUID.randomUUID().toString()).toUpperCase();
	}
	
	private static String md5Str(String src) {
		if (src == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		try {
			md5.update(src.getBytes());
			for (byte b : md5.digest())
				sb.append(Integer.toString(b >>> 4 & 0xF, 16)).append(
						Integer.toString(b & 0xF, 16));
		} catch (Exception e) {

		}
		return sb.toString();
	}
}
