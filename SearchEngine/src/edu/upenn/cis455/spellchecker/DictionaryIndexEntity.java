package edu.upenn.cis455.spellchecker;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class DictionaryIndexEntity {
	@PrimaryKey
	private String prefix;

	@SecondaryKey(relate = Relationship.ONE_TO_MANY, relatedEntity = DictionaryEntity.class)
	private ArrayList<Integer> wordsIndex;

	private DictionaryIndexEntity() {
	}

	public DictionaryIndexEntity(String prefix) {
		this.prefix = prefix;
		this.wordsIndex = new ArrayList<Integer>();
	}

	public DictionaryIndexEntity(String prefix, ArrayList<Integer> wordsIndex) {
		this.prefix = prefix;
		this.wordsIndex = new ArrayList<Integer>(wordsIndex);
	}

	public boolean addWordIndex(int wordIndex) {
		if (wordsIndex.contains(wordIndex)) {
			return false;
		} else {
			wordsIndex.add(wordIndex);
			return true;
		}
	}

	public ArrayList<Integer> getWordsIndex() {
		return new ArrayList<Integer>(wordsIndex);
	}
}
