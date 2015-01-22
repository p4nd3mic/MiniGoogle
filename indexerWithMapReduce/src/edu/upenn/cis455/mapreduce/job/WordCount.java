package edu.upenn.cis455.mapreduce.job;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class WordCount implements Job {

	public void map(String key, String value, Context context) {
		String[] words = value.split("\\W+");
		for (String word : words) {
			if(word.equals("")){
				continue;
			}
			//System.out.println("map: " + word);
			context.write(word, "1");
		}
	}

	public void reduce(String key, String[] values, Context context) {

		int sum = 0;
		for (String string : values) {
			sum++;
		}
		context.write(key, String.valueOf(sum));
	}

}
