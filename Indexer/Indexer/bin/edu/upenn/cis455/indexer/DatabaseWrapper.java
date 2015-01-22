package edu.upenn.cis455.indexer;

import java.util.ArrayList;
import java.util.HashMap;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

public class DatabaseWrapper {
	// PrimaryIndex
	private PrimaryIndex<Integer, DocumentEntity> documentById;

	private PrimaryIndex<String, WordEntity> wordByContent;

	private PrimaryIndex<Integer, InvertedIndexEntity> invertedByIndex;

	private PrimaryIndex<String, LocDateEntity> locationByPostCode;

	// SecondaryIndex

	// Cursor

	public DatabaseWrapper(EntityStore store) throws DatabaseException {
		documentById = store.getPrimaryIndex(Integer.class,
				DocumentEntity.class);
		wordByContent = store.getPrimaryIndex(String.class, WordEntity.class);
		invertedByIndex = store.getPrimaryIndex(Integer.class,
				InvertedIndexEntity.class);
		locationByPostCode = store.getPrimaryIndex(String.class,
				LocDateEntity.class);
	}

	/*
	 * LocDateEntity OPERATIONS
	 */
	public boolean isExistLocDateEntity(String postCode) {
		LocDateEntity entity = getLocDateEntity(postCode);
		if (entity != null) {
			return true;
		}
		return false;
	}

	public void addLocDateEntity(String postCode, String cityName,
			String stateName, String stateAbbr, String latitude,
			String longitude) {
		LocDateEntity entity = new LocDateEntity(postCode, cityName, stateName,
				stateAbbr, latitude, longitude);
		locationByPostCode.putNoReturn(entity);
	}
	
	public ArrayList<String> getLocDateInfo(String postCode) {
		ArrayList<String> result = new ArrayList<String>();
		LocDateEntity entity = getLocDateEntity(postCode);
		if (entity != null) {
			String cityName = entity.getCityName();
			String stateName = entity.getStateName();
			String stateAbbr = entity.getStateAbbr();
			result.add(cityName);
			result.add(stateName);
			result.add(stateAbbr);
		}
		return result;
	}

	private LocDateEntity getLocDateEntity(String postCode) {
		return locationByPostCode.get(postCode);
	}

	/*
	 * DocumentEntity OPERATIONS
	 */
	public boolean isExistDocumentEntity(int docId) {
		DocumentEntity entity = getDocumentEntity(docId);
		if (entity != null) {
			return true;
		}
		return false;
	}

	// TODO: no matter exists or not, add the document -- need to re-check
	public void addDocumentEntity(int docId, String url, String title) {
		DocumentEntity entity = new DocumentEntity(docId, url, title);
		documentById.putNoReturn(entity);
	}

	public void addDocumentEntity(int docId, String url, String title,
			ArrayList<String> excerpt) {
		DocumentEntity entity = new DocumentEntity(docId, url, title, excerpt);
		documentById.putNoReturn(entity);
	}
	
	public void addDocumentEntity(int docId, String url, String title,
			ArrayList<String> excerpt, String location) {
		DocumentEntity entity = new DocumentEntity(docId, url, title, excerpt, location);
		documentById.putNoReturn(entity);
	}

	@Deprecated
	public boolean updateDocumentEntity(int docId, String title,
			ArrayList<String> excerpt) {
		DocumentEntity entity = getDocumentEntity(docId);
		if (entity != null) {
			entity.setTitle(title);
			entity.setExcerpt(excerpt);
			documentById.putNoReturn(entity);
			return true;
		} else {
			return false;
		}
	}

	public ArrayList<Object> getDocumentInfo(int docId) {
		ArrayList<Object> result = new ArrayList<Object>();
		DocumentEntity entity = getDocumentEntity(docId);
		if (entity != null) {
			String url = entity.getUrl();
			String title = entity.getTitle();
			ArrayList<String> excerpt = new ArrayList<String>(
					entity.getExcerpt());
			String location = entity.getLocation();
			result.add(url);
			result.add(title);
			result.add(excerpt);
			result.add(location);
		}
		return result;
	}

	public long getTotalDocument() {
		return documentById.count();
	}

	private DocumentEntity getDocumentEntity(int docId) {
		return documentById.get(docId);
	}

	/*
	 * WordEntity OPERATIONS
	 */
	public boolean isExistWordEntity(String content) {
		WordEntity entity = getWordEntity(content);
		if (entity != null) {
			return true;
		}
		return false;
	}

	public boolean addWordEntity(String content) {
		WordEntity entity = new WordEntity(content);
		boolean isNew = wordByContent.putNoOverwrite(entity);
		return isNew;
	}

	public boolean addWordEntity(String content,
			ArrayList<Integer> invertedIndexList) {
		WordEntity entity = new WordEntity(content, invertedIndexList);
		boolean isNew = wordByContent.putNoOverwrite(entity);
		return isNew;
	}

