package edu.upenn.cis455.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cis455.hit.AnchorHit;
import edu.upenn.cis455.hit.FancyHit;
import edu.upenn.cis455.hit.Hit;
import edu.upenn.cis455.hit.PlainHit;

public class GenerateHitListDocument {
	private String html;
	private String url;
	private HashMap<String, ArrayList<Hit>> outcome;
	private int totalNumber;
	
	private int docId;
	
	public GenerateHitListDocument(String html, String url) {
		this.html = html;
		this.url = url;
		this.outcome = new HashMap<String, ArrayList<Hit>>();
		this.totalNumber = 0;
	}
	
	public GenerateHitListDocument(String html, String url, int docId) {
		this.html = html;
		this.url = url;
		this.outcome = new HashMap<String, ArrayList<Hit>>();
		this.docId = docId;
		this.totalNumber = 0;
	}
	

	private void generateHitList() {
		JsoupHelper jsoup = new JsoupHelper(html);
		Pattern pattern = Pattern.compile("\\b[A-Za-z0-9]+\\b");
		// Get PlainHit result -- 0
		ArrayList<String> paragraphResult = jsoup.getParagraph();
		for (int i = 0; i < paragraphResult.size(); i++) {
			String word = paragraphResult.get(i);
			Matcher matcher = pattern.matcher(word);
			if(word.equals("") || word.equals(" ")|| !matcher.find()){
				continue;
			}
			
			totalNumber++;
			if (outcome.containsKey(word)) {
				Hit plainHit = new PlainHit(1, i);
				outcome.get(word).add(plainHit);
			} else {
				ArrayList<Hit> positions = new ArrayList<Hit>();
				Hit plainHit = new PlainHit(1, i);
				positions.add(plainHit);
				outcome.put(word, positions);
			}
		}

		// Get FancyHit result -- 1 (URL subType == 0)
		ArrayList<String> urlResult = jsoup.getUrl(url);
		for (int i = 0; i < urlResult.size(); i++) {
			String word = urlResult.get(i);
			if(word.equals("") || word.equals(" ")){
				continue;
			}
			totalNumber++;
			if (outcome.containsKey(word)) {
				Hit urlHit = new FancyHit(1, i, 0);
				outcome.get(word).add(urlHit);
			} else {
				ArrayList<Hit> positions = new ArrayList<Hit>();
				Hit urlHit = new FancyHit(1, i, 0);
				positions.add(urlHit);
				outcome.put(word, positions);
			}
		}

		// Get FancyHit result -- 1 (title subType == 1)
		ArrayList<ArrayList<String>> fancyTitleResult = jsoup.getTitle();
		int fancyTitleIndex = 0;
		for (int i = 0; i < fancyTitleResult.size(); i++) {
			ArrayList<String> words = fancyTitleResult.get(i);

			for (String word : words) {
				if(word.equals("") || word.equals(" ")){
					continue;
				}
				totalNumber++;
				if (outcome.containsKey(word)) {
					Hit titleHit = new FancyHit(1, fancyTitleIndex, 1);
					fancyTitleIndex++;
					outcome.get(word).add(titleHit);
				} else {
					ArrayList<Hit> positions = new ArrayList<Hit>();
					Hit titleHit = new FancyHit(1, fancyTitleIndex, 1);
					fancyTitleIndex++;
					positions.add(titleHit);
					outcome.put(word, positions);
				}
			}
			fancyTitleIndex = fancyTitleIndex + 10;
		}

		// Get FancyHit result -- 1 (meta subType == 2)
		ArrayList<ArrayList<String>> fancyMetaResult = jsoup.getMeta();
		int fancyMetaIndex = 0;
		for (int i = 0; i < fancyMetaResult.size(); i++) {
			ArrayList<String> words = fancyMetaResult.get(i);

			for (String word : words) {
				if(word.equals("") || word.equals(" ")){
					continue;
				}
				totalNumber++;
				if (outcome.containsKey(word)) {
					Hit metaHit = new FancyHit(1, fancyMetaIndex, 2);
					fancyMetaIndex++;
					outcome.get(word).add(metaHit);
				} else {
					ArrayList<Hit> positions = new ArrayList<Hit>();
					Hit metaHit = new FancyHit(1, fancyMetaIndex, 2);
					fancyMetaIndex++;
					positions.add(metaHit);
					outcome.put(word, positions);
				}
			}
			fancyMetaIndex = fancyMetaIndex + 10;
		}

		// Get FancyHit result -- 1 (htag subType == 3)
		HashMap<ArrayList<String>, Integer> fancyHtagResult = jsoup.getHtag();
		int fancyHtagIndex = 0;
		for (ArrayList<String> words : fancyHtagResult.keySet()) {
			int fontSize = fancyHtagResult.get(words);

			for (String word : words) {
				if(word.equals("") || word.equals(" ")){
					continue;
				}
				totalNumber++;
				if (outcome.containsKey(word)) {
					Hit hTagHit = new FancyHit(fontSize, fancyHtagIndex, 3);
					fancyHtagIndex++;
					outcome.get(word).add(hTagHit);
				} else {
					ArrayList<Hit> positions = new ArrayList<Hit>();
					Hit hTagHit = new FancyHit(fontSize, fancyHtagIndex, 3);
					fancyHtagIndex++;
					positions.add(hTagHit);
					outcome.put(word, positions);
				}
			}
			fancyHtagIndex = fancyHtagIndex + 10;
		}
		
		

		// Get AnchorHit result -- 2
		ArrayList<ArrayList<String>> anchorResult = jsoup.getAnchor();
		int anchorIndex = 0;
		for (int i = 0; i < anchorResult.size(); i++) {
			ArrayList<String> words = anchorResult.get(i);

			for (String word : words) {
				if(word.equals("") || word.equals(" ")){
					continue;
				}
				totalNumber++;
				if (outcome.containsKey(word)) {
					Hit anchorHit = new AnchorHit(1, anchorIndex);
					anchorIndex++;
					outcome.get(word).add(anchorHit);
				} else {
					ArrayList<Hit> positions = new ArrayList<Hit>();
					Hit anchorHit = new AnchorHit(1, anchorIndex);
					anchorIndex++;
					positions.add(anchorHit);
					outcome.put(word, positions);
				}
			}
			anchorIndex = anchorIndex + 10;
		}
	}
	
