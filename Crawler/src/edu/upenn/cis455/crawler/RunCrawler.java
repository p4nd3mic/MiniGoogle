package edu.upenn.cis455.crawler;

import java.io.PrintStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;

public class RunCrawler {

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			help();
			return;
		}
		
		// Parse parameters
		Configuration conf = new Configuration();
		for(int i = 0; i < args.length; i++) {
			String arg = args[i];
			if("-hp".equals(arg)) {
				conf.set(MapReduceCrawler.CONF_HDFS_ROOT, args[++i]);
			} else if("-lp".equals(arg)) {
				conf.set(MapReduceCrawler.CONF_LOCAL_ROOT, args[++i]);
			} else if("-s".equals(arg)) {
				conf.set(MapReduceCrawler.CONF_SEED, args[++i]);
			} else if("-i".equals(arg)) {
				conf.set(MapReduceCrawler.CONF_INITIAL, args[++i]);
			} else if("-l".equals(arg)) {
				String argv = args[++i];
				int l = parseInt(argv, -1);
				if(l >= 1) {
					conf.setInt(MapReduceCrawler.CONF_LIMIT, l);
				} else {
					System.err.println(argv + " is not a valid parameter value for -l");
					return;
				}
			} else if("-m".equals(arg)) {
				String argv = args[++i];
				int m = parseInt(argv, -1);
				if(m >= 1) {
					conf.setInt(MapReduceCrawler.CONF_MAPPERS, m);
				} else {
					System.err.println(argv + " is not a valid parameter value for -m");
					return;
				}
			} else if("-r".equals(arg)) {
				String argv = args[++i];
				int r = parseInt(argv, -1);
				if(r >= 1) {
					conf.setInt(MapReduceCrawler.CONF_REDUCERS, r);
				} else {
					System.err.println(argv + " is not a valid parameter value for -r");
					return;
				}
			} else if("--help".equals(arg)) {
				help();
				return;
			} else {
				System.err.println("Unrecognized option " + arg
					+ ". Use --help to display available options.");
				return;
			}
		}
		if(conf.get(MapReduceCrawler.CONF_HDFS_ROOT) == null) {
			System.err.println("HDFS root path not specified");
			return;
		}
		if(conf.get(MapReduceCrawler.CONF_LOCAL_ROOT) == null) {
			System.err.println("Local data root path not specified");
			return;
		}
		
		int res = ToolRunner.run(conf, new MapReduceCrawler(), args);
		System.exit(res);
	}

	private static void help() {
		PrintStream ps = System.out;
		ps.println("Required parameters:");
		ps.println("  -hp\tRoot path of HDFS system for map reduce job");
		ps.println("  -lp\tRoot path of local storage");
		ps.println("Optional parameters:");
		ps.println("  -s\tSpecify the seed url to start crawling");
		ps.println("  -i\tSpecify the file containing lines of seed URLs");
		ps.println("  -l\tMaximum number of document to be crawled for each peer, default to 1");
		ps.println("  -m\tNumber of map workers, default to 1");
		ps.println("  -r\tNumber of reduce workers, default to 1");
		ps.println("If running without -s and -t parameter, the crawler would resume from last crawl");
	}
	
	private static int parseInt(String s, int def) {
		int v = def;
		try {
			v = Integer.parseInt(s);
		} catch (NumberFormatException e) {
		}
		return v;
	}
}
