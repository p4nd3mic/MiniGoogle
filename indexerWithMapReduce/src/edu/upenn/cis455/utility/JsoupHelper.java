package edu.upenn.cis455.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;

public class JsoupHelper {
	private Document doc;
//	private StanfordLemmatizer slem;
	private Pattern pattern = Pattern.compile("\\b[A-Za-z0-9]+\\b");
	private Pattern pattern2 = Pattern.compile("\\b[A-Za-z]+\\b");

	private Pattern phonePattern = Pattern
			.compile("(?:\\([2-9]\\d{2}\\)\\ ?|[2-9]\\d{2}(?:\\-?|\\ ?))[2-9]\\d{2}[- ]?\\d{4}");
	private Pattern codePattern = Pattern.compile("\\d{5}");

	private DatabaseWrapper wrapper;

	public JsoupHelper(String html) {
		this.doc = Jsoup.parse(html);
//		this.slem = new StanfordLemmatizer();

		this.wrapper = DBSingleton.getInstance().getWrapper();

	}

	public String getText() {
		return doc.text();
	}

	// For PlainHit
	public ArrayList<String> getParagraph() {
		StringBuilder temp = new StringBuilder();
		StringBuilder result = new StringBuilder();
		for (Element element : doc.select("p")) {
			// System.out.println(element.text());
			temp.append(element.text().toLowerCase() + " ");
		}
//		Matcher matcher = pattern.matcher(temp.toString());
//		while (matcher.find()) {
//			String one = matcher.group();
//			result.append(one + " ");
//		}
		List<String> list = Arrays.asList(temp.toString().split("(?<=\\G(?>\\w+|\\W))\\s*"));
		ArrayList<String> store = new ArrayList<String>(list);
		
//		ArrayList<String> store = new ArrayList<String>(slem.lemmatize(result
//				.toString()));
		
		return store;
	}

	// For FancyHit -- 0
	public ArrayList<String> getUrl(String url) {
		StringBuilder result = new StringBuilder();

		Matcher matcher = pattern2.matcher(url);
		while (matcher.find()) {
			String one = matcher.group().toLowerCase();
			result.append(one + " ");
		}
		List<String> list = Arrays.asList(result.toString().split(" "));
		ArrayList<String> store = new ArrayList<String>(list);
		
//		ArrayList<String> store = new ArrayList<String>(slem.lemmatize(result
//				.toString()));
		return store;
	}

	// For FancyHit -- 1
	public ArrayList<ArrayList<String>> getTitle() {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		ArrayList<String> store = new ArrayList<String>();
		for (Element element : doc.select("title")) {
			// System.out.println("title: " + element.text());
			StringBuilder temp = new StringBuilder();
			Matcher matcher = pattern.matcher(element.text().toLowerCase());
			while (matcher.find()) {
				String one = matcher.group();
				temp.append(one + " ");
			}
			String tempStr = temp.toString();
			if (tempStr.equals("") || tempStr.equals(" ")) {
				continue;
			}
			// System.out.println(temp);
			
			List<String> list = Arrays.asList(tempStr.toString().split(" "));
			store = new ArrayList<String>(list);
			
//			store = new ArrayList<String>(slem.lemmatize(tempStr));
			result.add(store);
		}
		
		return result;
	}

	// For FancyHit -- 2
	public ArrayList<ArrayList<String>> getMeta() {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		ArrayList<String> store = new ArrayList<String>();

		for (Element meta : doc.select("meta")) {
			for (Attribute attri : meta.attributes()) {
				// System.out.println("key: " + attri.getKey() + " - value: " +
				// attri.getValue());
				StringBuilder temp = new StringBuilder();
				Matcher matcher = pattern2.matcher(attri.getValue());

				while (matcher.find()) {
					String one = matcher.group().toLowerCase();
					temp.append(one + " ");
				}
				String tempStr = temp.toString();
				if (tempStr.equals("") || tempStr.equals(" ")) {
					continue;
				}
				
				List<String> list = Arrays.asList(tempStr.toString().split(" "));
				store = new ArrayList<String>(list);
				
//				store = new ArrayList<String>(slem.lemmatize(tempStr));
				result.add(store);
			}
		}
		return result;
	}

