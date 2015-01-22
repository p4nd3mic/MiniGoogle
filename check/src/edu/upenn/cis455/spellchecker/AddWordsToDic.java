package edu.upenn.cis455.spellchecker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class AddWordsToDic {
	public static void main(String[] args) throws Exception {
		DBSingleton.setDbPath("/home/cis455/info/checkerDB");

		DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();

		BufferedReader br = null;
		br = new BufferedReader(new FileReader("wordlist.txt"));

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
		System.out.println("Finish...");
		DBSingleton.getInstance().closeBDBstore();
	}
}