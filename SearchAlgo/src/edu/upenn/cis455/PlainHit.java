package edu.upenn.cis455;

public class PlainHit extends Hit{

	
	public String excerpt="";
	
	public PlainHit(){
		type=0;
	}
	
	public PlainHit(Long position, String excerpt){
		this.position=position;
		this.excerpt=excerpt;
	}
	
	public void setPosition(Long position){
		this.position=position;
	}
	
	public void setExcerpt(String excerpt){
		this.excerpt=excerpt;
	}

}
