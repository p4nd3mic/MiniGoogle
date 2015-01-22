package edu.upenn.cis455.mapreduce;


public interface Context {

	void write(String key, String value);

	int getCounter(String name);
	
	int incrementCounter(String name, int value);
	
	MultipleContext getMultipleContext(String name);
}
