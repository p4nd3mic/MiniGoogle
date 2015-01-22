package edu.upenn.cis455.mapreduce.job;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.MapReduceBase;
import edu.upenn.cis455.mapreduce.Mapper;
import edu.upenn.cis455.mapreduce.Reducer;
import edu.upenn.cis455.pagerank.Pagerank;

public class Diff1 extends MapReduceBase implements Mapper, Reducer {
	
	public static final String TAG = Diff1.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private static final Pattern pattern = Pattern.compile("rank=(\\d+.\\d+)");
	
	public void map(String key, String value, Context context) {

		String val = value.toString();
		// Now create matcher object.
		Matcher m = pattern.matcher(val);
		//if pattern then emit vertex with its rank
		if (m.find()) {
			//System.out.println("map");
			context.write(key,m.group(1));
		}
	}
	
	public void reduce(String key, String[] values, Context context) {
		int size = 0;
		Double diff = 0.0;

		for (String value : values) {
			size++;
			try {
				Double rank = Double.parseDouble(value.toString());
				// calculate difference
				diff = rank - diff;
			} catch (Exception e) {
				System.out.println("numformatexception in diffmapper1");
			}
		}
		diff = Math.abs(diff);
		try {
			//emit absolute value difference
			String strDiff = String.valueOf(diff);
			//System.out.println("map");
			//System.out.println("key="+key+"   value="+m.group(1));
			//output same key. and all the differences
			context.write(Pagerank.KEY_DIFF, strDiff);
		} catch (Exception e) {
			System.out.println("formatexception difmapper1");
		}
	}
}
