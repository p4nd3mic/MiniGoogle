package edu.upenn.cis455.indexTest;

import java.io.File;

import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;
import edu.upenn.cis455.indexer.Indexer;
import edu.upenn.cis455.indexer.KeyWordSearchImage;
import edu.upenn.cis455.utility.Stemmer;

public class ImageResult {
	public static void main(String[] args) {
		File dbPath = new File("/home/cis455/se/storage1/", Indexer.PATH_DB_DIR);
		DBSingleton.setDbPath(dbPath.getAbsolutePath());
		DBSingleton db = DBSingleton.getInstance();
//		DatabaseWrapper wrapper = db.getWrapper();
//		
//		wrapper.traverseInvertedForImage();
		
		String keyword = "youtube";

		Stemmer stemmer = null;
		char chs[] = keyword.toCharArray();
		stemmer = new Stemmer();
		stemmer.add(chs, chs.length);
		stemmer.stem();

		String modifiedWord = stemmer.toString();

		KeyWordSearchImage search = new KeyWordSearchImage(keyword);
		String result = search.getResultSet();
		if (result != null) {
			System.out.println(result);
		}
		
		DBSingleton.getInstance().closeBDBstore();
	}
}
