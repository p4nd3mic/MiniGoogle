package edu.upenn.cis455.mapreduce;

public interface Mapper {

	void map(String key, String value, Context context);
}
