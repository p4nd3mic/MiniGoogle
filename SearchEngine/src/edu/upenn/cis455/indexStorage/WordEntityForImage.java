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
public class WordEntityForImage {
	@PrimaryKey
	private String content;

	@SecondaryKey(relate = Relationship.ONE_TO_MANY, relatedEntity = InvertedIndexForImageEntity.class)
	private ArrayList<Integer> invertedIndexForImageList;

	private WordEntityForImage() {

	}

	public WordEntityForImage(String content) {
		this.content = content;
		this.invertedIndexForImageList = new ArrayList<Integer>();
	}

	public WordEntityForImage(String content,
			ArrayList<Integer> invertedIndexForImageList) {
		this.content = content;
		this.invertedIndexForImageList = new ArrayList<Integer>(
				invertedIndexForImageList);
	}

	public String getContent() {
		return content;
	}

	public ArrayList<Integer> getinvertedIndexForImageList() {
		return new ArrayList<Integer>(invertedIndexForImageList);
	}

	public boolean addInvertedIndexForImage(int invertedIndexForImage) {
		if (invertedIndexForImageList.contains(invertedIndexForImage)) {
			return false;
		} else {
			invertedIndexForImageList.add(invertedIndexForImage);
			return true;
		}
	}

	public int getinvertedIndexListSize() {
		return invertedIndexForImageList.size();
	}
}
