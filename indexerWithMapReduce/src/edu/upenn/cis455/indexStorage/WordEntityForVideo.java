package edu.upenn.cis455.indexStorage;

import java.util.ArrayList;
import java.util.HashMap;

import com.sleepycat.je.Sequence;
import com.sleepycat.je.SequenceConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class WordEntityForVideo {
	@PrimaryKey
	private String content;

	@SecondaryKey(relate = Relationship.ONE_TO_MANY, relatedEntity = InvertedIndexForVideoEntity.class)
	private ArrayList<Integer> invertedIndexForVideoList;

	private WordEntityForVideo() {

	}

	public WordEntityForVideo(String content) {
		this.content = content;
		this.invertedIndexForVideoList = new ArrayList<Integer>();
	}

	public WordEntityForVideo(String content,
			ArrayList<Integer> invertedIndexForVideoList) {
		this.content = content;
		this.invertedIndexForVideoList = new ArrayList<Integer>(
				invertedIndexForVideoList);
	}

	public String getContent() {
		return content;
	}

	public ArrayList<Integer> getinvertedIndexForVideoList() {
		return new ArrayList<Integer>(invertedIndexForVideoList);
	}

	public boolean addInvertedIndexForVideo(int invertedIndexForVideo) {
		if (invertedIndexForVideoList.contains(invertedIndexForVideo)) {
			return false;
		} else {
			invertedIndexForVideoList.add(invertedIndexForVideo);
			return true;
		}
	}

	public int getinvertedIndexListSize() {
		return invertedIndexForVideoList.size();
	}
}
