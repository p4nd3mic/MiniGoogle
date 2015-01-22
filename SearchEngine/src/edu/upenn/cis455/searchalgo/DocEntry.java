package edu.upenn.cis455.searchalgo;

import java.util.ArrayList;

public class DocEntry {

	public ArrayList<PlainHit> PlainHitList = null;
	public ArrayList<FancyHit> FancyHitList = null;
	public ArrayList<AnchorHit> AnchorHitList = null;
	public String title = "";
	public String location = "N";
	public double tf;
	public String url = "";
	public String originalWord = "";
	public String pageUrl = "";
	public String filetype = "";

	public DocEntry() {
		this.PlainHitList = new ArrayList<PlainHit>();
		this.FancyHitList = new ArrayList<FancyHit>();
		this.AnchorHitList = new ArrayList<AnchorHit>();
	}

	public void setFileType(String filetype) {
		this.filetype = filetype;
	}

	public void setTitle(String title) {
		if (title != null) {
			this.title = title;
		}
	}

	public void setTf(double tf) {
		this.tf = tf;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setOriginalWord(String originalWord) {
		this.originalWord = originalWord;
	}

	public void addToPlainList(PlainHit phit) {
		this.PlainHitList.add(phit);
	}

	public void addToFancyList(FancyHit fhit) {
		this.FancyHitList.add(fhit);
	}

	public void addToAnchorList(AnchorHit ahit) {
		this.AnchorHitList.add(ahit);
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
}
