package edu.upenn.cis455.webserver.servlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import edu.upenn.cis455.webserver.servlet.app.SessionManager;

public class Session implements HttpSession {

	private Map<String, Object> mAttributes = new HashMap<String, Object>();
	
	private long mCreationTime = 0L;
	private long mLastAccessedTime = mCreationTime;
	private int mMaxInactiveInterval = -1;
	private String mId;
	private boolean mIsNew = false;
	private boolean mIsValid = false;
	private SessionManager mManager;
	
	public Session(SessionManager manager) {
		this.mManager = manager;
	}
	
	@Override
	public long getCreationTime() {
		return mCreationTime;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public long getLastAccessedTime() {
		return mLastAccessedTime;
	}

	@Override
	public ServletContext getServletContext() {
		if(mManager == null) {
			return null;
		}
		return mManager.getContext();
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.mMaxInactiveInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		return this.mMaxInactiveInterval;
	}

	@Override
	@Deprecated
	public HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return mAttributes.get(name);
	}

	@Override
	@Deprecated
	public Object getValue(String name) {
		return mAttributes.get(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(mAttributes.keySet());
	}

	@Override
	@Deprecated
	public String[] getValueNames() {
		return null;
	}

	@Override
	public void setAttribute(String name, Object value) {
		mAttributes.put(name, value);
	}

	@Override
	@Deprecated
	public void putValue(String name, Object value) {
		mAttributes.put(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		mAttributes.remove(name);
	}

	@Override
	@Deprecated
	public void removeValue(String name) {
		mAttributes.remove(name);
	}
	
	@Override
	public void invalidate() {
		expires();
	}

	@Override
	public boolean isNew() {
		return mIsNew;
	}
	
	public void access() {
		mLastAccessedTime = System.currentTimeMillis();
	}

	public void setCreationTime(long creationTime) {
		this.mCreationTime = creationTime;
	}

	public void setLastAccessedTime(long lastAccessedTime) {
		this.mLastAccessedTime = lastAccessedTime;
	}

	public void setId(String id) {
		if(id != null && mManager != null) {
			mManager.remove(this);
		}
		
		this.mId = id;
		
		if(mManager != null) {
			mManager.add(this);
		}
	}

	public void setNew(boolean isNew) {
		this.mIsNew = isNew;
	}

	public boolean isValid() {
		if(!mIsValid) {
			return false;
		}
		
		// Check timeout
		if(mMaxInactiveInterval >= 0) {
			long now = System.currentTimeMillis();
			int idle = (int) ((now - mLastAccessedTime) / 1000L);
			if(idle >= mMaxInactiveInterval) {
				expires();
			}
		}
		return mIsValid;
	}
	
	public void setValid(boolean valid) {
		this.mIsValid = valid;
	}
	
	private void expires() {
		if(!mIsValid) {
			return;
		}
		setValid(false);
		if(mManager != null) {
			mManager.remove(this);
		}
	}
}