	public boolean updateWordEntity(String content,
			ArrayList<Integer> invertedIndexList) {
		WordEntity entity = getWordEntity(content);
		if (entity != null) {
			for (Integer invertedIndex : invertedIndexList) {
				entity.addDocId(invertedIndex);
			}
			wordByContent.putNoReturn(entity);
			return true;
		} else {
			return false;
		}
	}

	public ArrayList<Integer> getInvertedIndexList(String content) {
		return wordByContent.get(content).getinvertedIndexList();
	}

	public int getNumDocForWord(String content) {
		return wordByContent.get(content).getinvertedIndexListSize();
	}

	private WordEntity getWordEntity(String content) {
		return wordByContent.get(content);
	}

	/*
	 * InvertedIndexEntity OPERATIONS
	 */

	public boolean isExistInvertedEntity(int invertedIndex) {
		InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
		if (entity != null) {
			return true;
		}
		return false;
	}

	// IMPORTANT: USE THIS METHOD WHEN YOU ARE SURE PRIMARY KEY DOES NOT ALREADY
	// EXIST
	public int addInvertedIndexEntity(int docId, double TF) {
		InvertedIndexEntity entity = new InvertedIndexEntity(docId, TF);
		invertedByIndex.putNoReturn(entity);
		// System.out.println("getInvertedIndex: " + entity.getInvertedIndex());
		return entity.getInvertedIndex();
	}

	// IMPORTANT: USE THIS METHOD WHEN YOU ARE SURE PRIMARY KEY DOES NOT ALREADY
	// EXIST
	public int addInvertedIndexEntity(int docId, ArrayList<Hit> hitList,
			double TF) {
		InvertedIndexEntity entity = new InvertedIndexEntity(docId, hitList, TF);
		invertedByIndex.putNoReturn(entity);
		// System.out.println("getInvertedIndex: " + entity.getInvertedIndex());
		return entity.getInvertedIndex();
	}

	public boolean updateInvertedIndexEntity(int invertedIndex,
			ArrayList<Hit> hitList, double TF) {
		InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
		if (entity != null) {
			entity.setHitList(hitList);
			entity.setTF(TF);
			invertedByIndex.putNoReturn(entity);
			return true;
		} else {
			return false;
		}
	}

	public HashMap<Integer, Integer> getDocIdToIndex(
			ArrayList<Integer> invertedIndexList) {
		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (Integer invertedIndex : invertedIndexList) {
			InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
			result.put(entity.getDocId(), invertedIndex);
		}
		return result;
	}

	public String getAllInfo(String keyword) {
		ArrayList<SingleResult> singleResultList = new ArrayList<SingleResult>();

		ArrayList<Integer> invertedIndexList = getInvertedIndexList(keyword);

		for (Integer invertedIndex : invertedIndexList) {
			InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);

			int docId = entity.getDocId();
			ArrayList<Hit> hitList = entity.gethitList();
			double TF = entity.getTF();

			ArrayList<Object> docResult = getDocumentInfo(docId);

			String url = (String) docResult.get(0);
			String title = (String) docResult.get(1);
			ArrayList<String> excerpt = (ArrayList<String>) docResult.get(2);
			String location = (String)docResult.get(3);
			SingleResult singleResult = new SingleResult(url, title, excerpt,
					TF, hitList, location);
			singleResultList.add(singleResult);
		}

		// TODO : Get IDF with cooperation with other machines
		double IDF = 0.666;

		WordResult wordResult = new WordResult(keyword, IDF, singleResultList);
		wordResult.filter();

		return wordResult.toString();
	}

	private InvertedIndexEntity getInvertedIndexEntity(int invertedIndex) {
		return invertedByIndex.get(invertedIndex);
	}

	@Deprecated
	public HashMap<Integer, ArrayList<Hit>> getAllHitList(
			ArrayList<Integer> invertedList) {
		HashMap<Integer, ArrayList<Hit>> result = new HashMap<Integer, ArrayList<Hit>>();
		for (Integer invertedIndex : invertedList) {
			getHitList(invertedIndex, result);
		}
		return result;
	}

	@Deprecated
	public HashMap<Integer, Double> getAllTF(ArrayList<Integer> invertedList) {
		HashMap<Integer, Double> result = new HashMap<Integer, Double>();
		for (Integer invertedIndex : invertedList) {
			getTF(invertedIndex, result);
		}
		return result;
	}

	@Deprecated
	private void getTF(int invertedIndex, HashMap<Integer, Double> map) {
		InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
		map.put(entity.getDocId(), entity.getTF());
	}

	@Deprecated
	private void getHitList(int invertedIndex,
			HashMap<Integer, ArrayList<Hit>> map) {
		InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
		map.put(entity.getDocId(), entity.gethitList());
	}

}
