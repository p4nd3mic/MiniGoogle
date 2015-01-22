package edu.upenn.cis455.indexStorage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import edu.upenn.cis455.hit.Hit;

@Entity
public class InvertedIndexForVideoEntity {
	@PrimaryKey(sequence = "ID")
	private int invertedIndexForVideo;

	@SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = VideoEntity.class)
	private int videoId;
	private String originalWord;
	private ArrayList<Hit> hitList;

	private double TF;

	private InvertedIndexForVideoEntity() {
	}

	public InvertedIndexForVideoEntity(int videoId, double TF) {
		this.videoId = videoId;
		this.TF = TF;
		this.hitList = new ArrayList<Hit>();
	}

	public InvertedIndexForVideoEntity(String originalWord, int videoId, ArrayList<Hit> hitList,
			double TF) {
		this.originalWord = originalWord;
		this.videoId = videoId;
		this.TF = TF;
		this.hitList = new ArrayList<Hit>(hitList);
	}

	public int getInvertedIndexForVideo() {
		return invertedIndexForVideo;
	}

	public int getVideoId() {
		return videoId;
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
