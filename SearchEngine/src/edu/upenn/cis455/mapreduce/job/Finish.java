package edu.upenn.cis455.mapreduce.job;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.MapReduceBase;
import edu.upenn.cis455.mapreduce.Mapper;
import edu.upenn.cis455.mapreduce.Reducer;
import edu.upenn.cis455.pagerank.AddGetFromDB;
import edu.upenn.cis455.util.StringUtil;

public class Finish extends MapReduceBase implements Mapper, Reducer {

	private static final Pattern pattern = Pattern.compile("rank=(\\d+.\\d+)");

	@Override
	public void map(String key, String value, Context context) {
		String val = value.toString();
		Matcher m = pattern.matcher(val);
		if (m.find()) {
			String strRank = m.group(1);
			double rank = StringUtil.parseDouble(strRank, -1);
			if(rank != -1) {
				context.write(key, String.valueOf(rank));
			}
			
//			try {
//				Double rank = Double.parseDouble(strRank);
//				//emit rank -> v. Custom comparator will sort in descending order.
//				context.write(key,strRank);
//			} catch (Exception e) {
//				System.out.println("numformatexception finishmapper");
//			}
		}		
	}

	@Override
	public void reduce(String key, String[] values, Context context) {
//		File storedRanks = new File("/home/cis455/storage1/storedRanks");
//		AddGetFromDB ranks = new AddGetFromDB(dbPath);
		AddGetFromDB ranks = AddGetFromDB.getInstance();

		for (String value : values) {
			context.write(key, value);
			//add url->rank to db.
			ranks.addRankToDB(key, value);
		}		
	}

}
