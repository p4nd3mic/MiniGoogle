package edu.upenn.cis455.indexTest;

import java.util.HashSet;

import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;
import edu.upenn.cis455.indexer.*;
import edu.upenn.cis455.utility.Stemmer;

public class VideoResult {
	public static void main(String[] args) {
		DBSingleton.setDbPath("/home/cloudera/Desktop/DB");
		String keyword = "dog";

		Stemmer stemmer = null;
		char chs[] = keyword.toCharArray();
		stemmer = new Stemmer();
		stemmer.add(chs, chs.length);
		stemmer.stem();

		String modifiedWord = stemmer.toString();

		KeyWordSearchVideo search = new KeyWordSearchVideo(keyword,
				modifiedWord);
		String result = search.getResultSet();
		if (result != null) {
			System.out.println(result);
		}
		DBSingleton.getInstance().closeBDBstore();
	}
}
