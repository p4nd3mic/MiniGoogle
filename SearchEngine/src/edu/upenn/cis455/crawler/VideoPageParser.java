package edu.upenn.cis455.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.upenn.cis455.db.data.VideoInfo;

public class VideoPageParser {
	
	private static List<VideoSiteParser> mParsers = new ArrayList<VideoSiteParser>();
	
	static {
		initParserMap();
	}
	
	public static VideoInfo parse(String url, Document jsoupDoc) {
		VideoInfo info = null;
		for(VideoSiteParser parser : mParsers) {
			info = parser.parseInfo(url, jsoupDoc);
			if(info != null) {
				break;
			}
		}
		return info;
	}
	
	public static String getThumbnailUrl(String url) {
		String tnUrl = null;
		for(VideoSiteParser parser : mParsers) {
			tnUrl = parser.getThumbnailUrl(url);
			if(tnUrl != null) {
				break;
			}
		}
		return tnUrl;
	}
	
	private static void initParserMap() {
		mParsers.add(new YoutubeParser());
	}

	private interface VideoSiteParser {
		VideoInfo parseInfo(String url, Document jsoupDoc);
		String getThumbnailUrl(String videoUrl);
	}
	
	private static class YoutubeParser implements VideoSiteParser {

		private static final String SITE_NAME = "youtube";
		private Pattern pattern = Pattern.compile("www.youtube.com\\/watch.*[\\?&]v=([^&#]*)");
		private static final String THUMBNAIL_URL = "http://img.youtube.com/vi/%s/0.jpg";
		
		@Override
		public VideoInfo parseInfo(String url, Document jsoupDoc) {
			VideoInfo videoInfo = null;
			String id = null;
			String title = null;
			String description = null;
			
			Matcher m = pattern.matcher(url);
			if(m.find()) {
				if(m.groupCount() >= 1) {
					id = m.group(1);
				}
			}
			
			if(id != null) {
				videoInfo = new VideoInfo(url);
				videoInfo.setVideoSite(SITE_NAME);
				videoInfo.setVideoId(id);
				// Parse video title
				Element titleElem = null;
				Elements titles = jsoupDoc.select("#eow-title");
				if(titles.size() > 0) {
					titleElem = titles.get(0);
				}
				if(titleElem != null) {
					title = titleElem.attr("title");
				}
				if(title != null) {
					videoInfo.setTitle(title);
				}
				// Parse video description
				Element descElem = null;
				Elements descs = jsoupDoc.select("#eow-description");
				if(descs.size() > 0) {
					descElem = descs.get(0);
				}
				if(descElem != null) {
					description = descElem.text();
				}
				if(description != null) {
					videoInfo.setDescription(description);
				}
			}
			return videoInfo;
		}

		@Override
		public String getThumbnailUrl(String videoUrl) {
			String tnUrl = null;
			String id = null;
			Matcher m = pattern.matcher(videoUrl);
			if(m.find()) {
				if(m.groupCount() >= 1) {
					id = m.group(1);
				}
			}
			if(id != null) {
				tnUrl = String.format(THUMBNAIL_URL, id);
			}
			return tnUrl;
		}
	}
}
