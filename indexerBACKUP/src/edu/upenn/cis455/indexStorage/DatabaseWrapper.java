package edu.upenn.cis455.indexStorage;

import java.util.ArrayList;
import java.util.HashMap;

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
	private PrimaryIndex<Integer, DocumentEntity> documentById;
	private PrimaryIndex<String, WordEntity> wordByContent;
	private PrimaryIndex<Integer, InvertedIndexEntity> invertedByIndex;
	private PrimaryIndex<String, LocDateEntity> locationByPostCode;
	// -- basic -- end

	// -- image -- begin
	private PrimaryIndex<Integer, ImageEntity> imageById;
	private PrimaryIndex<String, WordEntityForImage> wordImageByContent;
	private PrimaryIndex<Integer, InvertedIndexForImageEntity> invertedByIndexForImage;
	// -- image -- end

	// -- video -- begin
	private PrimaryIndex<Integer, VideoEntity> videoById;
	private PrimaryIndex<String, WordEntityForVideo> wordVideoByContent;
	private PrimaryIndex<Integer, InvertedIndexForVideoEntity> invertedByIndexForVideo;

	// -- video -- end

	public DatabaseWrapper(EntityStore store) throws DatabaseException {
		documentById = store.getPrimaryIndex(Integer.class,
				DocumentEntity.class);
		wordByContent = store.getPrimaryIndex(String.class, WordEntity.class);
		invertedByIndex = store.getPrimaryIndex(Integer.class,
				InvertedIndexEntity.class);
		locationByPostCode = store.getPrimaryIndex(String.class,
				LocDateEntity.class);

		// -- image -- begin
		imageById = store.getPrimaryIndex(Integer.class, ImageEntity.class);
		wordVideoByContent = store.getPrimaryIndex(String.class,
				WordEntityForVideo.class);
		invertedByIndexForVideo = store.getPrimaryIndex(Integer.class,
				InvertedIndexForVideoEntity.class);
		// -- image -- end

		// -- video -- begin
		videoById = store.getPrimaryIndex(Integer.class, VideoEntity.class);
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
		DocumentEntity entity = new DocumentEntity(docId, url, title, excerpt,
				location);
		documentById.putNoReturn(entity);
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
	public int addInvertedIndexEntity(String originalWord, int docId,
			ArrayList<Hit> hitList, double TF) {
		InvertedIndexEntity entity = new InvertedIndexEntity(originalWord,
				docId, hitList, TF);
		invertedByIndex.putNoReturn(entity);
		// System.out.println("getInvertedIndex: " + entity.getInvertedIndex());
		return entity.getInvertedIndex();
	}

	public boolean updateInvertedIndexEntity(String originalWord,
			int invertedIndex, ArrayList<Hit> hitList, double TF) {
		InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
		if (entity != null) {
			entity.setOriginalWord(originalWord);
			entity.setHitList(hitList);
			entity.setTF(TF);
			invertedByIndex.putNoReturn(entity);
			return true;
		} else {
			return false;
		}
	}
	
	public String getOriginalWord(int invertedIndex){
		InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
		if (entity != null) {
			return entity.getOriginalWord();
		}
		return null;
	}

	public HashMap<Integer, ArrayList<Integer>> getDocIdToIndex(
			ArrayList<Integer> invertedIndexList) {
		HashMap<Integer, ArrayList<Integer>> result = new HashMap<Integer, ArrayList<Integer>>();
		for (Integer invertedIndex : invertedIndexList) {

			InvertedIndexEntity entity = getInvertedIndexEntity(invertedIndex);
			int docId = entity.getDocId();
			if (result.containsKey(docId)) {
				ArrayList<Integer> invertedList = result.get(docId);
				invertedList.add(invertedIndex);
				result.put(docId, invertedList);
			} else {
				ArrayList<Integer> invertedList = new ArrayList<Integer>();
				invertedList.add(invertedIndex);
				result.put(entity.getDocId(), invertedList);
			}

		}
		return result;
	}

	public String getAllInfo(String original, String keyword) {
		ArrayList<SingleResultDocument> singleResultList = new ArrayList<SingleResultDocument>();

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
			String location = (String) docResult.get(3);
			String originalWord = entity.getOriginalWord();

			SingleResultDocument singleResult = new SingleResultDocument(
					originalWord, url, title, excerpt, TF, hitList, location);
			singleResultList.add(singleResult);
			// System.out.println(singleResultList);
		}

		// TODO: IDF
		long totalDocNum = getTotalDocument();
		// System.out.println("totalDocNum: " + totalDocNum);
		long totalOccurNum = invertedIndexList.size();
		// System.out.println("totalOccurNum: " + totalOccurNum);
		double IDF = Math.log(totalDocNum / totalOccurNum);

		WordResultDocument wordResult = new WordResultDocument(original, IDF,
				singleResultList);
		wordResult.filter();

		return wordResult.toString();
	}

	private InvertedIndexEntity getInvertedIndexEntity(int invertedIndex) {
		return invertedByIndex.get(invertedIndex);
	}

	// basic -- end

	// image -- begin
	/*
	 * ImageEntity OPERATIONS
	 */
	public boolean isExistImageEntity(int imageId) {
		ImageEntity entity = getImageEntity(imageId);
		if (entity != null) {
			return true;
		}
		return false;
	}

	// TODO: no matter exists or not, add the image -- need to re-check
	public void addImageEntity(int imageId, String url, String decription,
			String pageUrl, String type) {
		ImageEntity entity = new ImageEntity(imageId, url, decription, pageUrl,
				type);
		imageById.putNoReturn(entity);
	}

	public ArrayList<String> getImageInfo(int imageId) {
		ArrayList<String> result = new ArrayList<String>();
		ImageEntity entity = getImageEntity(imageId);
		if (entity != null) {
			String url = entity.getUrl();
			String decription = entity.getDecription();
			String pageUrl = entity.getPageUrl();
			String type = entity.getType();
			result.add(url);
			result.add(decription);
			result.add(pageUrl);
			result.add(type);
		}
		return result;
	}

	public long getTotalImage() {
		return imageById.count();
	}

	private ImageEntity getImageEntity(int imageId) {
		return imageById.get(imageId);
	}

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
		return wordImageByContent.get(content).getinvertedIndexForImageList();
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
	public int addInvertedIndexForImageEntity(int imageId, double TF) {
		InvertedIndexForImageEntity entity = new InvertedIndexForImageEntity(
				imageId, TF);
		invertedByIndexForImage.putNoReturn(entity);
		return entity.getInvertedIndexForImage();
	}

	// IMPORTANT: USE THIS METHOD WHEN YOU ARE SURE PRIMARY KEY DOES NOT ALREADY
	// EXIST
	public int addInvertedIndexForImageEntity(String originalWord, int imageId,
			ArrayList<Hit> hitList, double TF) {
		InvertedIndexForImageEntity entity = new InvertedIndexForImageEntity(
				originalWord, imageId, hitList, TF);
		invertedByIndexForImage.putNoReturn(entity);
		return entity.getInvertedIndexForImage();
	}

	public boolean updateInvertedIndexForImageEntity(String originalWord,
			int invertedIndexForImage, ArrayList<Hit> hitList, double TF) {
		InvertedIndexForImageEntity entity = getInvertedIndexForImageEntity(invertedIndexForImage);
		if (entity != null) {
			entity.setOriginalWord(originalWord);
			entity.setHitList(hitList);
			entity.setTF(TF);
			invertedByIndexForImage.putNoReturn(entity);
			return true;
		} else {
			return false;
		}
	}

	public HashMap<Integer, Integer> getImageIdToIndex(
			ArrayList<Integer> invertedIndexListForImage) {
		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (Integer invertedIndex : invertedIndexListForImage) {
			InvertedIndexForImageEntity entity = getInvertedIndexForImageEntity(invertedIndex);
			result.put(entity.getImageId(), invertedIndex);
		}
		return result;
	}

	public String getAllInfoForImage(String original, String keyword) {
		ArrayList<SingleResultImage> singleResultList = new ArrayList<SingleResultImage>();

		ArrayList<Integer> invertedIndexList = getInvertedIndexListForImage(keyword);

		for (Integer invertedIndex : invertedIndexList) {
			InvertedIndexForImageEntity entity = getInvertedIndexForImageEntity(invertedIndex);

			int imageId = entity.getImageId();
			ArrayList<Hit> hitList = entity.gethitList();
			double TF = entity.getTF();

			ArrayList<String> docResult = getImageInfo(imageId);

			String url = docResult.get(0);
			String description = docResult.get(1);
			String pageUrl = docResult.get(2);
			String type = docResult.get(3);
			String originalWord = entity.getOriginalWord();

			SingleResultImage singleResult = new SingleResultImage(
					originalWord, url, description, pageUrl, type, TF, hitList);
			singleResultList.add(singleResult);
		}

		// TODO : Get IDF with cooperation with other machines
		long totalImageNum = getTotalImage();
		// System.out.println("totalImageNum: " + totalImageNum);
		long totalOccurNum = invertedIndexList.size();
		// System.out.println("totalOccurNum: " + totalOccurNum);
		double IDF = Math.log(totalImageNum / totalOccurNum);

		WordResultImage wordResult = new WordResultImage(original, IDF,
				singleResultList);
		wordResult.filter();

		return wordResult.toString();
	}

	private InvertedIndexForImageEntity getInvertedIndexForImageEntity(
			int invertedIndexForImage) {
		return invertedByIndexForImage.get(invertedIndexForImage);
	}

	// image -- end

	// video -- begin
	/*
	 * VideoEntity OPERATIONS
	 */
	public boolean isExistVideoEntity(int videoId) {
		VideoEntity entity = getVideoEntity(videoId);
		if (entity != null) {
			return true;
		}
		return false;
	}

	// TODO: no matter exists or not, add the video -- need to re-check
	public void addVideoEntity(int videoId, String url, String decription,
			String type) {
		VideoEntity entity = new VideoEntity(videoId, url, decription, type);
		videoById.putNoReturn(entity);
	}

	public ArrayList<String> getVideoInfo(int videoId) {
		ArrayList<String> result = new ArrayList<String>();
		VideoEntity entity = getVideoEntity(videoId);
		if (entity != null) {
			String url = entity.getUrl();
			String decription = entity.getDecription();
			String type = entity.getType();
			result.add(url);
			result.add(decription);
			result.add(type);
		}
		return result;
	}

	public long getTotalVideo() {
		return videoById.count();
	}

	private VideoEntity getVideoEntity(int videoId) {
		return videoById.get(videoId);
	}

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
		return wordVideoByContent.get(content).getinvertedIndexForVideoList();
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
	public int addInvertedIndexForVideoEntity(int videoId, double TF) {
		InvertedIndexForVideoEntity entity = new InvertedIndexForVideoEntity(
				videoId, TF);
		invertedByIndexForVideo.putNoReturn(entity);
		return entity.getInvertedIndexForVideo();
	}

	// IMPORTANT: USE THIS METHOD WHEN YOU ARE SURE PRIMARY KEY DOES NOT ALREADY
	// EXIST
	public int addInvertedIndexForVideoEntity(String originalWord, int videoId,
			ArrayList<Hit> hitList, double TF) {
		InvertedIndexForVideoEntity entity = new InvertedIndexForVideoEntity(
				originalWord, videoId, hitList, TF);
		invertedByIndexForVideo.putNoReturn(entity);
		return entity.getInvertedIndexForVideo();
	}

	public boolean updateInvertedIndexForVideoEntity(String originalWord,
			int invertedIndexForVideo, ArrayList<Hit> hitList, double TF) {
		InvertedIndexForVideoEntity entity = getInvertedIndexForVideoEntity(invertedIndexForVideo);
		if (entity != null) {
			entity.setOriginalWord(originalWord);
			entity.setHitList(hitList);
			entity.setTF(TF);
			invertedByIndexForVideo.putNoReturn(entity);
			return true;
		} else {
			return false;
		}
	}

	public HashMap<Integer, Integer> getVideoIdToIndex(
			ArrayList<Integer> invertedIndexListForVideo) {
		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (Integer invertedIndex : invertedIndexListForVideo) {
			InvertedIndexForVideoEntity entity = getInvertedIndexForVideoEntity(invertedIndex);
			result.put(entity.getVideoId(), invertedIndex);
		}
		return result;
	}

	public String getAllInfoForVideo(String original, String keyword) {
		ArrayList<SingleResultVideo> singleResultList = new ArrayList<SingleResultVideo>();

		ArrayList<Integer> invertedIndexList = getInvertedIndexListForVideo(keyword);

		for (Integer invertedIndex : invertedIndexList) {
			InvertedIndexForVideoEntity entity = getInvertedIndexForVideoEntity(invertedIndex);

			int videoId = entity.getVideoId();
			ArrayList<Hit> hitList = entity.gethitList();
			double TF = entity.getTF();

			ArrayList<String> docResult = getVideoInfo(videoId);

			String url = docResult.get(0);
			String description = docResult.get(1);
			String type = docResult.get(2);
			String originalWord = entity.getOriginalWord();
			SingleResultVideo singleResult = new SingleResultVideo(
					originalWord, url, description, type, TF, hitList);
			singleResultList.add(singleResult);
		}

		// TODO : Get IDF with cooperation with other machines
		long totalVideoNum = getTotalVideo();
		// System.out.println("totalVideoNum: " + totalVideoNum);
		long totalOccurNum = invertedIndexList.size();
		// System.out.println("totalOccurNum: " + totalOccurNum);
		double IDF = Math.log(totalVideoNum / totalOccurNum);

		WordResultVideo wordResult = new WordResultVideo(original, IDF,
				singleResultList);
		wordResult.filter();

		return wordResult.toString();
	}

	private InvertedIndexForVideoEntity getInvertedIndexForVideoEntity(
			int invertedIndexForVideo) {
		return invertedByIndexForVideo.get(invertedIndexForVideo);
	}
	// video -- end
}
