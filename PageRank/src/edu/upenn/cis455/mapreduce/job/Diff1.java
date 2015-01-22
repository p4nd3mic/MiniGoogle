package edu.upenn.cis455.mapreduce.job;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class Diff1 implements Job {
	public void map(String key, String value, Context context) {

	String val = value.toString();
	//separate key and value
	//String[] keyVal = val.split("\\t");
	//set the pattern. finding the rank
	String pattern = "rank=(\\d+.\\d+)";
	// Create a Pattern object
	Pattern r = Pattern.compile(pattern);
	// Now create matcher object.
	Matcher m = r.matcher(val);
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
			context.write("keyyy", strDiff);
		} catch (Exception e) {
			System.out.println("formatexception difmapper1");
		}

	
	}
	
	
}
