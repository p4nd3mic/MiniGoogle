package edu.upenn.cis455.hit;

import com.sleepycat.persist.model.Persistent;


@Persistent
public class FancyHit extends Hit {
	// 0 for url; 1 for title; 2 for meta tag; 3 for <h1> ~ <h6> tag
	private int subType;
	
	public FancyHit() {
		this.type = 1;
	}

	public FancyHit(int fontSize, int position, int subType) {
		super(fontSize, position);

		this.type = 1;
		this.subType = subType;
	}

	public int getSubType() {
		return subType;
	}

	public String toString() {
		return this.type + "#" + this.position + "#" + this.fontSize + "#"
				+ this.subType;
	}
	
	
	@Override
	public void deserialize(String str) {
		String[] parts = str.split("#");
		
		this.position = Integer.valueOf(parts[1]);
		this.fontSize = Integer.valueOf(parts[2]);
		this.subType = Integer.valueOf(parts[3]);
	}
}
