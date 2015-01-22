package edu.upenn.cis455.indexTest;


import edu.upenn.cis455.indexStorage.DBSingleton;
import edu.upenn.cis455.indexStorage.DatabaseWrapper;
import edu.upenn.cis455.indexer.KeyWordSearchDocument;
import edu.upenn.cis455.utility.Stemmer;

public class DocumentResult {
	public static void main(String[] args) {
		DBSingleton.setDbPath("/home/cloudera/Desktop/DB");
		DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();

		String keyword = "the";

		Stemmer stemmer = null;
		char chs[] = keyword.toCharArray();
		stemmer = new Stemmer();
		stemmer.add(chs, chs.length);
		stemmer.stem();

		String modifiedWord = stemmer.toString();

		KeyWordSearchDocument search = new KeyWordSearchDocument(keyword, modifiedWord);
		
		String result = search.getResultSet();
		if (result != null) {
			System.out.println(result);
		}
		
		DBSingleton.getInstance().closeBDBstore();
	}
}
