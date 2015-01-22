package edu.upenn.cis455.webserver.servlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class Config implements ServletConfig {

	private String mName;
	private ServletContext mContext;
	private Map<String, String> mInitParams;
	
	public Config(String name, ServletContext context) {
		this(name, context, null);
	}

	public Config(String name, ServletContext context,
			Map<String, String> initParams) {
		this.mName = name;
		this.mContext = context;
		this.mInitParams = initParams;
	}

	@Override
	public String getServletName() {
		return mName;
	}

	@Override
	public ServletContext getServletContext() {
		return mContext;
	}

	@Override
	public String getInitParameter(String name) {
		if(mInitParams == null) {
			return null;
		}
		return mInitParams.get(name);
	}

	@Override
	public Enumeration getInitParameterNames() {
		if(mInitParams != null) {
			return Collections.emptyEnumeration();
		}
		return Collections.enumeration(mInitParams.keySet());
	}

}
