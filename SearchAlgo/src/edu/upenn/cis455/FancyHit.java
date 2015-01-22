package edu.upenn.cis455;
public class FancyHit extends Hit{

	
	public Long subtype;
	
	public FancyHit(){
		type=1;
	}
	
	public FancyHit(Long subtype2, Long position){
		this.subtype=subtype2;
		this.position=position;
	}
	
	public void setSubtype(Long subtype){
		this.subtype=subtype;
	}
	
	public void setType(int type){
		this.type=type;
	}

}
