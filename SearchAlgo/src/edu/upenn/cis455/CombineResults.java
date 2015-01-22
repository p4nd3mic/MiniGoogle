package edu.upenn.cis455;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CombineResults {

	String keywords[];
	String location = "";
	JSONParser parser = new JSONParser();

	protected ArrayList<SingleKeyResult> keyResults = new ArrayList<SingleKeyResult>();
	Map<String, Double> Score = new HashMap<String, Double>();
	HashMap<String, ResultSet> rankingResults = new HashMap<String, ResultSet>();
	ArrayList<String> DocURLs = new ArrayList<String>();
	HashMap<String[], ArrayList<String>> KeyAndCommonURL = new HashMap<String[], ArrayList<String>>();
	HashMap<String, Double> SummaryChangedUrls = new HashMap<String, Double>();
	
	//for storing scores from page rank response
	HashMap<String, Double> PageRankScore= new HashMap<String, Double>();
	
	public CombineResults() {
	}

	public CombineResults(String keywords[], String location) {
		this.keywords = new String[keywords.length];
		System.arraycopy(keywords, 0, this.keywords, 0, keywords.length);
		this.location = location;
	}

	public boolean ParseJason(String JsonFile) {

		SingleKeyResult res = new SingleKeyResult();
		try {
			Object ob = parser.parse(JsonFile);

			JSONObject jsonObject = (JSONObject) ob;

			JSONArray results = (JSONArray) jsonObject.get("results");
			Double idf = (Double) jsonObject.get("idf");
			res.setIdf(idf);

			String keyword = (String) jsonObject.get("keyword");
			res.setKeyword(keyword);

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
				if (!s.equals(keywords[i]))
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
		ArrayList<DocEntry> DocEntry = keyResults.get(0).wordDocEntries
				.get(keywords[0]);
		for (int i = 0; i < DocEntry.size(); i++)
			currentDocURL.add(DocEntry.get(i).url);

		for (int i = 1; i < keyResults.size(); i++) {
			ArrayList<String> firstURLSet = new ArrayList<String>();
			for (int j = 0; j < currentDocURL.size(); j++)
				firstURLSet.add(currentDocURL.get(j));

			ArrayList<String> secondURLSet = new ArrayList<String>();
			DocEntry = keyResults.get(i).wordDocEntries.get(keywords[i]);
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
				if (!keyArr.equals(keywords[i])) {
					originalText = false;
					break;
				}
			}
			if (!originalText) {
				ArrayList<String> tempDoc = getResultDocsCommonUrl(keyArr,
						keyResults);
				KeyAndCommonURL.put(keyArr, tempDoc);
				for (int i = 0; i < tempDoc.size(); i++) {
					if (!DocURLs.contains(tempDoc.get(i)))
						DocURLs.add(tempDoc.get(i));

					if (Score.containsKey(tempDoc.get(i))) {
						double prevRank = Score.get(tempDoc.get(i));
						prevRank += ((0 + 0.5) + (1 + 0.5)) * 0.5;
						Score.put(tempDoc.get(i), prevRank);
					} else {
						double Rank = ((0 + 0.5) + (1 + 0.5)) * 0.5;
						Score.put(tempDoc.get(i), Rank);
					}
				}
			}
		} else {

			if (keyResults.get(currentKeyPos).wordDocEntries.size() == 1) {
				String[] newKeyArr = new String[keyArr.length];
				System.arraycopy(keyArr, 0, newKeyArr, 0, keyArr.length);
				newKeyArr[currentKeyPos] = keywords[currentKeyPos];
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
		for (int i = 0; i < keywords.length; i++) {
			if (s.equals(keywords[i]))
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

	public void getDocURLResults() {

		// add urls commonly occuring in all original form of keywords first
		ArrayList<String> tempDocURLs = getResultDocsCommonUrl(keywords,
				keyResults);
		KeyAndCommonURL.put(keywords, tempDocURLs);
		for (int i = 0; i < tempDocURLs.size(); i++) {
			if (!DocURLs.contains(tempDocURLs.get(i)))
				DocURLs.add(tempDocURLs.get(i));

			if (Score.containsKey(tempDocURLs.get(i))) {
				double prevRank = Score.get(tempDocURLs.get(i));
				prevRank += ((1 + 0.5) + (1 + 0.5)) * 0.5;
				Score.put(tempDocURLs.get(i), prevRank);
			} else {
				double Rank = ((1 + 0.5) + (1 + 0.5)) * 0.5;
				Score.put(tempDocURLs.get(i), Rank);
			}
		}

		// add urls commonly occuring in all keywords with some of them being
		// stemmer version
		String[] initialArr = new String[keywords.length];
		addStemmerwordScore(0, initialArr);

		// add urls considering original keywords being partially occurred
		for (int i = 0; i < keyResults.size(); i++) {
			SingleKeyResult skr = keyResults.get(i);
			for (String s : skr.wordDocEntries.keySet()) {
				boolean isOrigin = isOrigin(s);

				for (int j = 0; j < skr.wordDocEntries.get(s).size(); j++) {
					String url = skr.wordDocEntries.get(s).get(j).url;
					if (!DocURLs.contains(url))
						this.DocURLs.add(url);
					/*
					 * if (Score.containsKey(url)) { double prevRank =
					 * Score.get(tempDocURLs.get(i)); prevRank +=
					 * ((BoolToInt(isOrigin) + 0.5) + (0 + 0.5)) * 0.5;
					 * Score.put(url, prevRank); } else {
					 */
					double Rank = ((BoolToInt(isOrigin) + 0.5) + (0 + 0.5)) * 0.5;
					Score.put(url, Rank);
				}
			}
		}
	}

	// }

	public Long wordContinuousPlain(Long currentPos,
			ArrayList<PlainHit> nextPhitList) {
		for (int i = 0; i < nextPhitList.size(); i++) {
			if (nextPhitList.get(i).position == currentPos + 1)
				return (currentPos + 1);
		}
		return (long) -1;
	}

	public Long wordContinuousAnchor(Long currentPos,
			ArrayList<AnchorHit> nextAhitList) {
		for (int i = 0; i < nextAhitList.size(); i++) {
			if (nextAhitList.get(i).position == currentPos + 1)
				return (currentPos + 1);
		}
		return (long) -1;
	}

	// increase weight 0.2 *(4-subtype) for each matching of one subtype, at
	// most 0.8 for subtype=0
	public double handleOneSubtypeWeight(
			HashMap<Integer, ArrayList<Long>> keySubPosInfo, int subtype,
			boolean isOriginalKeys) {

		int maxContinuousAlignment = 1;
		int numberOfContinuousMatching = 0;
		double rank = 0;
		ArrayList<Long> currentPosList = keySubPosInfo.get(0);
		for (int m = 0; m < currentPosList.size(); m++) {
			Long currentPos = currentPosList.get(m);
			int nextkeyPos = 1;
			while (nextkeyPos < keySubPosInfo.size() - 1) {
				ArrayList<Long> nextPosList = keySubPosInfo.get(nextkeyPos);
				if (!nextPosList.contains(currentPos + 1))
					break;
				currentPos++;
				nextkeyPos++;
			}
			if (maxContinuousAlignment < nextkeyPos + 1)
				maxContinuousAlignment = nextkeyPos + 1;
			if (nextkeyPos == keySubPosInfo.size() - 1) {
				numberOfContinuousMatching++;
			}
		}
		if (numberOfContinuousMatching > 0) {
			if (isOriginalKeys)
				rank += 0.2 * (4 - subtype) * numberOfContinuousMatching;
			else
				rank += 0.2 * (4 - subtype) * numberOfContinuousMatching * 0.5;
		}

		else {
			double unitIncrease = (0.2 * (4 - subtype)) / keySubPosInfo.size();
			if (isOriginalKeys)
				rank += maxContinuousAlignment * unitIncrease;
			else
				rank += maxContinuousAlignment * unitIncrease * 0.5;
		}
		return rank;
	}

	public boolean containsConsecutiveWords(int startingPoint, String[] startingArray, String []keyArr){
		
		if(startingPoint+keyArr.length>startingArray.length)
			return false;
		int pos;
		for(pos=startingPoint;pos<startingArray.length;pos++){
			if(startingArray[pos].equals(keyArr[0])){
				break;
			}	 
		}
		
		if(pos+keyArr.length>startingArray.length)
			return false;
		boolean fullmatch=true;
		for(int i=0;i<keyArr.length;i++){
			if(!keyArr[i].equals(startingArray[pos+i])){
				fullmatch=false;
				break;
			}		
		}
		if(fullmatch)
			return true;
		else
			return containsConsecutiveWords(pos+1, startingArray, keyArr);
	}
	
	
	public double matchingKeyNum(String[] keyArr, String s) {
		double num = 0;
		boolean containsFullset = false;
		String []startingArray =s.split(" ");
		
		// check if full set continuous original key exists
		if(containsConsecutiveWords(0, startingArray, keywords))
			num+=1.5;

		for (String[] stemmerkeyArr : this.KeyAndCommonURL.keySet()) {
			if(containsConsecutiveWords(0, startingArray, stemmerkeyArr))
				num+=1;
		}

		for (int i = 0; i < keyResults.size(); i++) {
			SingleKeyResult skr = keyResults.get(i);
			for (String str : skr.wordDocEntries.keySet()) {
				if (s.contains(str)) {
					num += 0.2;
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
					while (nextkeyPos < plainHits.size() - 1) {
						ArrayList<PlainHit> nextPhitList = plainHits
								.get(nextkeyPos);
						if ((currentPos = wordContinuousPlain(currentPos,
								nextPhitList)) == -1)
							break;
						String nextExcerpt = getExcerpt(nextPhitList,
								currentPos);
						if (matchingKeyNum(keyArr, nextExcerpt) > matchingKeyNum(
								keyArr, currentSummary))
							currentSummary = nextExcerpt;

						nextkeyPos++;
					}
					if (maxContinuousAlignment < nextkeyPos + 1)
						maxContinuousAlignment = nextkeyPos + 1;

					// found one full matching
					if (nextkeyPos == plainHits.size() - 1) {
						numberOfContinuousMatching++;

						// have not modified via full matching
						if (!SummaryChangedUrls.keySet().contains(currentUrl)) {
							currentRes.setSummary(currentSummary);
							SummaryChangedUrls.put(currentUrl,
									matchingKeyNum(keyArr, currentSummary));
						} else {
							double prevNum = SummaryChangedUrls.get(currentUrl);
							double cur = matchingKeyNum(keyArr, currentSummary);
							if (prevNum < cur) {
								currentRes.setSummary(currentSummary);
								SummaryChangedUrls.put(currentUrl, cur);
							}
						}
					}
				}
				// align as position(i;i+1;i+2), increase weight by 0.15 for
				// each matching alignment
				if (numberOfContinuousMatching > 0) {
					if (isOriginalKeys)
						rank += 0.15
								* numberOfContinuousMatching
								* ((SummaryChangedUrls.get(currentUrl) * 1.0) / keywords.length);
					else
						rank += 0.15
								* numberOfContinuousMatching
								* 0.5
								* ((SummaryChangedUrls.get(currentUrl) * 1.0) / keywords.length);
				}

				// not well aligned in position, add percentage weight based on
				// maxContinuous Alignment
				else {
					if (isOriginalKeys)
						rank += maxContinuousAlignment
								* (1.0 / (plainHits.size()))
								* 0.15
								* ((SummaryChangedUrls.get(currentUrl) * 1.0) / keywords.length);
					else
						rank += maxContinuousAlignment
								* (1.0 / (plainHits.size()))
								* 0.15
								* 0.5
								* ((SummaryChangedUrls.get(currentUrl) * 1.0) / keywords.length);
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
						isOriginalKeys);
				rank += increasement;

				// for subtype 2 ---meta data
				increasement = handleOneSubtypeWeight(KeySub2PosInfo, 2,
						isOriginalKeys);
				rank += increasement;

				// for subtype 1 ---title
				increasement = handleOneSubtypeWeight(KeySub1PosInfo, 1,
						isOriginalKeys);
				rank += increasement;

				// for subtype 0 ---url
				increasement = handleOneSubtypeWeight(KeySub0PosInfo, 0,
						isOriginalKeys);
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
					while (nextkeyPos < anchorHits.size() - 1) {
						ArrayList<AnchorHit> nextAhitList = anchorHits
								.get(nextkeyPos);
						if ((currentPos = wordContinuousAnchor(currentPos,
								nextAhitList)) == -1)
							break;
						nextkeyPos++;
						maxContinuousAlignment++;
					}
					if (maxContinuousAlignment < nextkeyPos + 1)
						maxContinuousAlignment = nextkeyPos + 1;
					if (nextkeyPos == anchorHits.size() - 1) {
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
		for (int zz = 0; zz < keywords.length; zz++) {
			if (excerpt.contains(keywords[zz]))
				similarity += 0.5;
		}

		ArrayList<String> allStemmerWords = allStemmerWords();
		for (int mm = 0; mm < allStemmerWords.size(); mm++) {
			if (excerpt.contains(allStemmerWords.get(mm)))
				similarity += ((0.5) / keywords.length);
		}
		return similarity;

	}

	//TODO:send page rank request, receive response
	public void sendPageRankReq(){
		//send request to pageRank for urls
		
		/*parse pageRank json result, maybe open a new thread receiving incoming results? 
		  storing in PageRankScore object.
		*/
	}
	
	
	public HashMap<ResultSet, Double> getFinalResults() {

		getDocURLResults();
		//TODO:now we have all URLs stored in DocURLs, send json request to pageRank.
		
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
						// int MaxOccuranceforOneEntry = 0;
						DocEntry entry = DocList.get(k);

						if (!docInitialized) {
							docInitialized = true;
							bestResultSet.setTitle(entry.title);
							bestResultSet.setUrl(entry.url);
							bestResultSet.setDocType(singleRes.type);
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
							rank += entry.tf * 0.01;
						else
							rank += entry.tf * singleRes.idf;

						// assign each type 0 weight 0.1; each type 1 weight
						// 0.2;
						// each type 2 weight 0.3
						if (entry.PlainHitList.size() > 0)
							rank += 0.1 * entry.PlainHitList.size();
						if (entry.FancyHitList.size() > 0)
							rank += 0.2 * entry.FancyHitList.size();
						if (entry.AnchorHitList.size() > 0)
							rank += 0.3 * entry.AnchorHitList.size();

						// assign 0.1 if location matches
						if (!entry.location.equals("N")) {
							if (entry.location.toLowerCase().equals(location))
								rank += 0.1;
						}

						// update initial ResultSet info first, based on maximum
						// number of key occurance in excerpt
						for (int index = 0; index < entry.PlainHitList.size(); index++) {
							// int currentMaxkeyOccurance = 0;
							String excerpt = entry.PlainHitList.get(index).excerpt;
							if (!excerpt.equals("")) {
								double currentSimilarity = calculateSimilarity(excerpt);
								if (currentSimilarity > calculateSimilarity(bestResultSet.summary))
									currentBestSummary = excerpt;
							}
						}

						if (calculateSimilarity(currentBestSummary) > calculateSimilarity(bestResultSet.summary)) {
							bestResultSet.setSummary(currentBestSummary);
							bestResultSet.setTitle(entry.title);
							bestResultSet.setUrl(entry.url);
							bestResultSet.setDocType(singleRes.type);
						}
					}
				}
			}

			rankingResults.put(currentUrl, bestResultSet);
			// increase rank by a small factor based on number of similarity of
			// keys in the best summary
			double factor = (calculateSimilarity(bestResultSet.summary))
					/ (keywords.length);
			if (Score.containsKey(currentUrl))
				Score.put(currentUrl, Score.get(currentUrl) + rank
						* (1 + 0.1 * factor));
			else
				Score.put(currentUrl, rank * (1 + 0.1 * factor));
		}

		for (String[] Arr : KeyAndCommonURL.keySet()) {

			boolean isOriginalKeys = true;
			for (int kk = 0; kk < Arr.length; kk++) {
				if (Arr[kk].equals(keywords[kk])) {
					isOriginalKeys = false;
					break;
				}
			}
			ArrayList<String> commonUrl = KeyAndCommonURL.get(Arr);
			addPositionWeight(Arr, commonUrl, isOriginalKeys);
		}

		/*
		 * // decrease rank by 1/3 if the doc does not have summary info for
		 * (int i = 0; i < DocURLs.size(); i++) { String url = DocURLs.get(i);
		 * ResultSet rSet = this.rankingResults.get(url); if
		 * (rSet.summary.equals("")) { Score.put(url, 2 * Score.get(url) / 3); }
		 * }
		 */
		HashMap<ResultSet, Double> ResultAndScore = new HashMap<ResultSet, Double>();
		for (int i = 0; i < DocURLs.size(); i++) {
			String url = DocURLs.get(i);
			ResultSet rSet = this.rankingResults.get(url);
			ResultAndScore.put(rSet, Score.get(url));
		}
		return ResultAndScore;

	}

	public ArrayList<ResultSet> sendResults() {

		ArrayList<ResultSet> results = new ArrayList<ResultSet>();
		HashMap<ResultSet, Double> ResultAndScore = getFinalResults();
		//TODO:update score, adding information from page rank here
		
		
		Set<Entry<ResultSet, Double>> set = ResultAndScore.entrySet();
		List<Entry<ResultSet, Double>> list = new ArrayList<Entry<ResultSet, Double>>(
				set);
		Collections.sort(list, new Comparator<Map.Entry<ResultSet, Double>>() {
			public int compare(Map.Entry<ResultSet, Double> o1,
					Map.Entry<ResultSet, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// add ResultSet with summary info first
		for (Map.Entry<ResultSet, Double> entry : list) {
			if (results.size() >= 100)
				break;
			ResultSet rs = entry.getKey();
			if (!rs.summary.equals(""))
				results.add(rs);
		}

		if (results.size() >= 100)
			return results;

		for (Map.Entry<ResultSet, Double> entry : list) {
			if (results.size() >= 100)
				break;
			ResultSet rs = entry.getKey();
			if (rs.summary.equals(""))
				results.add(rs);
		}

		return results;

	}

}
