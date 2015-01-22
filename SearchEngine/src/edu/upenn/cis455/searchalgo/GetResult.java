package edu.upenn.cis455.searchalgo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.upenn.cis455.client.HttpClient;
import edu.upenn.cis455.client.HttpPostRequest;
import edu.upenn.cis455.client.HttpResponse;
import edu.upenn.cis455.client.ResponseReader;
import edu.upenn.cis455.client.post.UrlEncodedBody;
import edu.upenn.cis455.mapreduce.master.WorkerInfo;
import edu.upenn.cis455.mapreduce.worker.HashDivider;
import edu.upenn.cis455.util.StringUtil;

public class GetResult {
	
	public static final String TAG = GetResult.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

	private CombineResults cres = null;
	private String[] keywords = null;
	private String location = "";
	private List<WorkerInfo> workers;
	private int type = 0;
	
	private HttpClient mHttpClient = new HttpClient();
	private ResponseReader mReader = new ResponseReader();

	public GetResult(String searchText, String location,
			List<WorkerInfo> workers, int type, int docCount, int locationPrio) {
		keywords = searchText.split(" ");
		this.location = location;
		cres = new CombineResults(keywords, location, workers, docCount, type,locationPrio);
		this.workers = workers;
		this.type = type;
	}

	public ArrayList<ResultSet> getResults() {

		Map<String, String> jsonFileResults = new HashMap<String, String>();

		HashDivider divider = new HashDivider();
		divider.setIntervals(workers.size());
		
		String typeStr = null;
		switch (type) {
		case 0: typeStr = "document"; break;
		case 1: typeStr = "image"; break;
		case 2: typeStr = "video"; break;
		default: typeStr = "document"; break;
		}
		for (int i = 0; i < keywords.length; i++) {
			String keyword = keywords[i];
			int index = 0;
			try {
				index = divider.indexOf(keyword.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
			}
			WorkerInfo worker = null;;
			if(index < workers.size()) {
				worker = workers.get(index);
			}
			if(worker == null) {
				return new ArrayList<ResultSet>();
			}
			String tfIdf = getTfIdf(worker, keyword, typeStr);
			if(tfIdf != null) {
				jsonFileResults.put(keyword, tfIdf);
			} else {
				logger.error("Failed to get tfidf of keyword: "
					+ keyword + " from " + worker.toString() + ", index=" + index);
				// Search for other workers
				for(int j = 0; j < workers.size(); j++) {
					if(j == index) {
						continue;
					}
					worker = workers.get(j);
					tfIdf = getTfIdf(worker, keyword, typeStr);
					if(tfIdf != null) {
						logger.warn("Get tfidf of keyword: "
								+ keyword + " from " + worker.toString() + ", index=" + j);
						jsonFileResults.put(keyword, tfIdf);
						break;
					}
				}
			}
			
		}
		for (int i = 0; i < keywords.length; i++) {
			String currentFile=jsonFileResults.get(keywords[i]);
			if(currentFile==null)
				continue;
			else{
				if(type==0){
					cres.ParseJason(currentFile);
				}
				else{
					cres.ParseJasonMedia(currentFile);
				}
			}
		}

		return cres.sendResults();
	}

	public ArrayList<String> getAllStemmerWords() {
		return cres.allKeyCombination();
	}
	
	private String getTfIdf(WorkerInfo worker, String keyword, String type) {
		String jsonContent = null;
		Map<String, String> params = new HashMap<String, String>();
		params.put("keyword", keyword);
		params.put("type", type);
		HttpResponse response = null;
		try {
			response = sendPostToWorker(worker, "/gettfidf", params);
			if(response.getStatusCode() == 200) {
				jsonContent = mReader.readText(response);
			}
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
		return jsonContent;
	}

	private HttpResponse sendPostToWorker(WorkerInfo worker, String path,
			Map<String, String> params) throws Exception {
		StringBuilder sb = new StringBuilder("http://");
		sb.append(worker.host);
		if(worker.port != 80) {
			sb.append(':').append(worker.port);
		}
		sb.append(path);
		HttpPostRequest request = new HttpPostRequest(sb.toString());
		if(params != null) {
			request.setPostBody(new UrlEncodedBody(params));
		}
		return mHttpClient.execute(request);
	}
}
