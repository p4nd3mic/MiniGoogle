package edu.upenn.cis455.mapreduce.job;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.MapReduceBase;
import edu.upenn.cis455.mapreduce.Mapper;
import edu.upenn.cis455.mapreduce.Reducer;

public class Iter extends MapReduceBase implements Mapper, Reducer {
	
	private static final Pattern pattern = Pattern.compile("rank=(\\d+.\\d+),count=(\\d+)");
	
	public void map(String key, String value, Context context) {

		String val = value.toString();
		// separate key and value
		//String[] keyVal = val.split("\\t");

			// emit the intial key value and the word original which will be
			// used to parse in reducer.
			context.write(key, "original=" + value);
			// separate the values by comma
			String[] vertices = value.split(",");

			double formula = 0.0;
			// set pattern to find count and rank

			// Now create matcher object.
			Matcher m = pattern.matcher(val);
			if (m.find()) {
				try {
					// get rank
					Double rank = Double.parseDouble(m.group(1));
					// get count
					Double count = Double.parseDouble(m.group(2));
					// for each friend vertex calculate formula and emit to
					// reducer.
					formula = (1 / count) * rank;
					String formulaString = "";
					for (int i = 0; i < vertices.length - 2; i++) {
						formulaString = String.valueOf(formula);
						// emit vertex and partial rank formula
//						System.out.println("writing in mapper");
//						System.out.println(vertices[i]+"   "+ formulaString);
						context.write(vertices[i], formulaString);
						//System.out.println(vertices[i]+"   form="+formulaString);
					}
				} catch (Exception e) {
					System.out.println("NumberFormatException111");
				}
			}
		
	}

	public void reduce(String key, String[] values, Context context) {
		String edge = "";


		
		// used to count number of friends

		String strValue = "";
		String linkCountRank = "";
		Double summation = 0.0;

		for (String value : values) {
//			System.out.println("entering reduce");
//			System.out.println("key="+key+"  v:"+value);
			strValue = value.toString();
			// check if this is the intermediate format. v -> v1,..vn,rank=,count=
			if (strValue.startsWith("original=")) {
				// linkCountRank is original format without word original
				linkCountRank = strValue.substring(9);
			//if this isn't intermediate format then it is partial formula.
			} else {
				try {
					summation = summation + Double.parseDouble(strValue);
				} catch (Exception e) {
					System.out.println("NumberFormatException");
				}
			}
		}
		// rank(next) = d + (1 - d)* (1/(num nodes connected to)) *rankj(current)
		Double rank = .15 + (1 - .15) * summation;
		// now convert rank to string and replace rank in v1,v2...ranki,counti
		String strRank = String.valueOf(rank);
		// case where vertex does not point to other vertices.
		if (linkCountRank.equals("") || linkCountRank.startsWith("rank")) {
			context.write(key, "rank=" + strRank);
		} else {
			//find and replace current rank
			String newLinkCountRank = linkCountRank.replaceAll(
					"rank=(\\d+.\\d+)", "rank=" + strRank);
			// emit back into intermediate format with new rank.
			context.write(key, newLinkCountRank);
		}
	}
}
