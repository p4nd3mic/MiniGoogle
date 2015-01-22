package edu.upenn.cis455.mapreduce.job;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class Finish implements Job {

	@Override
	public void map(String key, String value, Context context) {
		String val = value.toString();
		String pattern = "rank=(\\d+.\\d+)";
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);
		// Now create matcher object.
		Matcher m = r.matcher(val);
		if (m.find()) {
			String strRank = m.group(1);
			try {
				Double rank = Double.parseDouble(strRank);
				//emit rank -> v. Custom comparator will sort in descending order.
				context.write(key,strRank);
			} catch (Exception e) {
				System.out.println("numformatexception finishmapper");
			}
		}		
	}

	@Override
	public void reduce(String key, String[] values, Context context) {
		for (String value : values) {
			context.write(key, value);
		}		
	}

}
