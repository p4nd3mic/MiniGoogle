package edu.upenn.cis455.indexer;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MapReduceIndexer {

	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {
		private DatabaseWrapper wrapper = DBSingleton.getInstance()
				.getWrapper();
		private JSONParser parser = new JSONParser();

		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			String v = value.toString();

			String[] partss = v.split("\t");

			String url = partss[0];
			String jsonfile = partss[1];

			Object obj = null;
			try {
				obj = parser.parse(jsonfile);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			if(obj == null){
				// TODO?
			}
			
			JSONObject jsonObject = (JSONObject) obj;
			String name = (String) jsonObject.get("filename");
			
			String filePath = "/home/cloudera/Desktop/crawled_wiki/documents/" + name;
			
			String html = new Scanner(new File(filePath)).useDelimiter("\\Z").next();

//			System.out.println("html: " + html);

			JsoupHelper soup = new JsoupHelper(html);

			// Change to docId
			int docId = HashHelper.generateDocId(url);

			// Store into DocumentEntity (docId; url; title; location)
			String title = soup.getHtmlTitle();
			ArrayList<String> paragraphText = soup.getParagraphText();
			String location = soup.getGeolocation();

			if (location != null) {
				wrapper.addDocumentEntity(docId, url, title, paragraphText,
						location);
			} else {
				wrapper.addDocumentEntity(docId, url, title, paragraphText);
			}

			// Get serialize String
			GenerateHitList generateHitList = new GenerateHitList(html, url,
					docId);
			String serialStr = generateHitList.serialize();

			System.out.println("docId: " + docId);
			System.out.println("location: " + location);
			// System.out.println("generateHitList: " + serialStr);

			// Ready for reduce
			String[] pieces = serialStr.split(";");

			for (String piece : pieces) {
				// System.out.println("piece: " + piece);
				String[] parts = piece.split("\\|");

				Stemmer stemmer = null;
				char chs[] = parts[0].toCharArray();
				stemmer = new Stemmer();
				stemmer.add(chs, chs.length);
				stemmer.stem();
				String modifiedWord = stemmer.toString();

				HashSet<String> candidates = new HashSet<String>();
				candidates.add(parts[0]);
				candidates.add(modifiedWord);

				for (String str : candidates) {
					output.collect(new Text(str), new Text(parts[1]));
				}
			}
		}
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		private DatabaseWrapper wrapper = DBSingleton.getInstance()
				.getWrapper();

		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			String word = key.toString();

			boolean isExist = wrapper.isExistWordEntity(word);
			System.err.println("word: " + word);

			// WordEntity operations
			if (isExist) {

				// Get invertedIndexList of this word first
				ArrayList<Integer> invertedIndexOldList = wrapper
						.getInvertedIndexList(word);
				ArrayList<Integer> invertedIndexNewList = new ArrayList<Integer>();
				// Get the (docId : InvertedIndex)
				HashMap<Integer, Integer> docIdToIndex = wrapper
						.getDocIdToIndex(invertedIndexOldList);

				while (values.hasNext()) {
					String[] parts = values.next().toString().split("-");

					System.out.println("docId: " + parts[0] + " " + "hitInfo: "
							+ parts[1] + " " + "TF: " + parts[2]);

					int docId = Integer.valueOf(parts[0]);
					double TF = Double.valueOf(parts[2]);

					String[] hitsStr = parts[1].split(":");
					ArrayList<Hit> hitList = deserializeHelper(hitsStr);

					if (docIdToIndex.containsKey(docId)) {
						int invertedIndex = docIdToIndex.get(docId);
						boolean success = wrapper.updateInvertedIndexEntity(
								invertedIndex, hitList, TF);
						System.out
								.println("invertedIndexEntity update success?: "
										+ success);
					} else {
						int invertedIndex = wrapper.addInvertedIndexEntity(
								docId, hitList, TF);
						invertedIndexNewList.add(invertedIndex);
					}

				}

				boolean success = wrapper.updateWordEntity(word,
						invertedIndexNewList);
				System.out.println("update WordEnity success?: " + success);

			} else {

				ArrayList<Integer> invertedIndexList = new ArrayList<Integer>();

				while (values.hasNext()) {
					String[] parts = values.next().toString().split("-");

					System.out.println("docId: " + parts[0] + " " + "hitInfo: "
							+ parts[1] + " " + "TF: " + parts[2]);

					int docId = Integer.valueOf(parts[0]);
					double TF = Double.valueOf(parts[2]);

					// deserialize hitListStr and get hitList
					String[] hitsStr = parts[1].split(":");
					ArrayList<Hit> hitList = deserializeHelper(hitsStr);

					int invertedIndex = wrapper.addInvertedIndexEntity(docId,
							hitList, TF);
					invertedIndexList.add(invertedIndex);
				}

				boolean success = wrapper
						.addWordEntity(word, invertedIndexList);
				System.out.println("new WordEntity success?: " + success);
			}
		}

		public ArrayList<Hit> deserializeHelper(String[] hitsStr) {
			ArrayList<Hit> hitList = new ArrayList<Hit>();
			for (String hitStr : hitsStr) {
				Hit hit;
				if (hitStr.startsWith("0#")) {
					hit = new PlainHit();
					hit.deserialize(hitStr);
				} else if (hitStr.startsWith("1#")) {
					hit = new FancyHit();
					hit.deserialize(hitStr);
				} else {
					hit = new AnchorHit();
					hit.deserialize(hitStr);
				}
				hitList.add(hit);
			}
			return hitList;
		}
	}

	public static void main(String[] args) throws Exception {
		DBSingleton.setDbPath("/home/cloudera/Desktop/DB");

		JobConf conf = new JobConf(MapReduceIndexer.class);
		conf.setJobName("wordcount");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);

		DBSingleton.getInstance().closeBDBstore();

	}
}