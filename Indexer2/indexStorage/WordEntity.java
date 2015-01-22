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
public class WordEntity {
	@PrimaryKey
	private String content;

	@SecondaryKey(relate = Relationship.ONE_TO_MANY, relatedEntity = InvertedIndexEntity.class)
	private ArrayList<Integer> invertedIndexList;

	private WordEntity() {

	}

	public WordEntity(String content) {
		this.content = content;
		this.invertedIndexList = new ArrayList<Integer>();
	}

	public WordEntity(String content, ArrayList<Integer> invertedIndexList) {
		this.content = content;
		this.invertedIndexList = new ArrayList<Integer>(invertedIndexList);
	}

	public String getContent() {
		return content;
	}

	public ArrayList<Integer> getinvertedIndexList() {
		return new ArrayList<Integer>(invertedIndexList);
	}

	public boolean addDocId(int invertedIndex) {
		if (invertedIndexList.contains(invertedIndex)) {
			return false;
		} else {
			invertedIndexList.add(invertedIndex);
			return true;
		}
	}

	public int getinvertedIndexListSize() {
		return invertedIndexList.size();
	}
}
