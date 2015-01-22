package edu.upenn.cis455.spellchecker;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;


@Entity
public class DictionaryEntity {
	@PrimaryKey(sequence = "ID")
	private int index;
	
	@SecondaryKey(relate = Relationship.ONE_TO_ONE)
	private String content;
	private int hit;
	
	private DictionaryEntity() {
	}
	
	public DictionaryEntity(String content){
		this.content = content;
		this.hit = 1;
	}

	public String getContent() {
		return content;
	}
	
	public int getIndex(){
		return index;
	}
	
	public int getHit(){
		return hit;
	}
	
	public void increaseHit(){
		hit = hit + 1;
	}
	
}
