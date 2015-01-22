package edu.upenn.cis455.indexStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

import edu.upenn.cis455.hit.Hit;
import edu.upenn.cis455.indexer.SingleResultDocument;
import edu.upenn.cis455.indexer.SingleResultImage;
import edu.upenn.cis455.indexer.SingleResultVideo;
import edu.upenn.cis455.indexer.WordResultDocument;
import edu.upenn.cis455.indexer.WordResultImage;
import edu.upenn.cis455.indexer.WordResultVideo;

public class DatabaseWrapper {
	// PrimaryIndex

	// -- basic -- begin
	private PrimaryIndex<String, WordEntity> wordByContent;
	private PrimaryIndex<Integer, InvertedIndexEntity> invertedByIndex;
	private PrimaryIndex<String, LocDateEntity> locationByPostCode;
	// -- basic -- end

	// -- image -- begin
	private PrimaryIndex<String, WordEntityForImage> wordImageByContent;
	private PrimaryIndex<Integer, InvertedIndexForImageEntity> invertedByIndexForImage;
	// -- image -- end

	// -- video -- begin
	private PrimaryIndex<String, WordEntityForVideo> wordVideoByContent;
	private PrimaryIndex<Integer, InvertedIndexForVideoEntity> invertedByIndexForVideo;
	// -- video -- end

	// For debugging -- begin
	public void traverseInverted() {
		EntityCursor<InvertedIndexEntity> inverted_cursor_document = invertedByIndex.entities();
		try {
			for (InvertedIndexEntity temp : inverted_cursor_document) {
				System.out.println(temp.getOriginalWord() + " : "
						+ temp.getUrl());
			}
			// Always make sure the cursor is closed when we are done with it.
		} finally {
			inverted_cursor_document.close();
		}
	}

	public void traverseInvertedForImage() {
		EntityCursor<InvertedIndexForImageEntity> inverted_cursor_image = invertedByIndexForImage.entities();
		try {
			for (InvertedIndexForImageEntity temp : inverted_cursor_image) {
				System.out.println(temp.getOriginalWord() + " : "
						+ temp.getUrl());
			}
			// Always make sure the cursor is closed when we are done with it.
		} finally {
			inverted_cursor_image.close();
		}
	}

	public void traverseInvertedForVideo() {
		EntityCursor<InvertedIndexForVideoEntity> inverted_cursor_video = invertedByIndexForVideo.entities();
		try {
			for (InvertedIndexForVideoEntity temp : inverted_cursor_video) {
				System.out.println(temp.getOriginalWord() + " : "
						+ temp.getUrl());
			}
			// Always make sure the cursor is closed when we are done with it.
		} finally {
			inverted_cursor_video.close();
		}
	}

	// For debugging -- end

