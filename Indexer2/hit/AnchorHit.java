package edu.upenn.cis455.hit;

import com.sleepycat.persist.model.Persistent;


@Persistent
public class AnchorHit extends Hit {
	public AnchorHit() {
		this.type = 2;
	}

	public AnchorHit(int fontSize, int position) {
		super(fontSize, position);
		this.type = 2;
	}
	
	@Override
	public String toString() {
		return this.type  + "#" + this.position + "#" + this.fontSize;
	}

	@Override
	public void deserialize(String str) {
		String[] parts = str.split("#");
		
		this.position = Integer.valueOf(parts[1]);
		this.fontSize = Integer.valueOf(parts[2]);
	}
}