	private int maxOccur(){
		int maxOccurNumber = Integer.MIN_VALUE;
		String maxWord = null;
		for (String word : outcome.keySet()) {
			if(maxOccurNumber < outcome.get(word).size()){
				maxOccurNumber = outcome.get(word).size();
				maxWord = word;
			}
		}
//		System.out.println("maxWord: " + maxWord);
		return maxOccurNumber;
		
		
//		for (ArrayList<Hit> hitList : outcome.values()) {
//		maxOccurNumber = Math.max(maxOccurNumber, hitList.size());
//	}
	}

	
	public String serialize() {
		generateHitList();
		int maxOccurNumber = maxOccur();
		
//		System.out.println("maxOccurNumber: " + maxOccurNumber);
//		System.out.println("totalNumber: " + totalNumber);
		
		StringBuilder sb = new StringBuilder();

		for (String word : outcome.keySet()) {
			ArrayList<Hit> temp = outcome.get(word);
			
			int numOccur = temp.size();
			double eachTF = 0;
			eachTF = 0.5 + (0.5 * (double)(numOccur)) / maxOccurNumber;
			
			eachTF = Math.rint(eachTF * 1000000) / 1000000;
			
			sb.append(word + "|");
			sb.append(docId + "-");
			for (Hit eachhit : temp) {
				sb.append(eachhit.toString() + ":");
			}
			// delete redundant ":"
			sb.deleteCharAt(sb.length() - 1);
			sb.append("-" + eachTF);
			sb.append(";");
		}
		return sb.toString();
	}
}