package edu.upenn.cis455.indexStorage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

import edu.upenn.cis455.hit.Hit;

@Entity
public class InvertedIndexEntity {
	@PrimaryKey(sequence = "ID")
	private int invertedIndex;

	// word info
	private String originalWord;
	private ArrayList<Hit> hitList;
	private double TF;

	// web page info
	private String url;
	private String title;
	private String location;

	private InvertedIndexEntity() {
	}

	public InvertedIndexEntity(String originalWord, ArrayList<Hit> hitList,
			double TF, String url, String title, String location) {
		this.originalWord = originalWord;
		this.hitList = new ArrayList<Hit>(hitList);
		this.TF = TF;
		this.url = url;
		this.title = title;
		this.location = location;
	}

	public int getInvertedIndex() {
		return invertedIndex;
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
