package edu.upenn.cis455.mapreduce.job;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.MapReduceBase;
import edu.upenn.cis455.mapreduce.Mapper;
import edu.upenn.cis455.mapreduce.Reducer;

//initial job. sets format
public class Init extends MapReduceBase implements Mapper, Reducer {
	
	public void map(String key, String value, Context context) {
		String out = value.trim() + ",";			
		context.write(key, out);
	}

	public void reduce(String key, String[] values, Context context) {
//		String edge = "";
		// used to count number of friends
		int count = 0;
		String combine = "";
		for (String val : values) {
			count++;
			// combine all vertices. get form vi = vj, vj+1,...vj+n
			combine = combine + val.toString();
		}
		// add an initial rank of 1.0
		combine = combine + "rank=1.0,";
		// add the count to the end
		combine = combine + "count=" + count;

		// emit vertex as key and friends plus count and rank as values
		// separated by commas

		context.write(key, combine);
	}
}
