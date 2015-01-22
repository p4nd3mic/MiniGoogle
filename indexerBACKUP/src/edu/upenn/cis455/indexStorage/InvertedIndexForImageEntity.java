package edu.upenn.cis455.indexStorage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import edu.upenn.cis455.hit.Hit;

@Entity
public class InvertedIndexForImageEntity {
	@PrimaryKey(sequence = "ID")
	private int invertedIndexForImage;

	@SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = ImageEntity.class)
	private int imageId;
	private String originalWord;
	private ArrayList<Hit> hitList;

	private double TF;
	
	
	private InvertedIndexForImageEntity() {
	}

	public InvertedIndexForImageEntity(int imageId, double TF) {
		this.imageId = imageId;
		this.TF = TF;
		this.hitList = new ArrayList<Hit>();
	}

	public InvertedIndexForImageEntity(String originalWord, int imageId, ArrayList<Hit> hitList, double TF) {
		this.originalWord = originalWord;
		this.imageId = imageId;
		this.TF = TF;
		this.hitList = new ArrayList<Hit>(hitList);
	}

	public int getInvertedIndexForImage() {
		return invertedIndexForImage;
	}

	public int getImageId() {
		return imageId;
	}

	public void setHitList(ArrayList<Hit> hitList) {
		this.hitList.clear();
		this.hitList = hitList;
	}

	public ArrayList<Hit> gethitList() {
		return new ArrayList<Hit>(hitList);
	}

	public int gethitListSize() {
		return hitList.size();
	}

	public double getTF() {
		return TF;
	}

	public void setTF(double tF) {
		TF = tF;
	}
	public String getOriginalWord() {
		return originalWord;
	}

	public void setOriginalWord(String originalWord) {
		this.originalWord = originalWord;
	}

}
