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

	// word info
	private String originalWord;
	private ArrayList<Hit> hitList;
	private double TF;

	// video info
	private String url;
	private String title;
	private String type;

	private InvertedIndexForVideoEntity() {
	}

	public InvertedIndexForVideoEntity(String originalWord,
			ArrayList<Hit> hitList, double TF, String url, String title,
			String type) {
		this.originalWord = originalWord;
		this.hitList = new ArrayList<Hit>(hitList);
		this.TF = TF;
		this.url = url;
		this.title = title;
		this.type = type;
	}

	public int getInvertedIndexForVideo() {
		return invertedIndexForVideo;
	}

	public void setHitList(ArrayList<Hit> hitList) {
		this.hitList.clear();
		this.hitList = hitList;
	}

	public ArrayList<Hit> getHitList() {
		return new ArrayList<Hit>(hitList);
	}

	public int getHitListSize() {
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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