	// For FancyHit -- 3
	public HashMap<ArrayList<String>, Integer> getHtag() {
		ArrayList<String> store = new ArrayList<String>();
		HashMap<ArrayList<String>, Integer> result = new HashMap<ArrayList<String>, Integer>();

		for (Element element : doc.select("h1")) {
			// System.out.println("h1: " + element.text());
			StringBuilder temp = new StringBuilder();
			Matcher matcher = pattern.matcher(element.text().toLowerCase());
			while (matcher.find()) {
				String one = matcher.group();
				temp.append(one + " ");
			}
			String tempStr = temp.toString();
			if (tempStr.equals("") || tempStr.equals(" ")) {
				continue;
			}
			
			List<String> list = Arrays.asList(tempStr.toString().split(" "));
			store = new ArrayList<String>(list);
			
//			store = new ArrayList<String>(slem.lemmatize(tempStr));
			result.put(store, 6);

			store = new ArrayList<String>();
		}

		for (Element element : doc.select("h2")) {
			// System.out.println("h2: " + element.text());
			StringBuilder temp = new StringBuilder();
			Matcher matcher = pattern.matcher(element.text().toLowerCase());
			while (matcher.find()) {
				String one = matcher.group();
				temp.append(one + " ");
			}
			String tempStr = temp.toString();
			if (tempStr.equals("") || tempStr.equals(" ")) {
				continue;
			}
			
			List<String> list = Arrays.asList(tempStr.toString().split(" "));
			store = new ArrayList<String>(list);
			
//			store = new ArrayList<String>(slem.lemmatize(tempStr));
			
			result.put(store, 5);
			store = new ArrayList<String>();
		}

		for (Element element : doc.select("h3")) {
			// System.out.println("h3: " + element.text());
			StringBuilder temp = new StringBuilder();
			Matcher matcher = pattern.matcher(element.text().toLowerCase());
			while (matcher.find()) {
				String one = matcher.group();
				temp.append(one + " ");
			}
			String tempStr = temp.toString();
			if (tempStr.equals("") || tempStr.equals(" ")) {
				continue;
			}
			
			List<String> list = Arrays.asList(tempStr.toString().split(" "));
			store = new ArrayList<String>(list);
			
//			store = new ArrayList<String>(slem.lemmatize(tempStr));
			result.put(store, 4);

			store = new ArrayList<String>();
		}

		for (Element element : doc.select("h4")) {
			// System.out.println("h4: " + element.text());
			StringBuilder temp = new StringBuilder();
			Matcher matcher = pattern.matcher(element.text().toLowerCase());
			while (matcher.find()) {
				String one = matcher.group();
				temp.append(one + " ");
			}
			String tempStr = temp.toString();
			if (tempStr.equals("") || tempStr.equals(" ")) {
				continue;
			}
			
			List<String> list = Arrays.asList(tempStr.toString().split(" "));
			store = new ArrayList<String>(list);
			
//			store = new ArrayList<String>(slem.lemmatize(tempStr));
			result.put(store, 3);
			store = new ArrayList<String>();
		}

		for (Element element : doc.select("h5")) {
			// System.out.println("h5: " + element.text());
			StringBuilder temp = new StringBuilder();
			Matcher matcher = pattern.matcher(element.text().toLowerCase());
			while (matcher.find()) {
				String one = matcher.group();
				temp.append(one + " ");
			}
			String tempStr = temp.toString();
			if (tempStr.equals("") || tempStr.equals(" ")) {
				continue;
			}
			
			List<String> list = Arrays.asList(tempStr.toString().split(" "));
			store = new ArrayList<String>(list);
			
//			store = new ArrayList<String>(slem.lemmatize(tempStr));
			result.put(store, 2);

			store = new ArrayList<String>();
		}

		for (Element element : doc.select("h6")) {
			// System.out.println("h6: " + element.text());
			StringBuilder temp = new StringBuilder();
			Matcher matcher = pattern.matcher(element.text().toLowerCase());
			while (matcher.find()) {
				String one = matcher.group();
				temp.append(one + " ");
			}
			String tempStr = temp.toString();
			if (tempStr.equals("") || tempStr.equals(" ")) {
				continue;
			}
			
			List<String> list = Arrays.asList(tempStr.toString().split(" "));
			store = new ArrayList<String>(list);
			
//			store = new ArrayList<String>(slem.lemmatize(tempStr));
			result.put(store, 1);

			store = new ArrayList<String>();
		}
		
		return result;
	}

