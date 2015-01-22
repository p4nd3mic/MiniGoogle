package edu.upenn.cis455.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.upenn.cis455.hit.*;

public class GenerateHitListForVideo {
	private String url;
	private String type;
	private String title;
	private Pattern pattern = Pattern.compile("\\b[0-9A-Za-z]+\\b");
	private HashMap<String, ArrayList<Hit>> outcome;

	public GenerateHitListForVideo(String url, String type, String title) {
		this.url = url;
		this.type = type;
		this.title = title;
		this.outcome = new HashMap<String, ArrayList<Hit>>();
	}

	public String serialize() {
		generateHitList();
		// System.out.println("serialize: " + outcome);
		int maxOccurNumber = maxOccur();
		StringBuilder sb = new StringBuilder();

		for (String word : outcome.keySet()) {
			ArrayList<Hit> temp = outcome.get(word);

			int numOccur = temp.size();
			double eachTF = 0;
			eachTF = 0.5 + (0.5 * (double) (numOccur)) / maxOccurNumber;

			eachTF = Math.rint(eachTF * 1000000) / 1000000;

			sb.append(word + "<<<|>>>");
			sb.append(url + "<<<->>>");
			sb.append(type + "<<<->>>");
			sb.append(title + "<<<->>>");
			for (Hit eachhit : temp) {
				sb.append(eachhit.toString() + "<<<:>>>");
			}
			// delete redundant ":"
			sb.deleteCharAt(sb.length() - 1);
			sb.append("<<<->>>" + eachTF);
			sb.append("<<<;>>>");
		}
		return sb.toString();
	}

	private ArrayList<String> titleWords() {
		StringBuilder result = new StringBuilder();
		Matcher matcher = pattern.matcher(title);
		while (matcher.find()) {
			String one = matcher.group().toLowerCase();
			result.append(one + " ");
		}
		List<String> list = Arrays.asList(result.toString().split(" "));
		ArrayList<String> store = new ArrayList<String>(list);
		StringBuilder tit = new StringBuilder();
		for (String str : store) {
			tit.append(str + " ");
		}
		title = tit.toString();
		return store;
	}

	private void generateHitList() {
		ArrayList<String> titleResult = titleWords();
		for (int i = 0; i < titleResult.size(); i++) {
			String word = titleResult.get(i);
			if (word.equals("") || word.equals(" ")) {
				continue;
			}
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
	}

	private int maxOccur() {
		int maxOccurNumber = Integer.MIN_VALUE;
		String maxWord = null;
		for (String word : outcome.keySet()) {
			if (maxOccurNumber < outcome.get(word).size()) {
				maxOccurNumber = outcome.get(word).size();
				maxWord = word;
			}
		}
		return maxOccurNumber;
	}
}
