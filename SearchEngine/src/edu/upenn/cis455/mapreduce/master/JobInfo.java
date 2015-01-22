package edu.upenn.cis455.mapreduce.master;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.upenn.cis455.mapreduce.Mapper;
import edu.upenn.cis455.mapreduce.Reducer;

public class JobInfo {

	private String jobName;
	private Class<? extends Mapper> mapperClass;
	private Class<? extends Reducer> reducerClass;
	private String inputDir;
	private String outputDir;
	private int mappersCount = 1;
	private int reducersCount = 1;
	private Map<String, String> params = new LinkedHashMap<String, String>();
	
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public Class<? extends Mapper> getMapperClass() {
		return mapperClass;
	}
	public void setMapperClass(Class<? extends Mapper> mapperClass) {
		this.mapperClass = mapperClass;
	}
	public Class<? extends Reducer> getReducerClass() {
		return reducerClass;
	}
	public void setReducerClass(Class<? extends Reducer> reducerClass) {
		this.reducerClass = reducerClass;
	}
	public String getInputDir() {
		return inputDir;
	}
	public void setInputDir(String inputDir) {
		this.inputDir = inputDir;
	}
	public String getOutputDir() {
		return outputDir;
	}
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	public int getMappersCount() {
		return mappersCount;
	}
	public void setMappersCount(int mappersCount) {
		if(mappersCount < 1) {
			mappersCount = 1;
		}
		this.mappersCount = mappersCount;
	}
	public int getReducersCount() {
		return reducersCount;
	}
	public void setReducersCount(int reducersCount) {
		if(reducersCount < 1) {
			reducersCount = 1;
		}
		this.reducersCount = reducersCount;
	}
	public void addParameter(String name, String value) {
		params.put(name, value);
	}
	public String getParameter(String name) {
		return params.get(name);
	}
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(params.keySet());
	}
}
