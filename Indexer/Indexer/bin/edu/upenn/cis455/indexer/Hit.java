package edu.upenn.cis455.indexer;

import com.sleepycat.persist.model.Persistent;


@Persistent
public abstract class Hit {
	// 0 for PlainHit; 1 for FancyHit; 2 for AnchorHit
	protected int type;
	
	protected int fontSize;
	protected int position;
	
	public Hit(){
		
	}
	
	public Hit(int fontSize, int position) {
		this.fontSize = fontSize;
		this.position = position;
	}
	
	public int getType() {
		return type;
	}
	
	public int getPosition() {
		return position;
	}
	
	public int getFontSize() {
		return fontSize;
	}

	
	@Override
	public abstract String toString();
	
	public abstract void deserialize(String str);
}
