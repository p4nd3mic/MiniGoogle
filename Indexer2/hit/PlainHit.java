package edu.upenn.cis455.hit;

import com.sleepycat.persist.model.Persistent;


@Persistent
public class PlainHit extends Hit{
	public PlainHit() {
		this.type = 0;
	}
	
	public PlainHit(int fontSize, int position){
		super(fontSize, position);
		this.type = 0;
	}
	
	@Override
	public String toString() {
		return this.type + "#" + this.position + "#" + this.fontSize; 
	}

	@Override
	public void deserialize(String str) {
		String[] parts = str.split("#");
		
		this.position = Integer.valueOf(parts[1]);
		this.fontSize = Integer.valueOf(parts[2]);
	}
}
