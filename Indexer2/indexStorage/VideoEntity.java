package edu.upenn.cis455.indexStorage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class VideoEntity {
	@PrimaryKey
	private int videoId;

	private String url;
	private String decription;
	private String type;

	private VideoEntity() {
	}

	public VideoEntity(int videoId, String url, String decription, String type) {
		super();
		this.videoId = videoId;
		this.url = url;
		this.decription = decription;
		this.type = type;
	}

	public int getvideoId() {
		return videoId;
	}

	public String getUrl() {
		return url;
	}

	public String getDecription() {
		return decription;
	}

	public String getType() {
		return type;
	}

}