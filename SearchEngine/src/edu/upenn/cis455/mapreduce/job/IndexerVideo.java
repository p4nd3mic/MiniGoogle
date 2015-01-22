package edu.upenn.cis455.mapreduce.job;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
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
import edu.upenn.cis455.mapreduce.MapReduceBase;
import edu.upenn.cis455.mapreduce.Mapper;
import edu.upenn.cis455.mapreduce.Reducer;
import edu.upenn.cis455.util.StringUtil;
import edu.upenn.cis455.utility.GenerateHitListForVideo;
import edu.upenn.cis455.utility.Stemmer;

public class IndexerVideo extends MapReduceBase implements Mapper, Reducer {
	
	public static final String TAG = IndexerVideo.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

	public void map(String key, String value, Context context) {

//		System.out.println("key: " + key + "  :  " + "value: " + value);
		JSONParser parser = new JSONParser();
		
		String jsonfile = value.toString();
		Object obj = null;
		try {
			obj = parser.parse(jsonfile);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}

		if (obj == null) {
			return;
		}

		JSONObject jsonObject = (JSONObject) obj;

		// pic' url, title, page url, type
		String url = key;
		String title = (String) jsonObject.get("title");
		String type = (String) jsonObject.get("type");

		// Get serialize String
		GenerateHitListForVideo generateHitList = new GenerateHitListForVideo(
				url, type, title);
		String serialStr = generateHitList.serialize();

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

		// VideoEntity operations
		boolean isExist = wrapper.isExistWordEntityForVideo(word);

		// VideoEntity operations
		if (isExist) {

			// Get invertedIndexList of this word first
			ArrayList<Integer> invertedIndexOldList = wrapper
					.getInvertedIndexListForVideo(word);
			ArrayList<Integer> invertedIndexNewList = new ArrayList<Integer>();

			HashMap<String, ArrayList<Integer>> videoUrlToIndex = wrapper
					.getVideoUrlToIndex(invertedIndexOldList);

			for (String value : values) {
				String[] partss = value.split("<<<\\|>>>");
				if(partss == null || partss.length != 2) {
					logger.error("Wrong partss length, value: " + value);
					continue;
				}
				String originalWord = partss[0];

				String[] parts = partss[1].split("<<<->>>");

//				System.out.println("url: " + parts[0] + " " + "type: "
//						+ parts[1] + " " + "title: " + parts[2]
//						+ "hitInfo: " + parts[3] + "TF: " + parts[4]);

				if(parts.length != 5){
					continue;
				}
				
				String url = parts[0];
				String type = parts[1];
				String title = parts[2];
				double TF = StringUtil.parseDouble(parts[4], -1);

				if(TF == -1){
					continue;
				}

				String[] hitsStr = parts[3].split("<<<:>>>");
				ArrayList<Hit> hitList = deserializeHelper(hitsStr);

				if (videoUrlToIndex.containsKey(url)) {
					ArrayList<Integer> tempList = videoUrlToIndex.get(url);

					for (Integer invertedIndexForVideo : tempList) {
						String origin = wrapper
								.getOriginalWordForVideo(invertedIndexForVideo);
						if (origin.equals(originalWord)) {
							boolean success = wrapper
									.updateInvertedIndexForVideoEntity(
											originalWord,
											invertedIndexForVideo, hitList, TF,
											url, title, type);
							// System.out
							// .println("invertedIndexEntity update success?: "
							// + success);
							break;
						}
					}
				} else {
					int invertedIndex = wrapper.addInvertedIndexForVideoEntity(
							originalWord, hitList, TF, url, title, type);
					invertedIndexNewList.add(invertedIndex);
				}

			}

			boolean success = wrapper.updateWordEntityForVideo(word,
					invertedIndexNewList);
//			System.out.println("update WordEnity success?: " + success);

		} else {

			ArrayList<Integer> invertedIndexList = new ArrayList<Integer>();

			for (String value : values) {
				String[] partss = value.split("<<<\\|>>>");
				if(partss == null || partss.length != 2) {
					logger.error("Wrong partss length, value: " + value);
					continue;
				}
				String originalWord = partss[0];

				String[] parts = partss[1].split("<<<->>>");

//				System.out.println("url: " + parts[0] + " " + "type: "
//						+ parts[1] + " " + "title: " + parts[2]
//						+ "hitInfo: " + parts[3] + "TF: " + parts[4]);
				
				if(parts.length != 5){
					continue;
				}
				
				String url = parts[0];
				String type = parts[1];
				String title = parts[2];
				
				double TF = StringUtil.parseDouble(parts[4], -1);

				if(TF == -1){
					continue;
				}

				String[] hitsStr = parts[3].split("<<<:>>>");
				ArrayList<Hit> hitList = deserializeHelper(hitsStr);

				int invertedIndex = wrapper.addInvertedIndexForVideoEntity(
						originalWord, hitList, TF, url, title, type);
				invertedIndexList.add(invertedIndex);
			}

			boolean success = wrapper.addWordEntityForVideo(word,
					invertedIndexList);
//			System.out.println("new WordEntity success?: " + success);
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
