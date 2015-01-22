package edu.upenn.cis455.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.upenn.cis455.hit.*;

public class GenerateHitListForImage {
	private Pattern pattern = Pattern.compile("\\b[0-9A-Za-z]+\\b");
	private HashMap<String, ArrayList<Hit>> outcome;

	private String url;
	private String pageUrl;
	private String type;
	private String description;

	public GenerateHitListForImage(String url, String pageUrl, String type,
			String description) {
		this.url = url;
		this.pageUrl = pageUrl;
		this.type = type;
		this.description = description;
		this.outcome = new HashMap<String, ArrayList<Hit>>();
	}

	public String serialize() {
		generateHitList();
//		System.out.println("serialize: " + outcome);
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
			sb.append(pageUrl + "<<<->>>");
			sb.append(type + "<<<->>>");
			sb.append(description + "<<<->>>");
			for (Hit eachhit : temp) {
				sb.append(eachhit.toString() + "<<<:>>>");
			}
			// delete redundant ":"
			sb.delete(sb.length() - 7, sb.length());
			sb.append("<<<->>>" + eachTF);
			sb.append("<<<;>>>");
		}
		return sb.toString();
	}

	private ArrayList<String> words() {
		StringBuilder result = new StringBuilder();
		Matcher matcher = pattern.matcher(description);
		while (matcher.find()) {
			String one = matcher.group().toLowerCase();
			result.append(one + " ");
		}
		List<String> list = Arrays.asList(result.toString().split(" "));
		ArrayList<String> store = new ArrayList<String>(list);
		return store;
	}

	private void generateHitList() {
		ArrayList<String> descriptionResult = words();
		for (int i = 0; i < descriptionResult.size(); i++) {
			String word = descriptionResult.get(i);
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
