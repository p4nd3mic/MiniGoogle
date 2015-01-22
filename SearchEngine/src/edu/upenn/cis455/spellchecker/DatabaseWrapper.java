package edu.upenn.cis455.spellchecker;

import java.util.ArrayList;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;


public class DatabaseWrapper {
	// PrimaryIndex
	private PrimaryIndex<Integer, DictionaryEntity> dicByWord;
	private PrimaryIndex<String, DictionaryIndexEntity> dicIndexByPre;

	// SecondaryKey
	private SecondaryIndex<String, Integer, DictionaryEntity> dicByContent;

	public DatabaseWrapper(EntityStore store) throws DatabaseException {
		dicByWord = store
				.getPrimaryIndex(Integer.class, DictionaryEntity.class);
		dicIndexByPre = store.getPrimaryIndex(String.class,
				DictionaryIndexEntity.class);
		dicByContent = store.getSecondaryIndex(dicByWord, String.class, "content");
	}

	/*
	 * DictionaryEntity OPERATIONS
	 */
	public int addDictionaryEntity(String content) {
		DictionaryEntity entity = new DictionaryEntity(content);
		dicByWord.putNoReturn(entity);
		return entity.getIndex();
	}
	
	public void getHit(String content){
		EntityCursor<DictionaryEntity> dic_cursor =
				dicByContent.subIndex(content).entities();
		try {
			for (DictionaryEntity temp : dic_cursor) {
				System.out.println(temp.getHit());
			}
			// Always make sure the cursor is closed when we are done with it.
		} finally {
			dic_cursor.close();
		}
	}
	
	
	public void increaseHit(String content){
		EntityCursor<DictionaryEntity> dic_cursor =
				dicByContent.subIndex(content).entities();
		try {
			for (DictionaryEntity temp : dic_cursor) {
				temp.increaseHit();
				dicByWord.putNoReturn(temp);
			}
			// Always make sure the cursor is closed when we are done with it.
		} finally {
			dic_cursor.close();
		}
	}
	

	public ArrayList<WordTemp> getWordTemps(ArrayList<Integer> indexSet) {
		ArrayList<WordTemp> result = new ArrayList<WordTemp>();
		
		for (int index : indexSet) {
			WordTemp temp = getWordTemp(index);
			if (temp != null) {
				result.add(temp);
			}
		}
		return result;
	}

	private WordTemp getWordTemp(int index) {
		DictionaryEntity entity = getDictionaryEntity(index);
		if (entity != null) {
			WordTemp temp = new WordTemp(entity.getContent(), entity.getHit());
			return temp;
		}else{
			return null;
		}
	}

	private DictionaryEntity getDictionaryEntity(int index) {
		return dicByWord.get(index);
	}

	/*
	 * DictionaryIndexEntity OPERATIONS
	 */
	public void addDictionaryIndexEntity(String prefix,
			ArrayList<Integer> wordsIndex) {
		DictionaryIndexEntity entity = new DictionaryIndexEntity(prefix,
				wordsIndex);
		dicIndexByPre.putNoReturn(entity);
	}

	public ArrayList<Integer> getWordsIndex(String prefix) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		DictionaryIndexEntity entity = getDictionaryIndexEntity(prefix);
		if (entity != null) {
			result.addAll(entity.getWordsIndex());
		}
		return result;
	}

	private DictionaryIndexEntity getDictionaryIndexEntity(String prefix) {
		return dicIndexByPre.get(prefix);
	}
}
