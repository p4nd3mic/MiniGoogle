package edu.upenn.cis455;
import java.util.ArrayList;


public class DocEntry {

	public ArrayList<PlainHit> PlainHitList=null;
	public ArrayList<FancyHit> FancyHitList=null;
	public ArrayList<AnchorHit> AnchorHitList=null;
	public String title="";
	public String location="N";
	public double tf;
	public String url="";
	public String originalWord="";
	
	
	
	public DocEntry(){
		this.PlainHitList=new ArrayList<PlainHit>();
		this.FancyHitList=new ArrayList<FancyHit>();
		this.AnchorHitList=new ArrayList<AnchorHit>();
	}
	
	
	
	public void setTitle(String title){
		this.title=title;
	}
	
	public void setTf(double tf){
		this.tf=tf;
	}
	
	public void setUrl(String url){
		this.url=url;
	}
	
	public void setLocation(String location){
		this.location=location;
	}
	
	public void setOriginalWord(String originalWord){
		this.originalWord=originalWord;
	}
	
	public void addToPlainList(PlainHit phit){
		this.PlainHitList.add(phit);
	}
	
	public void addToFancyList(FancyHit fhit){
		this.FancyHitList.add(fhit);
	}
	
	public void addToAnchorList(AnchorHit ahit){
		this.AnchorHitList.add(ahit);
	}
}
