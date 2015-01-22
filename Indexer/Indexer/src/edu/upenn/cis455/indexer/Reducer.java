package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.HashMap;

import com.sleepycat.persist.EntityStore;

public class Reducer {
	public void reducer(String word, ArrayList<String> infos) {

		BDBstore db = new BDBstore("/Users/xuxu/Desktop/db");
		EntityStore store = db.getEntityStore();
		DatabaseWrapper wrapper = new DatabaseWrapper(store);

		boolean isExist = wrapper.isExistWordEntity(word);

		// WordEntity operations
		if (isExist) {
			
			// Get invertedIndexList of this word first
			ArrayList<Integer> invertedIndexOldList = wrapper.getInvertedIndexList(word);
			ArrayList<Integer> invertedIndexNewList = new ArrayList<Integer>();
			// Get the (docId : InvertedIndex)
			HashMap<Integer, Integer> docIdToIndex = wrapper.getDocIdToIndex(invertedIndexOldList);
			
			for (String info : infos) {
				String[] parts = info.split("-");

				System.out.println("docId: " + parts[0] + " " + "hitInfo: "
						+ parts[1] + " " + "TF: " + parts[2]);
				
				int docId = Integer.valueOf(parts[0]);
				double TF = Double.valueOf(parts[2]);
				
				String[] hitsStr = parts[1].split(":");
				ArrayList<Hit> hitList = deserializeHelper(hitsStr);
				
				if(docIdToIndex.containsKey(docId)){
					int invertedIndex = docIdToIndex.get(docId);
					boolean success = wrapper.updateInvertedIndexEntity(invertedIndex, hitList, TF);
					System.out.println("invertedIndexEntity update success?: " + success);
				}else{
					int invertedIndex = wrapper.addInvertedIndexEntity(docId, hitList, TF);
					invertedIndexNewList.add(invertedIndex);
				}
				
			}
			
			boolean success = wrapper.updateWordEntity(word, invertedIndexNewList);
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
				
				
				
				int invertedIndex = wrapper.addInvertedIndexEntity(docId, hitList, TF);
				invertedIndexList.add(invertedIndex);
			}
			
			boolean success = wrapper.addWordEntity(word, invertedIndexList);
			System.out.println("new WordEntity success?: " + success);
		}
		
		db.close();

	}
	
	
	public ArrayList<Hit> deserializeHelper(String[] hitsStr){
		ArrayList<Hit> hitList = new ArrayList<Hit>();
		for (String hitStr : hitsStr) {
			Hit hit;
			if(hitStr.startsWith("0#")){
				hit = new PlainHit();
				hit.deserialize(hitStr);
			}else if(hitStr.startsWith("1#")){
				hit = new FancyHit();
				hit.deserialize(hitStr);
			}else{
				hit = new AnchorHit();
				hit.deserialize(hitStr);
			}
			hitList.add(hit);
		}
		return hitList;
	}

	public static void main(String[] args) {
		String word = "first";
		ArrayList<String> info = new ArrayList<String>();

		info.add("1950071188-0#17#1:0#76#1:0#185#1:0#794#1:0#1990#1:0#2032#1-0.516304");
		info.add("1174896541-0#13#1:0#21#1:0#29#1:0#38#1:0#92#1:0#98#1:0#122#1:0#148#1:0#194#1:0#224#1:0#233#1:0#251#1:0#257#1:0#269#1:0#278#1:1#112#1#2:1#12#5#3:2#60#1:2#165#1:2#179#1:2#242#1:2#336#1:2#394#1:2#409#1:2#580#1:2#778#1:2#856#1:2#1003#1:2#1050#1:2#1086#1:2#1241#1:2#1256#1:2#1285#1:2#1334#1:2#1405#1:2#1437#1:2#1481#1:2#1497#1:2#1501#1:2#1514#1:2#1530#1:2#1547#1:2#1562#1:2#1610#1-1.0");
//		info.add("1950071999-0#912#1-0.502717");

		Reducer r = new Reducer();
		r.reducer(word, info);

	}
}
