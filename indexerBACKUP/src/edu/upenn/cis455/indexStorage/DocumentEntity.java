package edu.upenn.cis455.indexStorage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class DocumentEntity {
	@PrimaryKey
	private int docId;

	private String url;
	private String title;
	
	private ArrayList<String> excerpt;
	
	private String location;
	
	private DocumentEntity() {
	}
	
	public DocumentEntity(int docID, String url, String title) {
		this.docId = docID;
		this.url = url;
		this.title = title;
		this.excerpt = new ArrayList<String>();
		this.location = "N";
	}

	
	public DocumentEntity(int docID, String url, String title, ArrayList<String> excerpt) {
		this.docId = docID;
		this.url = url;
		this.title = title;
		this.excerpt = new ArrayList<String>(excerpt);
		this.location = "N";
	}
	
	public DocumentEntity(int docID, String url, String title, ArrayList<String> excerpt, String location) {
		this.docId = docID;
		this.url = url;
		this.title = title;
		this.excerpt = new ArrayList<String>(excerpt);
		this.location = location;
	}


	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
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

	public ArrayList<String> getExcerpt() {
		return new ArrayList<String>(excerpt);
	}

	public void setExcerpt(ArrayList<String> excerpt) {
		this.excerpt = new ArrayList<String>(excerpt);
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
