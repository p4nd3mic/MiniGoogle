package edu.upenn.cis455.indexStorage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis455.hit.Hit;

@Entity
public class InvertedIndexForImageEntity {
	@PrimaryKey(sequence = "ID")
	private int invertedIndexForImage;

	// word info
	private String originalWord;
	private ArrayList<Hit> hitList;
	private double TF;

	// image info
	private String url;
	private String description;
	private String pageUrl;
	private String type;

	private InvertedIndexForImageEntity() {
	}

	public InvertedIndexForImageEntity(String originalWord,
			ArrayList<Hit> hitList, double TF, String url, String description,
			String pageUrl, String type) {
		this.originalWord = originalWord;
		this.hitList = new ArrayList<Hit>(hitList);
		this.TF = TF;
		this.url = url;
		this.description = description;
		this.pageUrl = pageUrl;
		this.type = type;
	}

	public int getInvertedIndexForImage() {
		return invertedIndexForImage;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String decription) {
		this.description = decription;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