	// For AnchorHit
	public ArrayList<ArrayList<String>> getAnchor() {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		ArrayList<String> store = new ArrayList<String>();

		for (Element element : doc.select("a")) {
			// System.out.println("anchor: " + element.text());
			StringBuilder temp = new StringBuilder();
			Matcher matcher = pattern.matcher(element.text().toLowerCase());
			while (matcher.find()) {
				String one = matcher.group();
				temp.append(one + " ");
			}
			String tempStr = temp.toString();
			if (tempStr.equals("") || tempStr.equals(" ")) {
				continue;
			}

			List<String> list = Arrays.asList(tempStr.toString().split(" "));
			store = new ArrayList<String>(list);
			
//			store = new ArrayList<String>(slem.lemmatize(tempStr));
			result.add(store);
		}
		return result;
	}

	public String getHtmlTitle() {
		Element element = doc.select("title").first();
		StringBuilder result = new StringBuilder();
		if(element != null){
			List<String> wordList = Arrays.asList(element.text().split("(?<=\\G(?>\\w+|\\W))\\s*"));
			for (String str : wordList) {
				result.append(str + " ");
			}
			return result.toString();
		}else{
			return "No Title Name";
		}
	}

	public ArrayList<String> getParagraphText() {
		StringBuilder temp = new StringBuilder();
		for (Element element : doc.select("p")) {
			// System.out.println(element.text());
			temp.append(element.text() + " ");
		}
		List<String> wordList = Arrays.asList(temp.toString().split("(?<=\\G(?>\\w+|\\W))\\s*"));
		return new ArrayList<String>(wordList);
	}

	// Heuristic helper
	public String getGeolocation() {
		// For HTML5 <address> tag
		StringBuilder temp = new StringBuilder();
		for (Element element : doc.select("address")) {
			// System.out.println(element.text());
			temp.append(element.text().toLowerCase() + " ");
		}
		ArrayList<Object> candidateCity = getCandidateCityName(temp.toString());

		// For postCode heuristic
		String content = getGeoHelper();
		ArrayList<Object> candidateCity2 = getCandidateCityName(content);

		if(candidateCity.size() == 0 && candidateCity2.size() == 0){
			return "N";
		}else if(candidateCity.size() == 0 && candidateCity2.size() != 0){
			return (String)candidateCity2.get(0);
		}else if(candidateCity.size() != 0 && candidateCity2.size() == 0){
			return (String)candidateCity.get(0);
		}else{
			if ((Integer) candidateCity.get(1) >= (Integer) candidateCity2.get(1)) {
				return (String)candidateCity.get(0);
			}else{
				return (String)candidateCity2.get(0);
			}
		}
	}

	private ArrayList<Object> getCandidateCityName(String content) {
		ArrayList<Object> results = new ArrayList<Object>();

		Matcher matcherCode = codePattern.matcher(content);
		int maxOccurLoc = 0;
		String cityName = null;
		while (matcherCode.find()) {
			String postCode = matcherCode.group();

//			System.out.println("postCode: " + postCode);
			
			ArrayList<String> infos = new ArrayList<String>();
			if (wrapper.isExistLocDateEntity(postCode)) {
				// cityName -- 0 stateName -- 1 stateAbbr -- 2
				infos = wrapper.getLocDateInfo(postCode);
			}

			if (infos.size() > 0) {
				String currentCityName = infos.get(0);
				
				// special case for new york city -> new york
				String[] splits = currentCityName.split(" ");
				if(splits.length == 3){
					currentCityName = splits[0] + " " + splits[1];
				}
				
				
//				System.out.println("currentCityName: " + currentCityName);
				Pattern tempPattern = Pattern.compile("\\b" + currentCityName
						+ "\\b");
				Matcher matcherCityName = tempPattern.matcher(content);
				int occurLoc = 0;
				while (matcherCityName.find()) {
					occurLoc++;
				}
				if (maxOccurLoc < occurLoc) {
					maxOccurLoc = occurLoc;
					cityName = currentCityName;
				}
			}
		}

		if (cityName != null) {
			results.add(cityName);
			results.add(maxOccurLoc);
			return results;
		} else {
			return new ArrayList<Object>();
		}
	}

	private String getGeoHelper() {
		StringBuilder temp = new StringBuilder();
		for (Element element : doc.select("p")) {
			// System.out.println(element.text());
			temp.append(element.text().toLowerCase() + " ");
		}
		return temp.toString();
	}

}