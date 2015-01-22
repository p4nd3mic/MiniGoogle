package edu.upenn.cis455.searchalgo;

public class AnchorHit extends Hit{

	
	public AnchorHit(){
		this.type=2;
	}
	
	public AnchorHit(Long position){
		this.position=position;
	}

	public void setPosition(Long position){
		this.position=position;
	}
}
