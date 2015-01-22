package edu.upenn.cis455.spellchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AddWordsToDic {
	public static void main(String[] args) throws Exception {
		DBSingleton.setDbPath("checker_db");
		DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();
		addWordToDb(wrapper, new File("wordlist.txt"));
		System.out.println("Finish...");
		DBSingleton.getInstance().closeBDBstore();
	}
	
	public static void addWordToDb(DatabaseWrapper wrapper, File wordlistFile)
			throws IOException {
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(wordlistFile));

		String sCurrentLine;
		String prefix = "a";
		ArrayList<Integer> indexSet = new ArrayList<Integer>();

		while ((sCurrentLine = br.readLine()) != null) {
			String word = sCurrentLine.toLowerCase();
			String currentPrefix = word.substring(0, 1);

			if (!currentPrefix.equals(prefix)) {
				wrapper.addDictionaryIndexEntity(prefix, indexSet);

				prefix = currentPrefix;
				indexSet = new ArrayList<Integer>();
				indexSet.add(wrapper.addDictionaryEntity(word));
			} else {
				int index = wrapper.addDictionaryEntity(word);
				indexSet.add(index);
			}
		}
		wrapper.addDictionaryIndexEntity(prefix, indexSet);
		
		if (br != null)
			br.close();
	}
}