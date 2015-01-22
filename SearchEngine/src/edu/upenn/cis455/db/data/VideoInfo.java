package edu.upenn.cis455.db.data;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class VideoInfo {

	private String url;
	private String title;
	private String description;
	private String videoSite;
	private String videoId;
	
	public VideoInfo(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUrl() {
		return url;
	}
	public String getVideoSite() {
		return videoSite;
	}
	public void setVideoSite(String videoSite) {
		this.videoSite = videoSite;
	}
	public String getVideoId() {
		return videoId;
	}
	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public static class Binding extends TupleBinding<VideoInfo> {

		@Override
		public VideoInfo entryToObject(TupleInput ti) {
			String url = ti.readString();
			VideoInfo object = new VideoInfo(url);
			object.setTitle(ti.readString());
			object.setDescription(ti.readString());
			object.setVideoSite(ti.readString());
			object.setVideoId(ti.readString());
			return object;
		}

		@Override
		public void objectToEntry(VideoInfo object, TupleOutput to) {
			to.writeString(object.getUrl());
			to.writeString(object.getTitle());
			to.writeString(object.getDescription());
			to.writeString(object.getVideoSite());
			to.writeString(object.getVideoId());
		}
	}
}
