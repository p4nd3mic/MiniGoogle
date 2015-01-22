package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.HashMap;

public class Reducer {
	public void reducer(String keyword, ArrayList<String> infos) {
		DBSingleton.setDbPath("/Users/xuxu/Desktop/db");
		DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();

		Stemmer stemmer = null;
		char chs[] = keyword.toCharArray();
		stemmer = new Stemmer();
		stemmer.add(chs, chs.length);
		stemmer.stem();

		String modifiedWord = stemmer.toString();

		System.out.println("Original word: " + keyword + " : " + "Modified word: "
				+ modifiedWord);

		ArrayList<String> candidates = new ArrayList<String>();
		candidates.add(keyword);
		candidates.add(modifiedWord);

		for (String word : candidates) {
			boolean isExist = wrapper.isExistWordEntity(word);
			// WordEntity operations
			if (isExist) {

				// Get invertedIndexList of this word first
				ArrayList<Integer> invertedIndexOldList = wrapper
						.getInvertedIndexList(word);
				ArrayList<Integer> invertedIndexNewList = new ArrayList<Integer>();
				// Get the (docId : InvertedIndex)
				HashMap<Integer, Integer> docIdToIndex = wrapper
						.getDocIdToIndex(invertedIndexOldList);

				for (String info : infos) {
					String[] parts = info.split("-");

					System.out.println("docId: " + parts[0] + " " + "hitInfo: "
							+ parts[1] + " " + "TF: " + parts[2]);

					int docId = Integer.valueOf(parts[0]);
					double TF = Double.valueOf(parts[2]);

					String[] hitsStr = parts[1].split(":");
					ArrayList<Hit> hitList = deserializeHelper(hitsStr);

					if (docIdToIndex.containsKey(docId)) {
						int invertedIndex = docIdToIndex.get(docId);
						boolean success = wrapper.updateInvertedIndexEntity(
								invertedIndex, hitList, TF);
						System.out
								.println("invertedIndexEntity update success?: "
										+ success);
					} else {
						int invertedIndex = wrapper.addInvertedIndexEntity(
								docId, hitList, TF);
						invertedIndexNewList.add(invertedIndex);
					}

				}

				boolean success = wrapper.updateWordEntity(word,
						invertedIndexNewList);
				System.out.println("update WordEnity success?: " + success);

			} else {

				ArrayList<Integer> invertedIndexList = new ArrayList<Integer>();

				for (String info : infos) {
					String[] parts = info.split("-");

					System.out.println("docId: " + parts[0] + " " + "hitInfo: "
							+ parts[1] + " " + "TF: " + parts[2]);

					int docId = Integer.valueOf(parts[0]);
					double TF = Double.valueOf(parts[2]);

					// deserialize hitListStr and get hitList
					String[] hitsStr = parts[1].split(":");
					ArrayList<Hit> hitList = deserializeHelper(hitsStr);

					int invertedIndex = wrapper.addInvertedIndexEntity(docId,
							hitList, TF);
					invertedIndexList.add(invertedIndex);
				}

				boolean success = wrapper
						.addWordEntity(word, invertedIndexList);
				System.out.println("new WordEntity success?: " + success);
			}
		}

		DBSingleton.getInstance().closeBDBstore();

	}

	public ArrayList<Hit> deserializeHelper(String[] hitsStr) {
		ArrayList<Hit> hitList = new ArrayList<Hit>();
		for (String hitStr : hitsStr) {
			Hit hit;
			if (hitStr.startsWith("0#")) {
				hit = new PlainHit();
				hit.deserialize(hitStr);
			} else if (hitStr.startsWith("1#")) {
				hit = new FancyHit();
				hit.deserialize(hitStr);
			} else {
				hit = new AnchorHit();
				hit.deserialize(hitStr);
			}
			hitList.add(hit);
		}
		return hitList;
	}

	public static void main(String[] args) {
		String word = "dumplings";
		ArrayList<String> info = new ArrayList<String>();

		info.add("656557728-0#33#1:1#201#1#2-0.583333");

		Reducer r = new Reducer();
		r.reducer(word, info);

	}
}
