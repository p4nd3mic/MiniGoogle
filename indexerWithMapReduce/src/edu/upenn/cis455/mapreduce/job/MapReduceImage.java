package edu.upenn.cis455.mapreduce.job;

import java.util.ArrayList;
import java.util.HashMap;

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
import edu.upenn.cis455.utility.GenerateHitListForImage;
import edu.upenn.cis455.utility.Stemmer;

public class MapReduceImage implements Job {

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

		// pic' url, description, page url, type
		String url = key;
		String pageUrl = (String) jsonObject.get("page");
		String type = (String) jsonObject.get("type");
		String description = (String) jsonObject.get("description");

		// Get serialize String
		GenerateHitListForImage generateHitList = new GenerateHitListForImage(
				url, pageUrl, type, description);
		String serialStr = generateHitList.serialize();

//		System.out.println("serialStr: " + serialStr);

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

		// ImageEntity operations
		boolean isExist = wrapper.isExistWordEntityForImage(word);

		// ImageEntity operations
		if (isExist) {

			// Get invertedIndexList of this word first
			ArrayList<Integer> invertedIndexOldList = wrapper
					.getInvertedIndexListForImage(word);
			ArrayList<Integer> invertedIndexNewList = new ArrayList<Integer>();

			HashMap<String, ArrayList<Integer>> imageUrlToIndex = wrapper
					.getImageUrlToIndex(invertedIndexOldList);

			for (String value : values) {
				String[] partss = value.split("<<<\\|>>>");
				String originalWord = partss[0];

				String[] parts = partss[1].split("<<<->>>");

				System.out
						.println("url: " + parts[0] + " " + "pageUrl: "
								+ parts[1] + " " + "type: " + parts[2]
								+ "desc: " + parts[3] + "hitInfo: " + parts[4]
								+ "TF: " + parts[5]);

				String url = parts[0];
				String pageUrl = parts[1];
				String type = parts[2];
				String description = parts[3];
				double TF = Double.valueOf(parts[5]);

				String[] hitsStr = parts[4].split("<<<:>>>");
				ArrayList<Hit> hitList = deserializeHelper(hitsStr);

				if (imageUrlToIndex.containsKey(url)) {
					ArrayList<Integer> tempList = imageUrlToIndex.get(url);
					for (Integer invertedIndexForImage : tempList) {
						String origin = wrapper
								.getOriginalWordForImage(invertedIndexForImage);
						if (origin.equals(originalWord)) {
							boolean success = wrapper
									.updateInvertedIndexForImageEntity(
											originalWord,
											invertedIndexForImage, hitList, TF,
											url, description, pageUrl, type);
							// System.out
							// .println("invertedIndexEntity update success?: "
							// + success);
							break;
						}
					}
				} else {
					int invertedIndex = wrapper.addInvertedIndexForImageEntity(
							originalWord, hitList, TF, url, description,
							pageUrl, type);
					invertedIndexNewList.add(invertedIndex);
				}

			}

			boolean success = wrapper.updateWordEntityForImage(word,
					invertedIndexNewList);
			System.out.println("update WordEnity success?: " + success);

		} else {

			ArrayList<Integer> invertedIndexList = new ArrayList<Integer>();

			for (String value : values) {
				String[] partss = value.split("<<<\\|>>>");
				String originalWord = partss[0];

				String[] parts = partss[1].split("<<<->>>");

				System.out
						.println("url: " + parts[0] + " " + "pageUrl: "
								+ parts[1] + " " + "type: " + parts[2]
								+ " desc: " + parts[3] + "hitInfo: " + parts[4]
								+ "TF: " + parts[5]);

				String url = parts[0];
				String pageUrl = parts[1];
				String type = parts[2];
				String description = parts[3];
				double TF = Double.valueOf(parts[5]);

				String[] hitsStr = parts[4].split("<<<:>>>");
				ArrayList<Hit> hitList = deserializeHelper(hitsStr);

				int invertedIndex = wrapper.addInvertedIndexForImageEntity(
						originalWord, hitList, TF, url, description, pageUrl,
						type);
				invertedIndexList.add(invertedIndex);
			}

			boolean success = wrapper.addWordEntityForImage(word,
					invertedIndexList);
			System.out.println("new WordEntity success?: " + success);
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
