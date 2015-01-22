package edu.upenn.cis455.searchalgo;

public class PlainHit extends Hit {

	public String excerpt = "";

	public PlainHit() {
		type = 0;
	}

	public PlainHit(Long position, String excerpt) {
		this.position = position;
		if (excerpt != null) {
			this.excerpt = excerpt;
		}
	}

	public void setPosition(Long position) {
		this.position = position;
	}

	public void setExcerpt(String excerpt) {
		if (excerpt != null) {
			this.excerpt = excerpt;
		}
	}

}
