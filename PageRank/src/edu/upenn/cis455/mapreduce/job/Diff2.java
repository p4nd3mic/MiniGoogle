package edu.upenn.cis455.mapreduce.job;


import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class Diff2 implements Job {
	
	public void map(String key, String value, Context context) {
	context.write(key, value);
	}
	
	public void reduce(String key, String[] values, Context context) {
		Double max = 0.0;
		Double temp = 0.0;
		String strMax = "";
		//loop through all values and output max
		for (String value : values) {
			try {
				strMax = value.toString();
				temp = Double.parseDouble(strMax);
				if (temp > max) {
					max = temp;
				}
			} catch (Exception e) {
				System.out.println("numformatError difReducer2");
			}
		}
		strMax = String.valueOf(max);
		context.write(strMax, "");
	}
}
