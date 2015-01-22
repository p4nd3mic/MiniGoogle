package edu.upenn.cis455.searchalgo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.upenn.cis455.client.HttpClient;
import edu.upenn.cis455.client.HttpPostRequest;
import edu.upenn.cis455.client.HttpResponse;
import edu.upenn.cis455.client.ResponseReader;
import edu.upenn.cis455.client.post.DataBody;
import edu.upenn.cis455.mapreduce.master.WorkerInfo;
import edu.upenn.cis455.mapreduce.worker.HashDivider;
import edu.upenn.cis455.util.StringUtil;

public class CombineResults {

	public static final String TAG = CombineResults.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

	String keywords[] = null;
	String startingKeywords[] = null;
	String location = "";
	JSONParser parser = new JSONParser();
	private List<WorkerInfo> workers;
	private int mDocCount;
	private int type = 0;
	private int locationPrio = 0;

	protected ArrayList<SingleKeyResult> keyResults = new ArrayList<SingleKeyResult>();
	Map<String, Double> Score = new HashMap<String, Double>();
	HashMap<String, ResultSet> rankingResults = new HashMap<String, ResultSet>();
	ArrayList<String> DocURLs = new ArrayList<String>();
	HashMap<String[], ArrayList<String>> KeyAndCommonURL = new HashMap<String[], ArrayList<String>>();
	HashMap<String, Double> SummaryChangedUrls = new HashMap<String, Double>();
	ArrayList<String> TitleUrlMatched = new ArrayList<String>();

	// for storing scores from page rank response
	HashMap<String, Double> PageRankScore = new HashMap<String, Double>();

	private HttpClient mHttpClient = new HttpClient();
	private ResponseReader mReader = new ResponseReader();

	// public CombineResults() {
	// }

	public CombineResults(String keywords[], String location,
			List<WorkerInfo> workers, int docCount, int type, int locationPrio) {
		this.keywords = new String[keywords.length];
		System.arraycopy(keywords, 0, this.keywords, 0, keywords.length);
		this.location = location;
		this.workers = workers;
		this.mDocCount = docCount;
		this.type = type;
		this.locationPrio = locationPrio;
	}