	public DatabaseWrapper(EntityStore store) throws DatabaseException {
		wordByContent = store.getPrimaryIndex(String.class, WordEntity.class);
		invertedByIndex = store.getPrimaryIndex(Integer.class,
				InvertedIndexEntity.class);
		locationByPostCode = store.getPrimaryIndex(String.class,
				LocDateEntity.class);

		// -- image -- begin
		wordVideoByContent = store.getPrimaryIndex(String.class,
				WordEntityForVideo.class);
		invertedByIndexForVideo = store.getPrimaryIndex(Integer.class,
				InvertedIndexForVideoEntity.class);
		// -- image -- end

		// -- video -- begin
		wordImageByContent = store.getPrimaryIndex(String.class,
				WordEntityForImage.class);
		invertedByIndexForImage = store.getPrimaryIndex(Integer.class,
				InvertedIndexForImageEntity.class);
		// -- video -- end
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
		WordEntity entity = getWordEntity(content);
		if (entity != null) {
			return wordByContent.get(content).getinvertedIndexList();
		} else {
			return new ArrayList<Integer>();
		}
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
	public int addInvertedIndexEntity(String originalWord,
			ArrayList<Hit> hitList, double TF, String url, String title,
			String location) {
		InvertedIndexEntity entity = new InvertedIndexEntity(originalWord,
				hitList, TF, url, title, location);
		invertedByIndex.putNoReturn(entity);
		// System.out.println("getInvertedIndex: " + entity.getInvertedIndex());
		return entity.getInvertedIndex();
	}

	public boolean updateInvertedIndexEntity(String originalWord,
			int invertedIndex, ArrayList<Hit> hitList, double TF, String url,
			String title, String location) {
		InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
		if (entity != null) {
			entity.setOriginalWord(originalWord);
			entity.setHitList(hitList);
			entity.setTF(TF);
			entity.setUrl(url);
			entity.setTitle(title);
			entity.setLocation(location);
			invertedByIndex.putNoReturn(entity);
			return true;
		} else {
			return false;
		}
	}

	public String getOriginalWord(int invertedIndex) {
		InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
		if (entity != null) {
			return entity.getOriginalWord();
		}
		return null;
	}

	public HashMap<String, ArrayList<Integer>> getUrlToIndex(
			ArrayList<Integer> invertedIndexList) {
		HashMap<String, ArrayList<Integer>> result = new HashMap<String, ArrayList<Integer>>();
		for (Integer invertedIndex : invertedIndexList) {

			InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
			String url = entity.getUrl();
			if (result.containsKey(url)) {
				ArrayList<Integer> invertedList = result.get(url);
				invertedList.add(invertedIndex);
				result.put(url, invertedList);
			} else {
				ArrayList<Integer> invertedList = new ArrayList<Integer>();
				invertedList.add(invertedIndex);
				result.put(url, invertedList);
			}

		}
		return result;
	}

	public String getAllInfo(String original, String keyword) {
		ArrayList<SingleResultDocument> singleResultList = new ArrayList<SingleResultDocument>();
		ArrayList<SingleResultDocument> singleResultListNotExact = new ArrayList<SingleResultDocument>();

		ArrayList<Integer> invertedIndexList = getInvertedIndexList(keyword);

		if (invertedIndexList.size() == 0) {
			return "";
		}

		HashSet<String> urlSet = new HashSet<String>();
		
		int maxNum = 0;
		int maxNumNotExact = 0;
		
		for (Integer invertedIndex : invertedIndexList) {
			

			InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
			String title = entity.getTitle();
			
			if (title.contains("?")) {
				continue;
			}
			
			String url = entity.getUrl();
			
			String location = entity.getLocation();
			String originalWord = entity.getOriginalWord();
			double TF = entity.getTF();

			// for sum document number
			urlSet.add(url);

			ArrayList<Hit> hitList = entity.getHitList();

			SingleResultDocument singleResult = new SingleResultDocument(
					originalWord, url, title, TF, hitList, location);
			
			if(originalWord.equals(original)){
				maxNum++;
				singleResultList.add(singleResult);
				if (maxNum > 400) {
					break;
				}
			}else{
				maxNumNotExact++;
				if(maxNumNotExact <= 100){
					singleResultListNotExact.add(singleResult);
				}
			}
			
			// System.out.println(singleResultList);
		}
		
		singleResultList.addAll(singleResultListNotExact);
		double numerator = urlSet.size();

		WordResultDocument wordResult = new WordResultDocument(original,
				numerator, singleResultList);
		wordResult.filter();

		return wordResult.toString();
	}

	private InvertedIndexEntity getInvertedIndexEntity(int invertedIndex) {
		return invertedByIndex.get(invertedIndex);
	}

	// basic -- end

	// image -- begin
	/*
	 * WordEntityForImage OPERATIONS
	 */
	public boolean isExistWordEntityForImage(String content) {
		WordEntityForImage entity = getWordEntityForImage(content);
		if (entity != null) {
			return true;
		}
		return false;
	}

	public boolean addWordEntityForImage(String content) {
		WordEntityForImage entity = new WordEntityForImage(content);
		boolean isNew = wordImageByContent.putNoOverwrite(entity);
		return isNew;
	}

	public boolean addWordEntityForImage(String content,
			ArrayList<Integer> invertedIndexListForImage) {
		WordEntityForImage entity = new WordEntityForImage(content,
				invertedIndexListForImage);
		boolean isNew = wordImageByContent.putNoOverwrite(entity);
		return isNew;
	}

	public boolean updateWordEntityForImage(String content,
			ArrayList<Integer> invertedIndexListForImage) {
		WordEntityForImage entity = getWordEntityForImage(content);
		if (entity != null) {
			for (Integer invertedIndex : invertedIndexListForImage) {
				entity.addInvertedIndexForImage(invertedIndex);
			}
			wordImageByContent.putNoReturn(entity);
			return true;
		} else {
			return false;
		}
	}

	public ArrayList<Integer> getInvertedIndexListForImage(String content) {
		WordEntityForImage entity = getWordEntityForImage(content);
		if (entity != null) {
			return entity.getinvertedIndexForImageList();
		} else {
			return new ArrayList<Integer>();
		}
	}

	private WordEntityForImage getWordEntityForImage(String content) {
		return wordImageByContent.get(content);
	}

	/*
	 * InvertedIndexForImageEntity OPERATIONS
	 */
	public boolean isExistInvertedIndexForImageEntity(int invertedIndexForImage) {
		InvertedIndexForImageEntity entity = getInvertedIndexForImageEntity(invertedIndexForImage);
		if (entity != null) {
			return true;
		}
		return false;
	}

	// IMPORTANT: USE THIS METHOD WHEN YOU ARE SURE PRIMARY KEY DOES NOT ALREADY
	// EXIST
	public int addInvertedIndexForImageEntity(String originalWord,
			ArrayList<Hit> hitList, double TF, String url, String description,
			String pageUrl, String type) {
		InvertedIndexForImageEntity entity = new InvertedIndexForImageEntity(
				originalWord, hitList, TF, url, description, pageUrl, type);
		invertedByIndexForImage.putNoReturn(entity);
		return entity.getInvertedIndexForImage();
	}

	public boolean updateInvertedIndexForImageEntity(String originalWord,
			int invertedIndexForImage, ArrayList<Hit> hitList, double TF,
			String url, String decription, String pageUrl, String type) {
		InvertedIndexForImageEntity entity = getInvertedIndexForImageEntity(invertedIndexForImage);
		if (entity != null) {
			entity.setOriginalWord(originalWord);
			entity.setHitList(hitList);
			entity.setTF(TF);
			entity.setPageUrl(pageUrl);
			entity.setUrl(url);
			entity.setDescription(decription);
			entity.setType(type);
			invertedByIndexForImage.putNoReturn(entity);
			return true;
		} else {
			return false;
		}
	}

	public HashMap<String, ArrayList<Integer>> getImageUrlToIndex(
			ArrayList<Integer> invertedIndexListForImage) {
		HashMap<String, ArrayList<Integer>> result = new HashMap<String, ArrayList<Integer>>();
		for (Integer invertedIndex : invertedIndexListForImage) {

			InvertedIndexForImageEntity entity = getInvertedIndexForImageEntity(invertedIndex);
			String url = entity.getUrl();
			if (result.containsKey(url)) {
				ArrayList<Integer> invertedList = result.get(url);
				invertedList.add(invertedIndex);
				result.put(url, invertedList);
			} else {
				ArrayList<Integer> invertedList = new ArrayList<Integer>();
				invertedList.add(invertedIndex);
				result.put(url, invertedList);
			}

		}
		return result;
	}

	public String getAllInfoForImage(String original, String keyword) {
		ArrayList<SingleResultImage> singleResultList = new ArrayList<SingleResultImage>();

		ArrayList<Integer> invertedIndexList = getInvertedIndexListForImage(keyword);

		if (invertedIndexList.size() == 0) {
			return "";
		}

		HashSet<String> urlSet = new HashSet<String>();

		int maxNum = 0;
		for (Integer invertedIndex : invertedIndexList) {
			maxNum++;

			InvertedIndexForImageEntity entity = getInvertedIndexForImageEntity(invertedIndex);

			String description = entity.getDescription();
			if(description.contains("?")){
				continue;
			}
			
			double TF = entity.getTF();
			String url = entity.getUrl();
			String pageUrl = entity.getPageUrl();
			String type = entity.getType();
			String originalWord = entity.getOriginalWord();

			// for sum image number
			urlSet.add(url);

			ArrayList<Hit> hitList = entity.getHitList();
			SingleResultImage singleResult = new SingleResultImage(
					originalWord, url, description, pageUrl, type, TF, hitList);
			singleResultList.add(singleResult);

			if (maxNum > 500) {
				break;
			}
		}

		double numerator = urlSet.size();
		WordResultImage wordResult = new WordResultImage(original, numerator,
				singleResultList);
		wordResult.filter();

		return wordResult.toString();
	}

	public String getOriginalWordForImage(int invertedIndexForImage) {
		InvertedIndexForImageEntity entity = getInvertedIndexForImageEntity(invertedIndexForImage);
		if (entity != null) {
			return entity.getOriginalWord();
		}
		return null;
	}

	private InvertedIndexForImageEntity getInvertedIndexForImageEntity(
			int invertedIndexForImage) {
		return invertedByIndexForImage.get(invertedIndexForImage);
	}

	// image -- end

	// video -- begin
	/*
	 * WordEntityForVideo OPERATIONS
	 */
	public boolean isExistWordEntityForVideo(String content) {
		WordEntityForVideo entity = getWordEntityForVideo(content);
		if (entity != null) {
			return true;
		}
		return false;
	}

	public boolean addWordEntityForVideo(String content) {
		WordEntityForVideo entity = new WordEntityForVideo(content);
		boolean isNew = wordVideoByContent.putNoOverwrite(entity);
		return isNew;
	}

	public boolean addWordEntityForVideo(String content,
			ArrayList<Integer> invertedIndexListForVideo) {
		WordEntityForVideo entity = new WordEntityForVideo(content,
				invertedIndexListForVideo);
		boolean isNew = wordVideoByContent.putNoOverwrite(entity);
		return isNew;
	}

	public boolean updateWordEntityForVideo(String content,
			ArrayList<Integer> invertedIndexListForVideo) {
		WordEntityForVideo entity = getWordEntityForVideo(content);
		if (entity != null) {
			for (Integer invertedIndex : invertedIndexListForVideo) {
				entity.addInvertedIndexForVideo(invertedIndex);
			}
			wordVideoByContent.putNoReturn(entity);
			return true;
		} else {
			return false;
		}
	}

	public ArrayList<Integer> getInvertedIndexListForVideo(String content) {
		WordEntityForVideo entity = getWordEntityForVideo(content);
		if (entity != null) {
			return entity.getinvertedIndexForVideoList();
		} else {
			return new ArrayList<Integer>();
		}
	}

	private WordEntityForVideo getWordEntityForVideo(String content) {
		return wordVideoByContent.get(content);
	}

	/*
	 * InvertedIndexForVideoEntity OPERATIONS
	 */
	public boolean isExistInvertedIndexForVideoEntity(int invertedIndexForVideo) {
		InvertedIndexForVideoEntity entity = getInvertedIndexForVideoEntity(invertedIndexForVideo);
		if (entity != null) {
			return true;
		}
		return false;
	}

	// IMPORTANT: USE THIS METHOD WHEN YOU ARE SURE PRIMARY KEY DOES NOT ALREADY
	// EXIST
	public int addInvertedIndexForVideoEntity(String originalWord,
			ArrayList<Hit> hitList, double TF, String url, String title,
			String type) {
		InvertedIndexForVideoEntity entity = new InvertedIndexForVideoEntity(
				originalWord, hitList, TF, url, title, type);
		invertedByIndexForVideo.putNoReturn(entity);
		return entity.getInvertedIndexForVideo();
	}

	public boolean updateInvertedIndexForVideoEntity(String originalWord,
			int invertedIndexForVideo, ArrayList<Hit> hitList, double TF,
			String url, String title, String type) {
		InvertedIndexForVideoEntity entity = getInvertedIndexForVideoEntity(invertedIndexForVideo);
		if (entity != null) {
			entity.setOriginalWord(originalWord);
			entity.setHitList(hitList);
			entity.setTF(TF);
			entity.setUrl(url);
			entity.setTitle(title);
			entity.setType(type);
			invertedByIndexForVideo.putNoReturn(entity);
			return true;
		} else {
			return false;
		}
	}

	public HashMap<String, ArrayList<Integer>> getVideoUrlToIndex(
			ArrayList<Integer> invertedIndexListForVideo) {
		HashMap<String, ArrayList<Integer>> result = new HashMap<String, ArrayList<Integer>>();
		for (Integer invertedIndex : invertedIndexListForVideo) {

			InvertedIndexForVideoEntity entity = getInvertedIndexForVideoEntity(invertedIndex);
			String url = entity.getUrl();
			if (result.containsKey(url)) {
				ArrayList<Integer> invertedList = result.get(url);
				invertedList.add(invertedIndex);
				result.put(url, invertedList);
			} else {
				ArrayList<Integer> invertedList = new ArrayList<Integer>();
				invertedList.add(invertedIndex);
				result.put(url, invertedList);
			}

		}
		return result;
	}

	public String getAllInfoForVideo(String original, String keyword) {
		ArrayList<SingleResultVideo> singleResultList = new ArrayList<SingleResultVideo>();

		ArrayList<Integer> invertedIndexList = getInvertedIndexListForVideo(keyword);

		if (invertedIndexList.size() == 0) {
			return "";
		}

		HashSet<String> urlSet = new HashSet<String>();

		int maxNum = 0;
		for (Integer invertedIndex : invertedIndexList) {
			maxNum++;

			InvertedIndexForVideoEntity entity = getInvertedIndexForVideoEntity(invertedIndex);

			String title = entity.getTitle();
			if(title.contains("?")){
				continue;
			}
			
			double TF = entity.getTF();
			String url = entity.getUrl();
			String type = entity.getType();
			String originalWord = entity.getOriginalWord();

			// for sum video number
			urlSet.add(url);

			ArrayList<Hit> hitList = entity.getHitList();
			SingleResultVideo singleResult = new SingleResultVideo(
					originalWord, url, title, type, TF, hitList);
			singleResultList.add(singleResult);

			if (maxNum > 500) {
				break;
			}
		}

		double numerator = urlSet.size();
		WordResultVideo wordResult = new WordResultVideo(original, numerator,
				singleResultList);
		wordResult.filter();

		return wordResult.toString();
	}

	public String getOriginalWordForVideo(int invertedIndexForVideo) {
		InvertedIndexForVideoEntity entity = getInvertedIndexForVideoEntity(invertedIndexForVideo);
		if (entity != null) {
			return entity.getOriginalWord();
		}
		return null;
	}

	private InvertedIndexForVideoEntity getInvertedIndexForVideoEntity(
			int invertedIndexForVideo) {
		return invertedByIndexForVideo.get(invertedIndexForVideo);
	}
	// video -- end
}
