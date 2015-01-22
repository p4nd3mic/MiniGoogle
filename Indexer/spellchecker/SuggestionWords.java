package edu.upenn.cis455.spellchecker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

public class SuggestionWords {
	private String prefix;
	private DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();
	private ArrayList<WordTemp> words;

	public SuggestionWords() {
		prefix = "a";
		words = wrapper.getWordTemps(wrapper.getWordsIndex(prefix));
	}

	public ArrayList<String> getWordsResult(String word) {
		String prefix = word.substring(0, 1);
		if (this.prefix.equals(prefix)) {
			return results(word);
		} else {
			this.prefix = prefix;
			words = wrapper.getWordTemps(wrapper.getWordsIndex(prefix));
			return results(word);
		}
	}

	private ArrayList<String> results(String word) {
		PriorityQueue<WordDis> queue = new PriorityQueue<WordDis>();
		ArrayList<String> result = new ArrayList<String>();
		for (WordTemp temp : words) {
			EditDistance distance = new EditDistance(word, temp.getContent());
			queue.add(new WordDis(temp, distance.getMinDistance()));
		}
		int i = 0;
		for (WordDis wordDis : queue) {

			result.add(wordDis.getWordTemp().getContent());
			if (i >= 5) {
				break;
			}
			i++;
		}
		return result;
	}

}
