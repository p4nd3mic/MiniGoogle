package edu.upenn.cis455.crawler;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import edu.upenn.cis455.client.HttpUrl;

public class CrawlerMap extends MapReduceBase
implements Mapper<LongWritable, Text, Text, Text> {

	private Text mHost = new Text();
	private Text mUrl = new Text();
	
	@Override
	public void map(LongWritable key, Text value,
			OutputCollector<Text, Text> output, Reporter report)
			throws IOException {
		HttpUrl url = HttpUrl.parseUrl(value.toString());
		if(url != null) {
			String host = url.getHost();
			if(host != null) {
				mHost.set(host);
				mUrl.set(url.getUrl());
				output.collect(mHost, mUrl);
			}
		}
	}
}
