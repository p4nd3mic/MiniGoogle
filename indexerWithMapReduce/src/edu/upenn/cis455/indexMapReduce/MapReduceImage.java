//package edu.upenn.cis455.indexMapReduce;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.*;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.io.*;
//import org.apache.hadoop.mapred.*;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//import edu.upenn.cis455.hit.AnchorHit;
//import edu.upenn.cis455.hit.FancyHit;
//import edu.upenn.cis455.hit.Hit;
//import edu.upenn.cis455.hit.PlainHit;
//import edu.upenn.cis455.indexStorage.DBSingleton;
//import edu.upenn.cis455.indexStorage.DatabaseWrapper;
//import edu.upenn.cis455.utility.GenerateHitListForImage;
//import edu.upenn.cis455.utility.HashHelper;
//import edu.upenn.cis455.utility.Stemmer;
//
//public class MapReduceImage {
//
//	public static class Map extends MapReduceBase implements
//			Mapper<LongWritable, Text, Text, Text> {
//		private DatabaseWrapper wrapper = DBSingleton.getInstance()
//				.getWrapper();
//		private JSONParser parser = new JSONParser();
//
//		public void map(LongWritable key, Text value,
//				OutputCollector<Text, Text> output, Reporter reporter)
//				throws IOException {
//			String jsonfile = value.toString();
//			Object obj = null;
//			try {
//				obj = parser.parse(jsonfile);
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}
//
//			if (obj == null) {
//				// TODO:
//			}
//
//			JSONObject jsonObject = (JSONObject) obj;
//
//			// pic' url, description, page url, type
//			String url = (String) jsonObject.get("url");
//			String pageUrl = (String) jsonObject.get("page");
//			String type = (String) jsonObject.get("type");
//			String description = (String) jsonObject.get("description");
//
//			// Get serialize String
//			GenerateHitListForImage generateHitList = new GenerateHitListForImage(
//					url, pageUrl, type, description);
//			String serialStr = generateHitList.serialize();
//
//			System.out.println("serialStr: " + serialStr);
//
//			// Ready for reduce
//			String[] pieces = serialStr.split("<<<;>>>");
//
//			for (String piece : pieces) {
//				// System.out.println("piece: " + piece);
//				String[] parts = piece.split("<<<\\|>>>");
//
//				if (parts.length != 2) {
//					continue;
//				}
//
//				Stemmer stemmer = null;
//				char chs[] = parts[0].toCharArray();
//				stemmer = new Stemmer();
//				stemmer.add(chs, chs.length);
//				stemmer.stem();
//				String modifiedWord = stemmer.toString();
//
//				output.collect(new Text(modifiedWord), new Text(piece));
//			}
//		}
//	}
//
//	public static class Reduce extends MapReduceBase implements
//			Reducer<Text, Text, Text, Text> {
//		private DatabaseWrapper wrapper = DBSingleton.getInstance()
//				.getWrapper();
//
//		public void reduce(Text key, Iterator<Text> values,
//				OutputCollector<Text, Text> output, Reporter reporter)
//				throws IOException {
//
//			String word = key.toString();
//
//			boolean isExist = wrapper.isExistWordEntityForImage(word);
//
//			// WordEntity operations
//			if (isExist) {
//
//				// Get invertedIndexList of this word first
//				ArrayList<Integer> invertedIndexOldList = wrapper
//						.getInvertedIndexListForImage(word);
//				ArrayList<Integer> invertedIndexNewList = new ArrayList<Integer>();
//
//				HashMap<String, ArrayList<Integer>> imageUrlToIndex = wrapper
//						.getImageUrlToIndex(invertedIndexOldList);
//
//				while (values.hasNext()) {
//					String[] partss = values.next().toString()
//							.split("<<<\\|>>>");
//					String originalWord = partss[0];
//
//					String[] parts = partss[1].split("<<<->>>");
//
//					System.out.println("url: " + parts[0] + " " + "pageUrl: "
//							+ parts[1] + " " + "type: " + parts[2] + "desc: "
//							+ parts[3] + "hitInfo: " + parts[4] + "TF: "
//							+ parts[5]);
//
//					String url = parts[0];
//					String pageUrl = parts[1];
//					String type = parts[2];
//					String description = parts[3];
//					double TF = Double.valueOf(parts[5]);
//
//					String[] hitsStr = parts[4].split("<<<:>>>");
//					ArrayList<Hit> hitList = deserializeHelper(hitsStr);
//
//					if (imageUrlToIndex.containsKey(url)) {
//						ArrayList<Integer> tempList = imageUrlToIndex.get(url);
//						for (Integer invertedIndexForImage : tempList) {
//							String origin = wrapper
//									.getOriginalWordForImage(invertedIndexForImage);
//							if (origin.equals(originalWord)) {
//								boolean success = wrapper
//										.updateInvertedIndexForImageEntity(
//												originalWord,
//												invertedIndexForImage, hitList,
//												TF, url, description, pageUrl,
//												type);
//								// System.out
//								// .println("invertedIndexEntity update success?: "
//								// + success);
//								break;
//							}
//						}
//					} else {
//						int invertedIndex = wrapper
//								.addInvertedIndexForImageEntity(originalWord,
//										hitList, TF, pageUrl, description,
//										pageUrl, type);
//						invertedIndexNewList.add(invertedIndex);
//					}
//
//				}
//
//				boolean success = wrapper.updateWordEntityForImage(word,
//						invertedIndexNewList);
//				System.out.println("update WordEnity success?: " + success);
//
//			} else {
//
//				ArrayList<Integer> invertedIndexList = new ArrayList<Integer>();
//
//				while (values.hasNext()) {
//					String[] partss = values.next().toString()
//							.split("<<<\\|>>>");
//					String originalWord = partss[0];
//
//					String[] parts = partss[1].split("<<<->>>");
//
//					System.out.println("url: " + parts[0] + " " + "pageUrl: "
//							+ parts[1] + " " + "type: " + parts[2] + "desc: "
//							+ parts[3] + "hitInfo: " + parts[4] + "TF: "
//							+ parts[5]);
//
//					String url = parts[0];
//					String pageUrl = parts[1];
//					String type = parts[2];
//					String description = parts[3];
//					double TF = Double.valueOf(parts[5]);
//
//					String[] hitsStr = parts[4].split("<<<:>>>");
//					ArrayList<Hit> hitList = deserializeHelper(hitsStr);
//
//					int invertedIndex = wrapper.addInvertedIndexForImageEntity(
//							originalWord, hitList, TF, url, description,
//							pageUrl, type);
//					invertedIndexList.add(invertedIndex);
//				}
//
//				boolean success = wrapper.addWordEntityForImage(word,
//						invertedIndexList);
//				System.out.println("new WordEntity success?: " + success);
//			}
//		}
//
//		public ArrayList<Hit> deserializeHelper(String[] hitsStr) {
//			ArrayList<Hit> hitList = new ArrayList<Hit>();
//			for (String hitStr : hitsStr) {
//				Hit hit;
//				if (hitStr.startsWith("0<#>")) {
//					hit = new PlainHit();
//					hit.deserialize(hitStr);
//				} else if (hitStr.startsWith("1<#>")) {
//					hit = new FancyHit();
//					hit.deserialize(hitStr);
//				} else {
//					hit = new AnchorHit();
//					hit.deserialize(hitStr);
//				}
//				hitList.add(hit);
//			}
//			return hitList;
//		}
//	}
//
//	public static void main(String[] args) throws Exception {
//		DBSingleton.setDbPath("/home/cloudera/Desktop/DB");
//
//		JobConf conf = new JobConf(MapReduceImage.class);
//		conf.setJobName("MapReduceImage");
//
//		conf.setOutputKeyClass(Text.class);
//		conf.setOutputValueClass(Text.class);
//
//		conf.setMapperClass(Map.class);
//		conf.setCombinerClass(Reduce.class);
//		conf.setReducerClass(Reduce.class);
//
//		conf.setInputFormat(TextInputFormat.class);
//		conf.setOutputFormat(TextOutputFormat.class);
//
//		FileInputFormat.setInputPaths(conf, new Path(args[0]));
//		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
//
//		JobClient.runJob(conf);
//
//		DBSingleton.getInstance().closeBDBstore();
//		File directory = new File("output");
//		delete(directory);
//	}
//	
//	public static void delete(File file) throws IOException {
//
//		if (file.isDirectory()) {
//
//			// directory is empty, then delete it
//			if (file.list().length == 0) {
//
//				file.delete();
//				System.out.println("Directory is deleted : "
//						+ file.getAbsolutePath());
//
//			} else {
//
//				// list all the directory contents
//				String files[] = file.list();
//
//				for (String temp : files) {
//					// construct the file structure
//					File fileDelete = new File(file, temp);
//
//					// recursive delete
//					delete(fileDelete);
//				}
//
//				// check the directory again, if empty then delete it
//				if (file.list().length == 0) {
//					file.delete();
//					System.out.println("Directory is deleted : "
//							+ file.getAbsolutePath());
//				}
//			}
//
//		} else {
//			// if file, then delete it
//			file.delete();
//			System.out.println("File is deleted : " + file.getAbsolutePath());
//		}
//	}
//}