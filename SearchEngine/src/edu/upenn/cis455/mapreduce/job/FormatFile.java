package edu.upenn.cis455.mapreduce.job;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.MapReduceBase;
import edu.upenn.cis455.mapreduce.Mapper;
import edu.upenn.cis455.mapreduce.Reducer;
import edu.upenn.cis455.pagerank.AddGetFromDB;
import edu.upenn.cis455.pagerank.Ranks;

public class FormatFile extends MapReduceBase implements Mapper, Reducer {
	
	public static final String TAG = FormatFile.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	public void map(String key, String value, Context context) {
	  	try {
		Type listType = new TypeToken<List<String>>() {}.getType();
		List<String> yourList = new Gson().fromJson(value, listType);
		AddGetFromDB ranks = AddGetFromDB.getInstance();
		if (yourList != null) {
			for (String s : yourList) {
//				System.out.println("adding "+key+"   to sink db.");
				ranks.addSinkToDB(key.trim());
	
				context.write(key.trim(), s.trim());
			}
		}
		} catch (JsonSyntaxException e) {
			logger.error(e.getMessage());
		}
	}
	
	public void reduce(String key, String[] values, Context context) {
		for (String s : values) {
			AddGetFromDB ranks = AddGetFromDB.getInstance();
			Ranks rank = ranks.getSinksObject(s);
			
			if (rank != null) {
				logger.info("rank.geturl = " + rank.getUrl());

				context.write(key, s);
			}/* else {
				System.out.println("rank is null");
			}*/
		}
	}
}
