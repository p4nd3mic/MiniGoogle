package edu.upenn.cis455.indexTest;


import java.io.File;

import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;
import edu.upenn.cis455.indexer.Indexer;
import edu.upenn.cis455.indexer.KeyWordSearchDocument;
import edu.upenn.cis455.util.StringUtil;
import edu.upenn.cis455.utility.Stemmer;

public class DocumentResult {
	public static void main(String[] args) {
		File dbPath = new File("/home/cis455/se/storage1/", Indexer.PATH_DB_DIR);
		DBSingleton.setDbPath(dbPath.getAbsolutePath());
		DBSingleton db = DBSingleton.getInstance();
		DatabaseWrapper wrapper = db.getWrapper();

		wrapper.traverseInverted();
		
		String keyword = "sangkee";

		KeyWordSearchDocument search = new KeyWordSearchDocument(keyword);
		
		String result = search.getResultSet();
		if (result != null) {
			System.out.println(result);
		}
		
		DBSingleton.getInstance().closeBDBstore();
	}
}
