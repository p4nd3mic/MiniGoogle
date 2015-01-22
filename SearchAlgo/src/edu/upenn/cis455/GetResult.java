package edu.upenn.cis455;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GetResult {

	CombineResults cres = null;
	String[] keywords = null;
	String location = "";

	public GetResult() {

		this.keywords = "searching engine".split(" ");
		this.location = "philadelphia";
		cres = new CombineResults(this.keywords, this.location);
	}

	public GetResult(String searchText, String location) {
		keywords = searchText.split(" ");
		this.location = location;
		cres = new CombineResults(keywords, location);
	}

	public String readFile(String fileName) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	public ArrayList<ResultSet> getResults() {

		HashMap<String, ArrayList<String>> JsonFileResults = new HashMap<String, ArrayList<String>>();
		
		//TODO: here json file is hard-coded, need to send request for each keyword, store incoming filecontents
		//and then call cres.parseJason
		
		for (int i = 0; i < keywords.length; i++) {
			try {
				ArrayList<String> fileContents = new ArrayList<String>();
				fileContents
						.add(readFile("/home/cis455/workspace/team02/SearchAlgo/data/doc"
								+ i + ".json"));
				JsonFileResults.put(keywords[i], fileContents);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < keywords.length; i++) {
			cres.ParseJason(JsonFileResults.get(keywords[i]).get(0));
		}

		return cres.sendResults();
	}

	public ArrayList<String> getAllStemmerWords() {
		return cres.allStemmerWords();
	}

	/*
	 * public static void main(String[] args) { String location =
	 * "philadelphia"; String keyText = "searching engine"; GetResult ge= new
	 * GetResult(keyText, location);
	 * 
	 * ArrayList<ResultSet> results = ge.getResults();
	 * 
	 * for (int i = 0; i < results.size(); i++) { ResultSet rSet =
	 * results.get(i); System.out.println(rSet.title + "       " + rSet.url);
	 * System.out.println(rSet.summary); System.out.println(); }
	 * 
	 * }
	 */

}
