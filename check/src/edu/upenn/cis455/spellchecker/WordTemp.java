package edu.upenn.cis455.spellchecker;

public class WordTemp {
	private String content;
	private int hit;

	public WordTemp(String content, int hit) {
		this.content = content;
		this.hit = hit;
	}

	public String getContent() {
		return content;
	}

	public int getHit() {
		return hit;
	}

}
