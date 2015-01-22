package edu.upenn.cis455.mapreduce;

public interface Reducer {

	void reduce(String key, String values[], Context context);
}
