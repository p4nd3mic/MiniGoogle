package edu.upenn.cis455.mapreduce.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.upenn.cis455.hit.AnchorHit;
import edu.upenn.cis455.hit.FancyHit;
import edu.upenn.cis455.hit.Hit;
import edu.upenn.cis455.hit.PlainHit;
import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;
import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.utility.GenerateHitListDocument;
import edu.upenn.cis455.utility.HashHelper;
import edu.upenn.cis455.utility.JsoupHelper;
import edu.upenn.cis455.utility.Stemmer;

public class MapReduceDocument implements Job {


	public void map(String key, String value, Context context) {
		
		System.out.println("key: " + key + "  :  " + "value: " + value);
		JSONParser parser = new JSONParser();

		String jsonfile = value;
		Object obj = null;
		try {
			obj = parser.parse(jsonfile);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (obj == null) {
			// TODO:
		}

		JSONObject jsonObject = (JSONObject) obj;

		String name = (String) jsonObject.get("filename");

		String filePath = "/Users/xuxu/Desktop/data_50/documents/" + name;

		String html = null;

		try {
			html = new String(Files.readAllBytes(Paths.get(filePath)),
					StandardCharsets.UTF_8);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// System.out.println("html: " + html);

		JsoupHelper soup = new JsoupHelper(html);

		String url = key;
		String title = soup.getHtmlTitle();
		String location = soup.getGeolocation();

		// Get serialize String
		GenerateHitListDocument generateHitList = new GenerateHitListDocument(
				html, url, title, location);
		String serialStr = generateHitList.serialize();

		// System.out.println("docId: " + docId);
		// System.out.println("location: " + location);
		// System.out.println("generateHitList: " + serialStr);

		// Ready for reduce
		String[] pieces = serialStr.split("<<<;>>>");

		for (String piece : pieces) {
			// System.out.println("piece: " + piece);
			String[] parts = piece.split("<<<\\|>>>");

			if (parts.length != 2) {
				continue;
			}

			Stemmer stemmer = null;
			char chs[] = parts[0].toCharArray();
			stemmer = new Stemmer();
			stemmer.add(chs, chs.length);
			stemmer.stem();
			String modifiedWord = stemmer.toString();

			context.write(modifiedWord, piece);
		}
	}

	public void reduce(String key, String[] values, Context context) {
		
		DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();
		String word = key;

		boolean isExist = wrapper.isExistWordEntity(word);

		// WordEntity operations
		if (isExist) {

			// Get invertedIndexList of this word first
			ArrayList<Integer> invertedIndexOldList = wrapper
					.getInvertedIndexList(word);
			ArrayList<Integer> invertedIndexNewList = new ArrayList<Integer>();
			// Get the (docId : InvertedIndex)
			HashMap<String, ArrayList<Integer>> urlToIndex = wrapper
					.getUrlToIndex(invertedIndexOldList);

			for (String value : values) {
				String[] partss = value.split("<<<\\|>>>");
				String originalWord = partss[0];

				String[] parts = partss[1].split("<<<->>>");

				System.out.println("url: " + parts[0] + " " + "title: "
						+ parts[1] + " " + "location: " + parts[2]
						+ "hitInfo: " + parts[3] + "TF: " + parts[4]);

				String url = parts[0];
				String title = parts[1];
				String location = parts[2];
				double TF = Double.valueOf(parts[4]);

				String[] hitsStr = parts[3].split("<<<:>>>");
				ArrayList<Hit> hitList = deserializeHelper(hitsStr);

				if (urlToIndex.containsKey(url)) {
					ArrayList<Integer> tempList = urlToIndex.get(url);
					for (Integer invertedIndex : tempList) {
						String origin = wrapper.getOriginalWord(invertedIndex);
						if (origin.equals(originalWord)) {
							boolean success = wrapper
									.updateInvertedIndexEntity(originalWord,
											invertedIndex, hitList, TF, url,
											title, location);
							// System.out
							// .println("invertedIndexEntity update success?: "
							// + success);
							break;
						}
					}
				} else {
					int invertedIndex = wrapper.addInvertedIndexEntity(
							originalWord, hitList, TF, url, title, location);
					invertedIndexNewList.add(invertedIndex);
				}

			}

			boolean success = wrapper.updateWordEntity(word,
					invertedIndexNewList);
			// System.out.println("update WordEnity success?: " + success);

		} else {

			ArrayList<Integer> invertedIndexList = new ArrayList<Integer>();

			for (String value : values) {
				String[] partss = value.split("<<<\\|>>>");
				String originalWord = partss[0];

				String[] parts = partss[1].split("<<<->>>");

				System.out.println("url: " + parts[0] + " " + "title: "
						+ parts[1] + " " + "location: " + parts[2]
						+ "hitInfo: " + parts[3] + "TF: " + parts[4]);

				String url = parts[0];
				String title = parts[1];
				String location = parts[2];
				double TF = Double.valueOf(parts[4]);

				String[] hitsStr = parts[3].split("<<<:>>>");
				ArrayList<Hit> hitList = deserializeHelper(hitsStr);

				int invertedIndex = wrapper.addInvertedIndexEntity(
						originalWord, hitList, TF, url, title, location);
				invertedIndexList.add(invertedIndex);
			}

			boolean success = wrapper.addWordEntity(word, invertedIndexList);
			// System.out.println("new WordEntity success?: " + success);
		}
	}

	public ArrayList<Hit> deserializeHelper(String[] hitsStr) {
		ArrayList<Hit> hitList = new ArrayList<Hit>();
		for (String hitStr : hitsStr) {
			Hit hit;
			if (hitStr.startsWith("0<#>")) {
				hit = new PlainHit();
				hit.deserialize(hitStr);
			} else if (hitStr.startsWith("1<#>")) {
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
