package edu.upenn.cis455.indexStorage;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class ImageEntity {
	@PrimaryKey
	private int imageId;

	private String url;
	private String decription;
	private String pageUrl;
	private String type;

	private ImageEntity() {
	}

	public ImageEntity(int imageId, String url, String decription,
			String pageUrl, String type) {
		super();
		this.imageId = imageId;
		this.url = url;
		this.decription = decription;
		this.pageUrl = pageUrl;
		this.type = type;
	}

	public int getImageId() {
		return imageId;
	}

	public String getUrl() {
		return url;
	}

	public String getDecription() {
		return decription;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public String getType() {
		return type;
	}

}