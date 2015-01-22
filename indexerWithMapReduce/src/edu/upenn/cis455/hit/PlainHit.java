package edu.upenn.cis455.hit;

import com.sleepycat.persist.model.Persistent;


@Persistent
public class PlainHit extends Hit{
	private String excerpt;
	
	public PlainHit() {
		this.type = 0;
	}
	
	public PlainHit(int fontSize, int position){
		super(fontSize, position);
		this.type = 0;
		this.excerpt = "";
	}
	
	public PlainHit(int fontSize, int position, String excerpt){
		super(fontSize, position);
		this.type = 0;
		this.excerpt = excerpt;
	}
	
	@Override
	public String toString() {
		return this.type + "<#>" + this.position + "<#>" + this.fontSize + "<#>" + this.excerpt; 
	}

	@Override
	public void deserialize(String str) {
		String[] parts = str.split("<#>");
		
		this.position = Integer.valueOf(parts[1]);
		this.fontSize = Integer.valueOf(parts[2]);
		if(parts.length < 4){
			this.excerpt = "";
		}else{
			this.excerpt = parts[3];
		}
	}
	
	public String getExcerpt(){
		return this.excerpt;
	}
}
