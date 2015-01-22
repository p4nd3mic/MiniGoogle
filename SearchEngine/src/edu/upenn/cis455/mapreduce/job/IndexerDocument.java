package edu.upenn.cis455.mapreduce.job;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import edu.upenn.cis455.indexer.Indexer;
import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.MapReduceBase;
import edu.upenn.cis455.mapreduce.Mapper;
import edu.upenn.cis455.mapreduce.Reducer;
import edu.upenn.cis455.util.StringUtil;
import edu.upenn.cis455.utility.GenerateHitListDocument;
import edu.upenn.cis455.utility.JsoupHelper;
import edu.upenn.cis455.utility.Stemmer;

public class IndexerDocument extends MapReduceBase implements Mapper, Reducer {

	public static final String TAG = IndexerDocument.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private File archiveDir;
	private int maxSizeKb = -1;

	@Override
	public void setup(Map<String, String> params) {
		String stoPath = getStorageDir();
		archiveDir = new File(stoPath, Crawler.PATH_DOCUMENTS_DIR);
		String maxSizeStr = params.get(Indexer.PARAM_MAX_SIZE);
		if(maxSizeStr != null) {
			maxSizeKb = StringUtil.parseInt(maxSizeStr, -1);
		}
	}

	public void map(String key, String value, Context context) {
//		logger.debug("Indexing: " + key + ", value: " + value);
		JSONParser parser = new JSONParser();

		String jsonfile = value;
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

		String name = (String) jsonObject.get("filename");

		File filePath = new File(archiveDir, name);

		String html = null;

		if(!filePath.exists()) {
			return;
		}
		
		double bytes = filePath.length();
		double kilobytes = (bytes / 1024);
		
		if(maxSizeKb > 0 && kilobytes >= maxSizeKb){
			logger.info("Document " + key + " size: " + kilobytes + " exceeds maximum: "
					+ maxSizeKb + ", skip");
			return;
		}
		
		try {
			html = new String(Files.readAllBytes(Paths.get(filePath.getAbsolutePath())),
					StandardCharsets.UTF_8);
		} catch (IOException e1) {
			logger.error(e1.getMessage(), e1);
		}
		if(html == null) {
			return;
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
				if(partss == null || partss.length != 2) {
					logger.error("Wrong partss length, value: " + value);
					continue;
				}
				String originalWord = partss[0];

				String[] parts = partss[1].split("<<<->>>");

//				logger.debug("url: " + parts[0] + " " + "title: "
//						+ parts[1] + " " + "location: " + parts[2]
//						+ "hitInfo: " + parts[3] + "TF: " + parts[4]);
				
				if(parts.length != 5){
					continue;
				}
				
				String url = parts[0];
				String title = parts[1];
				String location = parts[2];
				double TF = StringUtil.parseDouble(parts[4], -1);

				if(TF == -1){
					continue;
				}
				
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
				if(partss == null || partss.length != 2) {
					logger.error("Wrong partss length, value: " + value);
					continue;
				}
				String originalWord = partss[0];

				String[] parts = partss[1].split("<<<->>>");

//				logger.info("url: " + parts[0] + " " + "title: "
//						+ parts[1] + " " + "location: " + parts[2]
//						+ "hitInfo: " + parts[3] + "TF: " + parts[4]);
				
				if(parts.length != 5){
					continue;
				}
				
				String url = parts[0];
				String title = parts[1];
				String location = parts[2];
				double TF = StringUtil.parseDouble(parts[4], -1);

				if(TF == -1){
					continue;
				}
				
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
