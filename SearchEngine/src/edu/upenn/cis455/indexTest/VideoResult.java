package edu.upenn.cis455.indexTest;

import java.io.File;
import java.util.HashSet;

import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;
import edu.upenn.cis455.indexer.*;
import edu.upenn.cis455.utility.Stemmer;

public class VideoResult {
	public static void main(String[] args) {
		File dbPath = new File("/home/cis455/se/storage1/",
				Indexer.PATH_DB_DIR);
		DBSingleton.setDbPath(dbPath.getAbsolutePath());
		String keyword = "white";
//		DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();
//		wrapper.traverseInvertedForVideo();
		
		Stemmer stemmer = null;
		char chs[] = keyword.toCharArray();
		stemmer = new Stemmer();
		stemmer.add(chs, chs.length);
		stemmer.stem();

		String modifiedWord = stemmer.toString();

		KeyWordSearchVideo search = new KeyWordSearchVideo(keyword);
		String result = search.getResultSet();
		if (result != null) {
			System.out.println(result);
		}
		DBSingleton.getInstance().closeBDBstore();
	}
}