	public boolean ParseJasonMedia(String JsonFile) {
		if (JsonFile == null) {
			logger.error("JsonFile is null");
			return false;
		}

		SingleKeyResult res = new SingleKeyResult();
		try {
			Object ob = parser.parse(JsonFile);

			JSONObject jsonObject = (JSONObject) ob;

			JSONArray results = (JSONArray) jsonObject.get("results");

			String keyword = (String) jsonObject.get("keyword");
			res.setKeyword(keyword);

			// Calculate IDF
			double numerator = 0;
			Double idfNumerator = (Double) jsonObject.get("numerator");
			if (idfNumerator != null) {
				numerator = idfNumerator;
			}
			double idf = Math.log10(1.0 * mDocCount / (1 + numerator));
			res.setIdf(idf);

			for (Object o1 : results) {
				DocEntry docentry = new DocEntry();
				JSONObject obj1 = (JSONObject) o1;

				String title = "";

				// for image, description is regarded as title here
				if (type == 1) {
					title = (String) obj1.get("description");
					if (title != null)
						docentry.setTitle(title);

					String filetype = (String) obj1.get("type");
					if (filetype != null)
						docentry.setFileType(filetype);

					String pageUrl = (String) obj1.get("pageUrl");
					if (pageUrl != null)
						docentry.setPageUrl(pageUrl);
				}

				// for video, get title
				else if (type == 2) {
					title = (String) obj1.get("title");
					if (title != null)
						docentry.setTitle(title);
				}

				String location = (String) obj1.get("location");
				if (location != null)
					docentry.setLocation(location);

				Double tf = (Double) obj1.get("tf");
				if (tf != null)
					docentry.setTf(tf);

				String originalWord = (String) obj1.get("originalWord");
				if (originalWord != null)
					docentry.setOriginalWord(originalWord);

				String url = (String) obj1.get("url");
				if (url != null)
					docentry.setUrl(url);

				JSONArray positions = (JSONArray) obj1.get("positions");

				for (Object o2 : positions) {
					JSONObject obj2 = (JSONObject) o2;
					Long position = (Long) obj2.get("position");
					String excerpt = title;
					// Long type = (Long) obj2.get("type");
					PlainHit phit = new PlainHit(position, excerpt);
					docentry.PlainHitList.add(phit);
				}
				res.addDocEntry(docentry.originalWord, docentry);
			}
			keyResults.add(res);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean ParseJason(String JsonFile) {
		if (JsonFile == null) {
			logger.error("JsonFile is null");
			return false;
		}

		SingleKeyResult res = new SingleKeyResult();
		try {
			Object ob = parser.parse(JsonFile);

			JSONObject jsonObject = (JSONObject) ob;

			JSONArray results = (JSONArray) jsonObject.get("results");

			String keyword = (String) jsonObject.get("keyword");
			res.setKeyword(keyword);

			// Calculate IDF
			double numerator = 0;
			Double idfNumerator = (Double) jsonObject.get("numerator");
			if (idfNumerator != null) {
				numerator = idfNumerator;
			}
			double idf = Math.log10(1.0 * mDocCount / (1 + numerator));
			res.setIdf(idf);

			for (Object o1 : results) {
				DocEntry docentry = new DocEntry();
				JSONObject obj1 = (JSONObject) o1;

				String title = (String) obj1.get("title");
				if (title != null)
					docentry.setTitle(title);

				String location = (String) obj1.get("location");
				if (location != null)
					docentry.setLocation(location);

				Double tf = (Double) obj1.get("tf");
				if (tf != null)
					docentry.setTf(tf);

				String originalWord = (String) obj1.get("originalWord");
				if (originalWord != null)
					docentry.setOriginalWord(originalWord);

				String url = (String) obj1.get("url");
				if (url != null)
					docentry.setUrl(url);

				JSONArray positions = (JSONArray) obj1.get("positions");

				for (Object o2 : positions) {
					JSONObject obj2 = (JSONObject) o2;
					Long position = (Long) obj2.get("position");
					String excerpt = (String) obj2.get("excerpt");
					Long subtype = (Long) obj2.get("subtype");
					Long type = (Long) obj2.get("type");

					if (type == 0) {
						PlainHit phit = new PlainHit(position, excerpt);
						docentry.PlainHitList.add(phit);
					} else if (type == 1) {
						FancyHit fhit = new FancyHit(subtype, position);
						docentry.FancyHitList.add(fhit);
					} else if (type == 2) {
						AnchorHit ahit = new AnchorHit(position);
						docentry.AnchorHitList.add(ahit);
					}
				}
				res.addDocEntry(docentry.originalWord, docentry);
			}
			keyResults.add(res);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ArrayList<String> allStemmerWords() {
		ArrayList<String> stemmerwords = new ArrayList<String>();
		for (int i = 0; i < keyResults.size(); i++) {
			SingleKeyResult skr = keyResults.get(i);
			for (String s : skr.wordDocEntries.keySet()) {
				if (!s.equals(startingKeywords[i]))
					stemmerwords.add(s);
			}
		}
		return stemmerwords;
	}

	public ArrayList<String> allKeyCombination() {

		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < keyResults.size(); i++) {
			SingleKeyResult singlekeyres = keyResults.get(i);
			for (String s : singlekeyres.wordDocEntries.keySet())
				result.add(s);
		}
		return result;
	}

	private ArrayList<String> stemmerWordsForOneKeyResult(
			SingleKeyResult curKeyResult) {
		ArrayList<String> stemmerwords = new ArrayList<String>();
		for (String s : curKeyResult.wordDocEntries.keySet()) {
			if (!s.equals(curKeyResult.keyword)) {
				stemmerwords.add(s);
			}
		}
		return stemmerwords;
	}

	public boolean containsURL(ArrayList<DocEntry> docs, String url) {
		for (int i = 0; i < docs.size(); i++) {
			if (docs.get(i).url.equals(url))
				return true;
		}
		return false;
	}

	public ArrayList<String> getCommonDocURL(ArrayList<String> result1,
			ArrayList<String> result2) {
		ArrayList<String> commonDocURL = new ArrayList<String>();
		for (int i = 0; i < result1.size(); i++) {
			String url = result1.get(i);
			if (result2.contains(url))
				commonDocURL.add(url);
		}
		return commonDocURL;
	}

	public ArrayList<SingleKeyResult> ArrayCopy() {
		ArrayList<SingleKeyResult> copy = new ArrayList<SingleKeyResult>();
		for (int i = 0; i < keyResults.size(); i++) {
			copy.add(keyResults.get(i));
		}
		return copy;
	}

	public ArrayList<String> getResultDocsCommonUrl(String[] keywords,
			ArrayList<SingleKeyResult> keyResults) {

		ArrayList<String> currentDocURL = new ArrayList<String>();

		if (keyResults == null || keyResults.size() == 0) {
			return currentDocURL;
		}
		ArrayList<DocEntry> DocEntry = keyResults.get(0).wordDocEntries
				.get(keywords[0]);
		if (DocEntry == null)
			return new ArrayList<String>();

		for (int i = 0; i < DocEntry.size(); i++)
			currentDocURL.add(DocEntry.get(i).url);

		for (int i = 1; i < keyResults.size(); i++) {
			ArrayList<String> firstURLSet = new ArrayList<String>();
			for (int j = 0; j < currentDocURL.size(); j++)
				firstURLSet.add(currentDocURL.get(j));

			ArrayList<String> secondURLSet = new ArrayList<String>();
			DocEntry = keyResults.get(i).wordDocEntries.get(keywords[i]);
			if (DocEntry == null)
				return new ArrayList<String>();

			for (int j = 0; j < DocEntry.size(); j++)
				secondURLSet.add(DocEntry.get(j).url);
			currentDocURL = getCommonDocURL(firstURLSet, secondURLSet);
		}
		return currentDocURL;

	}

	public void addStemmerwordScore(int currentKeyPos, String[] keyArr) {

		if (currentKeyPos == keyArr.length) {
			boolean originalText = true;
			for (int i = 0; i < keyArr.length; i++) {
				if (!keyArr[i].equals(startingKeywords[i])) {
					originalText = false;
					break;
				}
			}
			if (!originalText) {
				ArrayList<String> tempDoc = getResultDocsCommonUrl(keyArr,
						keyResults);
				if (tempDoc.size() == 0)
					return;
				KeyAndCommonURL.put(keyArr, tempDoc);
				for (int i = 0; i < tempDoc.size(); i++) {
					if (!DocURLs.contains(tempDoc.get(i)))
						DocURLs.add(tempDoc.get(i));

					if (Score.containsKey(tempDoc.get(i))) {
						double prevRank = Score.get(tempDoc.get(i));
						prevRank += ((0 + 0.5) + (1 + 0.5)) * 0.05;
						Score.put(tempDoc.get(i), prevRank);
					} else {
						double Rank = ((0 + 0.5) + (1 + 0.5)) * 0.05;
						Score.put(tempDoc.get(i), Rank);
					}
				}
			}
		} else {

			if (keyResults.get(currentKeyPos).wordDocEntries.size() == 1) {
				String[] newKeyArr = new String[keyArr.length];
				System.arraycopy(keyArr, 0, newKeyArr, 0, keyArr.length);
				newKeyArr[currentKeyPos] = startingKeywords[currentKeyPos];
				addStemmerwordScore(currentKeyPos + 1, newKeyArr);
			} else {
				for (String s : keyResults.get(currentKeyPos).wordDocEntries
						.keySet()) {
					String[] newKeyArr = new String[keyArr.length];
					System.arraycopy(keyArr, 0, newKeyArr, 0, keyArr.length);
					newKeyArr[currentKeyPos] = s;
					addStemmerwordScore(currentKeyPos + 1, newKeyArr);
				}
			}

		}
	}

	public boolean isOrigin(String s) {
		for (int i = 0; i < startingKeywords.length; i++) {
			if (s.equals(startingKeywords[i]))
				return true;
		}
		return false;
	}

	public int BoolToInt(boolean f) {
		if (f)
			return 1;
		else
			return 0;
	}

	private boolean HasIndexFile(String keyword) {
		for (int i = 0; i < keyResults.size(); i++) {
			if (keyResults.get(i).keyword.equals(keyword))
				return true;
		}
		return false;
	}

	private int getKeywordPosition(int startPos, String keyword) {
		int i = startPos;
		for (i = startPos; i < keywords.length; i++) {
			if (keywords[i].equals(keyword))
				break;
		}
		if (i == keywords.length)
			return -1;
		else
			return i;
	}

	public void getDocURLResults() {

		// select all original keywords with index file
		if (keyResults.size() == keywords.length) {
			startingKeywords = new String[keywords.length];
			System.arraycopy(keywords, 0, startingKeywords, 0, keywords.length);
		} else {
			int num = 0;
			for (int i = 0; i < keywords.length; i++) {
				if (this.HasIndexFile(keywords[i]))
					num++;
			}

			startingKeywords = new String[num];
			int currentPos = -1;
			for (int i = 0; i < keywords.length; i++) {
				if (this.HasIndexFile(keywords[i])) {
					startingKeywords[++currentPos] = keywords[i];
				}
			}
		}

		// add urls commonly occuring in all original form of keywords first
		ArrayList<String> tempDocURLs = getResultDocsCommonUrl(
				startingKeywords, keyResults);
		KeyAndCommonURL.put(startingKeywords, tempDocURLs);
		for (int i = 0; i < tempDocURLs.size(); i++) {
			if (!DocURLs.contains(tempDocURLs.get(i)))
				DocURLs.add(tempDocURLs.get(i));

			if (Score.containsKey(tempDocURLs.get(i))) {
				double prevRank = Score.get(tempDocURLs.get(i));
				prevRank += ((1 + 0.5) + (1 + 0.5)) * 0.05;
				Score.put(tempDocURLs.get(i), prevRank);
			} else {
				double Rank = ((1 + 0.5) + (1 + 0.5)) * 0.05;
				Score.put(tempDocURLs.get(i), Rank);
			}
		}

		// add urls commonly occuring in all keywords with some of them being
		// stemmer version
		String[] initialArr = new String[startingKeywords.length];
		addStemmerwordScore(0, initialArr);

		// add urls considering keywords being partially occurred
		for (int i = 0; i < keyResults.size(); i++) {
			SingleKeyResult skr = keyResults.get(i);
			for (String s : skr.wordDocEntries.keySet()) {
				boolean isOrigin = isOrigin(s);
				for (int j = 0; j < skr.wordDocEntries.get(s).size(); j++) {
					String url = skr.wordDocEntries.get(s).get(j).url;
					if (!DocURLs.contains(url)) {
						this.DocURLs.add(url);
					}
					double Rank = ((BoolToInt(isOrigin) + 0.5) + (0 + 0.5)) * 0.05;
					Score.put(url, Rank);
				}
			}
		}
	}

	public Long wordContinuousPlain(Long currentPos,
			ArrayList<PlainHit> nextPhitList, int nextKeyPos) {
		int curKeyPos = nextKeyPos - 1;
		String curKey = keyResults.get(curKeyPos).keyword;
		String nextKey = keyResults.get(nextKeyPos).keyword;
		// int diff=getKeywordPosition(nextKey)-getKeywordPosition(curKey);
		int actualPrevPos = getKeywordPosition(curKeyPos, curKey);
		int diff = getKeywordPosition(actualPrevPos + 1, nextKey)
				- actualPrevPos;

		for (int i = 0; i < nextPhitList.size(); i++) {
			if (nextPhitList.get(i).position == currentPos + diff)
				return (currentPos + diff);
		}
		return (long) -1;
	}

	public Long wordContinuousAnchor(Long currentPos,
			ArrayList<AnchorHit> nextAhitList, int nextKeyPos) {
		int curKeyPos = nextKeyPos - 1;
		String curKey = keyResults.get(curKeyPos).keyword;
		String nextKey = keyResults.get(nextKeyPos).keyword;
		// int diff=getKeywordPosition(nextKey)-getKeywordPosition(curKey);
		int actualPrevPos = getKeywordPosition(curKeyPos, curKey);
		int diff = getKeywordPosition(actualPrevPos + 1, nextKey)
				- actualPrevPos;

		for (int i = 0; i < nextAhitList.size(); i++) {
			if (nextAhitList.get(i).position == currentPos + diff)
				return (currentPos + diff);
		}
		return (long) -1;
	}

	public double handleOneSubtypeWeight(
			HashMap<Integer, ArrayList<Long>> keySubPosInfo, int subtype,
			boolean isOriginalKeys, String currentUrl) {

		int maxContinuousAlignment = 1;
		int numberOfContinuousMatching = 0;
		double rank = 0;
		ArrayList<Long> currentPosList = keySubPosInfo.get(0);
		for (int m = 0; m < currentPosList.size(); m++) {
			Long currentPos = currentPosList.get(m);
			int nextkeyPos = 1;
			while (nextkeyPos < keySubPosInfo.size()) {
				int curKeyPos = nextkeyPos - 1;
				String curKey = keyResults.get(curKeyPos).keyword;
				String nextKey = keyResults.get(nextkeyPos).keyword;
				int actualPrevPos = getKeywordPosition(curKeyPos, curKey);
				int diff = getKeywordPosition(actualPrevPos + 1, nextKey)
						- actualPrevPos;

				ArrayList<Long> nextPosList = keySubPosInfo.get(nextkeyPos);
				if (!nextPosList.contains(currentPos + diff))
					break;
				currentPos += diff;
				nextkeyPos++;
			}
			if (maxContinuousAlignment < nextkeyPos)
				maxContinuousAlignment = nextkeyPos;
			if (nextkeyPos == keySubPosInfo.size()) {
				numberOfContinuousMatching++;
			}
		}
		if (numberOfContinuousMatching > 0) {
			if (subtype == 0 || subtype == 1) {
				TitleUrlMatched.add(currentUrl);
			}
			if (isOriginalKeys) {
				switch (subtype) {
				case 0:
					rank += 10.0 * numberOfContinuousMatching;
					break;
				case 1:
					rank += 15.0 * numberOfContinuousMatching;
					break;
				case 2:
					rank += 8.0 * numberOfContinuousMatching;
					break;
				case 3:
					rank += 4.0 * numberOfContinuousMatching;
					break;
				}

			}
			else {
				switch (subtype) {
				case 0:
					rank += 10.0 * numberOfContinuousMatching * 0.5;
					break;
				case 1:
					rank += 15.0 * numberOfContinuousMatching * 0.5;
					break;
				case 2:
					rank += 8.0 * numberOfContinuousMatching * 0.5;
					break;
				case 3:
					rank += 4.0 * numberOfContinuousMatching * 0.5;
					break;
				}
			}

		}

		else {
			double unitIncrease = 0;
			switch (subtype) {
			case 0:
				unitIncrease = 10 * (1.0 / keySubPosInfo.size());
				break;
			case 1:
				unitIncrease = 15 * (1.0 / keySubPosInfo.size());
				break;
			case 2:
				unitIncrease = 8 * (1.0 / keySubPosInfo.size());
				break;
			case 3:
				unitIncrease = 4 * (1.0 / keySubPosInfo.size());
				break;
			}

			if (isOriginalKeys)
				rank += maxContinuousAlignment * unitIncrease;
			else
				rank += maxContinuousAlignment * unitIncrease * 0.5;
		}
		return rank;
	}

	public boolean containsConsecutiveWords(int startingPoint,
			String[] startingArray, String[] keyArr) {

		if (startingPoint + keywords.length > startingArray.length)
			return false;
		int pos;
		for (pos = startingPoint; pos < startingArray.length; pos++) {
			if (startingArray[pos].equals(keyArr[0])) {
				break;
			}
		}

		if (pos + keywords.length > startingArray.length)
			return false;
		boolean fullmatch = true;
		for (int i = 1; i < keyArr.length; i++) {
			String prev = keyResults.get(i - 1).keyword;
			String cur = keyResults.get(i).keyword;
			int actualPrevPos = getKeywordPosition(i - 1, prev);
			int diff = getKeywordPosition(actualPrevPos + 1, cur)
					- actualPrevPos;

			System.out.println(keyArr[i] + " " + startingArray[pos + diff]);
			if (!keyArr[i].equals(startingArray[pos + diff])) {
				fullmatch = false;
				break;
			} else {
				pos += diff;
			}
		}
		if (fullmatch)
			return true;
		else
			return containsConsecutiveWords(pos + 1, startingArray, keyArr);
	}

	public double matchingKeyNum(String s) {
		if (s == null || s.equals(""))
			return 0;

		double num = 0;
		s = s.replaceAll("([a-z]+)[?:!.,;]*", "$1").toLowerCase();
		String[] startingArray = s.split(" ");

		// check if full set continuous original key exists
		if (containsConsecutiveWords(0, startingArray, startingKeywords))
			num += 1.5;

		for (String[] stemmerkeyArr : this.KeyAndCommonURL.keySet()) {
			if (containsConsecutiveWords(0, startingArray, stemmerkeyArr)) {
				num += 1;
				break;
			}
		}

		for (int i = 0; i < keyResults.size(); i++) {
			SingleKeyResult skr = keyResults.get(i);
			for (String str : skr.wordDocEntries.keySet()) {
				if (s.contains(str)) {
					num += (1.0 / (keyResults.size()));
					break;
				}
			}
		}
		return num;
	}

	public String getExcerpt(ArrayList<PlainHit> nextPhitList, Long position) {
		for (int i = 0; i < nextPhitList.size(); i++) {
			PlainHit phit = nextPhitList.get(i);
			if (phit.position == position)
				return phit.excerpt;
		}
		return null;
	}

	public void addPositionWeight(String[] keyArr,
			ArrayList<String> commonUrls, boolean isOriginalKeys) {

		for (int i = 0; i < commonUrls.size(); i++) {

			String currentUrl = commonUrls.get(i);
			double rank = 0;
			ResultSet currentRes = rankingResults.get(currentUrl);

			ArrayList<ArrayList<PlainHit>> plainHits = new ArrayList<ArrayList<PlainHit>>();
			ArrayList<ArrayList<FancyHit>> fancyHits = new ArrayList<ArrayList<FancyHit>>();
			ArrayList<ArrayList<AnchorHit>> anchorHits = new ArrayList<ArrayList<AnchorHit>>();

			for (int j = 0; j < keyResults.size(); j++) {
				SingleKeyResult singleRes = keyResults.get(j);
				DocEntry entry = singleRes.getDocOfUrl(keyArr[j], currentUrl);
				plainHits.add(entry.PlainHitList);
				fancyHits.add(entry.FancyHitList);
				anchorHits.add(entry.AnchorHitList);
			}

			ArrayList<Integer> missingPlainHitKeys = new ArrayList<Integer>();
			for (int j = 0; j < plainHits.size(); j++) {
				if (plainHits.get(j).size() == 0) {
					missingPlainHitKeys.add(j);
				}
			}

			ArrayList<Integer> missingAnchorHitKeys = new ArrayList<Integer>();
			for (int j = 0; j < anchorHits.size(); j++) {
				if (anchorHits.get(j).size() == 0) {
					missingAnchorHitKeys.add(j);
				}
			}

			ArrayList<Integer> missingFancyHitKeys = new ArrayList<Integer>();
			for (int j = 0; j < fancyHits.size(); j++) {
				if (fancyHits.get(j).size() == 0) {
					missingFancyHitKeys.add(j);
				}
			}

			// ///////////////////////// consider for plainHit starts
			// //////////////////////////

			// has plainhit for all keywords
			if (missingPlainHitKeys.size() == 0) {
				int numberOfContinuousMatching = 0;
				int maxContinuousAlignment = 1;

				ArrayList<PlainHit> currentPhitList = plainHits.get(0);
				for (int m = 0; m < currentPhitList.size(); m++) {

					Long currentPos = currentPhitList.get(m).position;
					String currentSummary = currentPhitList.get(m).excerpt;
					int nextkeyPos = 1;
					while (nextkeyPos < plainHits.size()) {
						ArrayList<PlainHit> nextPhitList = plainHits
								.get(nextkeyPos);
						if ((currentPos = wordContinuousPlain(currentPos,
								nextPhitList, nextkeyPos)) == -1)
							break;
						String nextExcerpt = getExcerpt(nextPhitList,
								currentPos);
						if (matchingKeyNum(nextExcerpt) > matchingKeyNum(currentSummary))
							currentSummary = nextExcerpt;

						nextkeyPos++;
					}
					if (maxContinuousAlignment < nextkeyPos)
						maxContinuousAlignment = nextkeyPos;

					// found one full matching
					if (nextkeyPos == plainHits.size()) {
						numberOfContinuousMatching++;

						// have not modified via full matching
						if (!SummaryChangedUrls.keySet().contains(currentUrl)) {
							currentRes.setSummary(currentSummary);
							SummaryChangedUrls.put(currentUrl,
									matchingKeyNum(currentSummary));
						} else {
							double prevNum = SummaryChangedUrls.get(currentUrl);
							double cur = matchingKeyNum(currentSummary);
							if (prevNum < cur) {
								currentRes.setSummary(currentSummary);
								SummaryChangedUrls.put(currentUrl, cur);
							}
						}
					}
				}
				// align as continuous position, increase weight by 0.2 for
				// each matching alignment
				if (numberOfContinuousMatching > 0) {
					if (isOriginalKeys)
						rank += 2.0 * numberOfContinuousMatching;
					else
						rank += 2.0 * numberOfContinuousMatching * 0.5;
				}

				// not well aligned in position, add percentage weight based on
				// maxContinuous Alignment
				else {
					if (isOriginalKeys)
						rank += maxContinuousAlignment
								* (1.0 / (plainHits.size())) * 2.0;

					else
						rank += maxContinuousAlignment
								* (1.0 / (plainHits.size())) * 2.0 * 0.5;
				}
			}
			// ///////////////////////// consider position for plainHit ends
			// //////////////////////////

			// ///////////////////////// consider position for fancyHits starts
			// //////////////////////////
			// has fancyhit for all keywords
			if (missingFancyHitKeys.size() == 0) {

				// key---keyPos, value--- occuring postion of one specific
				// subtype
				HashMap<Integer, ArrayList<Long>> KeySub0PosInfo = new HashMap<Integer, ArrayList<Long>>();
				HashMap<Integer, ArrayList<Long>> KeySub1PosInfo = new HashMap<Integer, ArrayList<Long>>();
				HashMap<Integer, ArrayList<Long>> KeySub2PosInfo = new HashMap<Integer, ArrayList<Long>>();
				HashMap<Integer, ArrayList<Long>> KeySub3PosInfo = new HashMap<Integer, ArrayList<Long>>();

				for (int k = 0; k < fancyHits.size(); k++) {
					ArrayList<FancyHit> currentFList = fancyHits.get(k);
					ArrayList<Long> list0 = new ArrayList<Long>();
					ArrayList<Long> list1 = new ArrayList<Long>();
					ArrayList<Long> list2 = new ArrayList<Long>();
					ArrayList<Long> list3 = new ArrayList<Long>();
					for (int m = 0; m < currentFList.size(); m++) {
						if (currentFList.get(m).subtype == 0)
							list0.add(currentFList.get(m).position);
						else if (currentFList.get(m).subtype == 1)
							list1.add(currentFList.get(m).position);
						else if (currentFList.get(m).subtype == 2)
							list2.add(currentFList.get(m).position);
						else if (currentFList.get(m).subtype == 3)
							list3.add(currentFList.get(m).position);
					}

					KeySub0PosInfo.put(k, list0);
					KeySub1PosInfo.put(k, list1);
					KeySub2PosInfo.put(k, list2);
					KeySub3PosInfo.put(k, list3);
				}

				double increasement;

				// for subtype 3 ---h1~h6
				increasement = handleOneSubtypeWeight(KeySub3PosInfo, 3,
						isOriginalKeys, currentUrl);
				rank += increasement;

				// for subtype 2 ---meta data
				increasement = handleOneSubtypeWeight(KeySub2PosInfo, 2,
						isOriginalKeys, currentUrl);
				rank += increasement;

				// for subtype 1 ---title
				increasement = handleOneSubtypeWeight(KeySub1PosInfo, 1,
						isOriginalKeys, currentUrl);
				rank += increasement;

				// for subtype 0 ---url
				increasement = handleOneSubtypeWeight(KeySub0PosInfo, 0,
						isOriginalKeys, currentUrl);
				rank += increasement;
			}

			// ///////////////////////// consider position for fancyHit ends
			// //////////////////////////

			// ///////////////////////// consider position for anchorHit starts
			// //////////////////////////
			// has anchorhit for all keywords,1 for each matching alignment
			if (missingAnchorHitKeys.size() == 0) {
				int numberOfContinuousMatching = 0;
				int maxContinuousAlignment = 1;
				ArrayList<AnchorHit> currentAhitList = anchorHits.get(0);
				for (int m = 0; m < currentAhitList.size(); m++) {
					Long currentPos = currentAhitList.get(m).position;
					int nextkeyPos = 1;
					while (nextkeyPos < anchorHits.size()) {
						ArrayList<AnchorHit> nextAhitList = anchorHits
								.get(nextkeyPos);
						if ((currentPos = wordContinuousAnchor(currentPos,
								nextAhitList, nextkeyPos)) == -1)
							break;
						nextkeyPos++;
						maxContinuousAlignment++;
					}
					if (maxContinuousAlignment < nextkeyPos)
						maxContinuousAlignment = nextkeyPos;
					if (nextkeyPos == anchorHits.size()) {
						numberOfContinuousMatching++;
					}
				}
				if (numberOfContinuousMatching > 0) {
					if (isOriginalKeys)
						rank += numberOfContinuousMatching * 1.0;
					else
						rank += numberOfContinuousMatching * 1.0 * 0.5;
				}

				// not well aligned in position, add percentage weight based on
				// maxContinuous Alignment
				else {
					if (isOriginalKeys)
						rank += maxContinuousAlignment
								* (1.0 / (anchorHits.size())) * 1.0;
					else
						rank += maxContinuousAlignment
								* (1.0 / (anchorHits.size())) * 1.0 * 0.5;
				}
			}
			// ///////////////////////// consider position for AnchorHit ends
			// //////////////////////////

			double oldRank = Score.get(currentUrl);
			Score.put(currentUrl, oldRank + rank);

		}
	}

	public double calculateSimilarity(String excerpt) {
		if (excerpt.equals(""))
			return 0;
		double similarity = 0;
		excerpt=excerpt.toLowerCase();
		ArrayList<String> appearedKeywords = new ArrayList<String>();

		for (int zz = 0; zz < startingKeywords.length; zz++) {
			if (excerpt.contains(startingKeywords[zz])) {
				similarity += 0.5;
				appearedKeywords.add(startingKeywords[zz]);
			}
		}

		for (int i = 0; i < keyResults.size(); i++) {
			if (appearedKeywords.contains(keyResults.get(i).keyword)) {
				continue;
			}
			ArrayList<String> curStemmerWords = stemmerWordsForOneKeyResult(keyResults
					.get(i));
			for (int j = 0; j < curStemmerWords.size(); j++) {
				if (excerpt.contains(curStemmerWords.get(j))) {
					similarity += 0.35;
					break;
				}
			}
		}

		ArrayList<String> allStemmerWords = allStemmerWords();
		String[] excerptArr = excerpt.split(" ");
		for (int i = 0; i < excerptArr.length; i++) {
			if (allStemmerWords.contains(excerptArr[i]))
				similarity += 0.02;
		}
		return similarity;
	}

	public HashMap<ResultSet, Double> getFinalResults() {

		getDocURLResults();
		sendPageRankReq();

		for (int i = 0; i < DocURLs.size(); i++) {

			String currentUrl = DocURLs.get(i);
			double rank = 0;
			ResultSet bestResultSet = new ResultSet();
			boolean docInitialized = false;

			for (int j = 0; j < keyResults.size(); j++) {
				SingleKeyResult singleRes = keyResults.get(j);
				ArrayList<DocEntry> DocList = singleRes
						.getDocListOfUrl(currentUrl);

				if (DocList.size() > 0) {
					for (int k = 0; k < DocList.size(); k++) {
						String currentBestSummary = "";
						DocEntry entry = DocList.get(k);

						if (!docInitialized) {
							docInitialized = true;
							bestResultSet.setTitle(entry.title);
							bestResultSet.setUrl(entry.url);
							bestResultSet.setDocType(singleRes.type);
							bestResultSet.setLocation(entry.location);

							// added for image
							if (type == 1) {
								bestResultSet.setPageUrl(entry.pageUrl);
							}
							for (int index = 0; index < entry.PlainHitList
									.size(); index++) {
								if (!entry.PlainHitList.get(index).excerpt
										.equals("")) {
									bestResultSet.setSummary(entry.PlainHitList
											.get(index).excerpt);
									break;
								}
							}
						}

						if (singleRes.idf == 0)
							rank += entry.tf * 0.05;
						else
							rank += entry.tf * singleRes.idf;

						int totalLength = 0;
						if (entry.PlainHitList.size() > 0)
							totalLength += entry.PlainHitList.size();
						if (entry.FancyHitList.size() > 0)
							totalLength += entry.FancyHitList.size();
						if (entry.AnchorHitList.size() > 0)
							totalLength += entry.FancyHitList.size();

						if (totalLength > 0) {
							if (entry.PlainHitList.size() > 0)
								rank += 0.1 * (entry.PlainHitList.size() / totalLength);
							if (entry.FancyHitList.size() > 0)
								rank += 0.2 * (entry.FancyHitList.size() / totalLength);
							if (entry.AnchorHitList.size() > 0)
								rank += 0.3 * (entry.AnchorHitList.size() / totalLength);
						}

						// update initial ResultSet info first, based on maximum
						// number of key occurance in excerpt
						for (int index = 0; index < entry.PlainHitList.size(); index++) {
							String excerpt = entry.PlainHitList.get(index).excerpt;
							if (!excerpt.equals("")) {
								double currentSimilarity = calculateSimilarity(excerpt);
								// System.out.println("URL: "+currentUrl+"\n Excerpt: "+excerpt+" "+currentSimilarity);
								if (currentSimilarity > calculateSimilarity(bestResultSet.summary))
									currentBestSummary = excerpt;
							}
						}

						if (calculateSimilarity(currentBestSummary) > calculateSimilarity(bestResultSet.summary)) {
							bestResultSet.setSummary(currentBestSummary);
							bestResultSet.setTitle(entry.title);
							bestResultSet.setUrl(entry.url);
							bestResultSet.setDocType(singleRes.type);
							bestResultSet.setLocation(entry.location);
						}
					}
				}
			}

			rankingResults.put(currentUrl, bestResultSet);
			if (Score.containsKey(currentUrl))
				Score.put(currentUrl, Score.get(currentUrl) + rank);
			else
				Score.put(currentUrl, rank);

		}

		for (String[] Arr : KeyAndCommonURL.keySet()) {

			boolean isOriginalKeys = true;
			for (int kk = 0; kk < Arr.length; kk++) {
				if (!Arr[kk].equals(startingKeywords[kk])) {
					isOriginalKeys = false;
					break;
				}
			}
			ArrayList<String> commonUrl = KeyAndCommonURL.get(Arr);
			addPositionWeight(Arr, commonUrl, isOriginalKeys);
		}

		HashMap<ResultSet, Double> ResultAndScore = new HashMap<ResultSet, Double>();
		for (int i = 0; i < DocURLs.size(); i++) {
			String url = DocURLs.get(i);
			ResultSet rSet = this.rankingResults.get(url);

			// rescan rSet, add to summaryChangedUrl and TitleChangedUrl if not
			// existing
			if (!SummaryChangedUrls.containsKey(rSet.url)) {
				checkSummaryFullMatch(rSet.url, rSet.summary);
			}
			if (!TitleUrlMatched.contains(rSet.url)) {
				checkTitleFullMatch(rSet.url, rSet.title);
			}

			// add pageRank score
			HashMap<String, Double> normalizedPageRank = normalizePageRank();
			Double prDouble = normalizedPageRank.get(url);
			rSet.setprankScore(prDouble);
			double tfidfscore = Score.get(url);
			rSet.settfidf(tfidfscore);
			ResultAndScore.put(rSet, (tfidfscore * prDouble));

		}
		return ResultAndScore;
	}

	protected void checkSummaryFullMatch(String url, String content) {

		if (content == null || content.equals(""))
			return;

		String[] strs = content.replaceAll("([a-z]+)[?:!.,;]*", "$1").toLowerCase()
				.split(" ");
		double rank = Score.get(url);
		if (containsConsecutiveWords(0, strs, startingKeywords)) {
			SummaryChangedUrls.put(url, matchingKeyNum(content));
		}
		if (!SummaryChangedUrls.keySet().contains(url)) {
			for (String keycombination[] : KeyAndCommonURL.keySet()) {
				if (containsConsecutiveWords(0, strs, keycombination)) {
					SummaryChangedUrls.put(url, matchingKeyNum(content));
					break;
				}
			}
		}
		rank += 5.0 * matchingKeyNum(content);
		Score.put(url, rank);
	}

	protected void checkTitleFullMatch(String url, String title) {

		if (title == null || title.equals(""))
			return;

		String[] titleStrs = title.replaceAll("([a-z]+)[?:!.,;]*", "$1").toLowerCase().split(
				" ");
		double rank = Score.get(url);
		if (containsConsecutiveWords(0, titleStrs, startingKeywords)) {
			TitleUrlMatched.add(url);
		}
		if (!TitleUrlMatched.contains(url)) {
			for (String keycombination[] : KeyAndCommonURL.keySet()) {
				if (containsConsecutiveWords(0, titleStrs, keycombination)) {
					TitleUrlMatched.add(url);
					break;
				}
			}
		}
		rank += 15.0 * matchingKeyNum(title);
		Score.put(url, rank);
	}

	private void sendPageRankReq() {
		// send request to pageRank for urls

		HashDivider divider = new HashDivider();
		divider.setIntervals(workers.size());
		Map<WorkerInfo, JSONArray> urlMap = new HashMap<WorkerInfo, JSONArray>();
		for (String url : DocURLs) {
			int index = 0;
			try {
				index = divider.indexOf(url.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
			}
			if (index >= workers.size()) {
				logger.error("Wrong hash index " + index + " for " + url);
				continue;
			}
			WorkerInfo worker = workers.get(index);
			JSONArray urlArray = urlMap.get(worker);
			if (urlArray == null) {
				urlArray = new JSONArray();
				urlMap.put(worker, urlArray);
			}
			urlArray.add(url);
		}
		for (Entry<WorkerInfo, JSONArray> entries : urlMap.entrySet()) {
			WorkerInfo worker = entries.getKey();
			JSONArray urlArray = entries.getValue();

			String jsonStr = getPageRank(worker, urlArray.toJSONString());
			if (jsonStr != null) {
				try {
					JSONObject json = (JSONObject) parser.parse(jsonStr);
					Iterator iter = json.keySet().iterator();
					while (iter.hasNext()) {
						String url = (String) iter.next();
						String prValue = (String) json.get(url);
						if (prValue != null) {
							double pr = StringUtil.parseDouble(prValue, 0.0);
							PageRankScore.put(url, pr);
						}
					}
				} catch (ParseException e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				logger.error("Failed to get pagerank from " + worker.toString());
			}
		}
	}

	private String getPageRank(WorkerInfo worker, String content) {
		String jsonStr = null;
		HttpResponse response = null;
		try {
			response = sendPostToWorker(worker, "/getranks", content);
			if (response.getStatusCode() == 200) {
				jsonStr = mReader.readText(response);
				// logger.debug("Get pagerank: " + jsonStr);
				// json = (JSONObject) parser.parse(jsonStr);
				// PageRankScore.putAll(json);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
		return jsonStr;
	}

	private HashMap<String, Double> normalizePageRank() {
		HashMap<String, Double> normalizedScore = new HashMap<String, Double>();
		for (int i = 0; i < DocURLs.size(); i++) {
			String url = DocURLs.get(i);
			// String str = String.valueOf(PageRankScore.get(url));
			// double prDouble = StringUtil.parseDouble(str, 0.0);
			Double prDouble = PageRankScore.get(url);
			if (prDouble == null) {
				prDouble = 0.0;
			}
			normalizedScore.put(url, prDouble);
		}
		// initialize to first entry pageRank score
		double min = normalizedScore.get(DocURLs.get(0));
		double max = normalizedScore.get(DocURLs.get(0));
		for (String url : normalizedScore.keySet()) {
			if (min > normalizedScore.get(url))
				min = normalizedScore.get(url);
			if (max < normalizedScore.get(url))
				max = normalizedScore.get(url);
		}

		// use formula Normalize(e)=(e-min)/(max-min)
		if (min == max) {
			for (String url : normalizedScore.keySet()) {
				normalizedScore.put(url, 0.5);
			}
		} else {
			for (String url : normalizedScore.keySet()) {
				double originalValue = normalizedScore.get(url);
				double normalizedValue = (originalValue - min) / (max - min);
				if (normalizedValue == 0)
					normalizedValue = min + 0.01;
				normalizedScore.put(url, normalizedValue);
			}
		}

		return normalizedScore;
	}

	private boolean UrlAlreadyAdded(String url, ArrayList<ResultSet> results) {
		for (ResultSet rs : results) {
			if (url.equals(rs.url))
				return true;
		}
		return false;
	}

	public ArrayList<ResultSet> sendResults() {

		ArrayList<ResultSet> results = new ArrayList<ResultSet>();
		HashMap<ResultSet, Double> ResultAndScore = getFinalResults();

		Set<Entry<ResultSet, Double>> set = ResultAndScore.entrySet();
		List<Entry<ResultSet, Double>> list = new ArrayList<Entry<ResultSet, Double>>(
				set);
		Collections.sort(list, new Comparator<Map.Entry<ResultSet, Double>>() {
			public int compare(Map.Entry<ResultSet, Double> o1,
					Map.Entry<ResultSet, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		if (this.locationPrio == 1) {
			// if location priority selected, put location matching results
			// first
			for (Map.Entry<ResultSet, Double> entry : list) {
				ResultSet rs = entry.getKey();
				rs.setScore(entry.getValue());
				if ((!UrlAlreadyAdded(rs.url, results))
						&& (rs.location.equals(this.location)))
					results.add(rs);
			}
		}

		for (Map.Entry<ResultSet, Double> entry : list) {
			ResultSet rs = entry.getKey();
			rs.setScore(entry.getValue());
			if ((!UrlAlreadyAdded(rs.url, results))
					&& SummaryChangedUrls.containsKey(rs.url)
					&& TitleUrlMatched.contains(rs.url))
				results.add(rs);
		}

		for (Map.Entry<ResultSet, Double> entry : list) {
			ResultSet rs = entry.getKey();
			rs.setScore(entry.getValue());
			if ((!UrlAlreadyAdded(rs.url, results))
					&& TitleUrlMatched.contains(rs.url))
				results.add(rs);
		}

		for (Map.Entry<ResultSet, Double> entry : list) {
			ResultSet rs = entry.getKey();
			rs.setScore(entry.getValue());
			if ((!UrlAlreadyAdded(rs.url, results))
					&& SummaryChangedUrls.containsKey(rs.url))
				results.add(rs);
		}

		for (Map.Entry<ResultSet, Double> entry : list) {
			ResultSet rs = entry.getKey();
			rs.setScore(entry.getValue());
			if (!UrlAlreadyAdded(rs.url, results) && (!rs.summary.equals("")))
				results.add(rs);
		}

		for (Map.Entry<ResultSet, Double> entry : list) {
			ResultSet rs = entry.getKey();
			rs.setScore(entry.getValue());
			if (!UrlAlreadyAdded(rs.url, results) && (rs.summary.equals("")))
				results.add(rs);
		}
		return results;
	}

	private HttpResponse sendPostToWorker(WorkerInfo worker, String path,
			String content) throws Exception {
		StringBuilder sb = new StringBuilder("http://");
		sb.append(worker.host);
		if (worker.port != 80) {
			sb.append(':').append(worker.port);
		}
		sb.append(path);
		HttpPostRequest request = new HttpPostRequest(sb.toString());
		if (content != null) {
			request.setPostBody(new DataBody(content.getBytes()));
		}
		return mHttpClient.execute(request);
	}
}
