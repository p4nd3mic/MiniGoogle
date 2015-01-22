package edu.upenn.cis455.mapreduce.worker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.upenn.cis455.client.HttpClient;
import edu.upenn.cis455.client.HttpGetRequest;
import edu.upenn.cis455.client.HttpRequest;
import edu.upenn.cis455.client.HttpResponse;
import edu.upenn.cis455.client.ResponseReader;
import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.MultipleContext;

public abstract class BaseContext implements Context {
	
	public static final String TAG = BaseContext.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

	protected File mOutputDir;
	private String mMasterHost = null;
	private int mMasterPort = -1;
	private HttpClient mHttpClient = new HttpClient();
	private ResponseReader mReader = new ResponseReader();
	private Map<String, MultipleContext> mMultiContexts =
			new HashMap<String, MultipleContext>();
	
	public BaseContext(File outputDir) {
		mOutputDir = outputDir;
	}
	
	public void setMaster(String host, int port) {
		mMasterHost = host;
		mMasterPort = port;
	}
	
	@Override
	public int getCounter(String name) {
		if(mMasterHost == null || mMasterPort == -1) {
			return -1;
		}
		String content = sendGetRequest(String.format("/counter?name=%s", name));
		int counter = parseInt(content, -1);
		return counter;
	}

	@Override
	public int incrementCounter(String name, int value) {
		if(mMasterHost == null || mMasterPort == -1) {
			return -1;
		}
		String content = sendGetRequest(String.format("/counter?name=%s&incr=%d", name, value));
		int counter = parseInt(content, -1);
		return counter;
	}

	@Override
	public MultipleContext getMultipleContext(String name) {
		MultipleContext mc = mMultiContexts.get(name);
		if(mc == null) {
			mc = new MultipleContext(mOutputDir, name);
			mMultiContexts.put(name, mc);
		}
		return mc;
	}
	
	public void close() {
		for(MultipleContext mc : mMultiContexts.values()) {
			mc.close();
		}
	}

	private String sendGetRequest(String path) {
		String content = null;
		StringBuilder sb = new StringBuilder("http://");
		sb.append(mMasterHost);
		if(mMasterPort != 80) {
			sb.append(':').append(mMasterPort);
		}
		sb.append(path);
		String url = sb.toString();
		HttpRequest request = new HttpGetRequest(url);
		HttpResponse response = null;
		try {
			response = mHttpClient.execute(request);
			byte[] bytes = mReader.readContent(response);
			content = new String(bytes);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if(response != null) {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
		return content;
	}
	
	private static int parseInt(String s, int defValue) {
		int value = defValue;
		if(s != null) {
			try {
				value = Integer.parseInt(s);
			} catch (NumberFormatException e) {
			}
		}
		return value;
	}
}
