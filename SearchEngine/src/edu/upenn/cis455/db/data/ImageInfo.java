package edu.upenn.cis455.db.data;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ImageInfo {

	private String url;
	private String description;
	private String type;
	private String pageUrl;
	
	public ImageInfo(String url) {
		this.url = url;
	}
	public String getUrl() {
		return url;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPageUrl() {
		return pageUrl;
	}
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	
	public static class Binding extends TupleBinding<ImageInfo> {
		@Override
		public ImageInfo entryToObject(TupleInput ti) {
			String url = ti.readString();
			ImageInfo object = new ImageInfo(url);
			object.setDescription(ti.readString());
			object.setType(ti.readString());
			object.setPageUrl(ti.readString());
			return object;
		}

		@Override
		public void objectToEntry(ImageInfo object, TupleOutput to) {
			to.writeString(object.getUrl());
			to.writeString(object.getDescription());
			to.writeString(object.getType());
			to.writeString(object.getPageUrl());
		}
	}
}
