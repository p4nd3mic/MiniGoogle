package edu.upenn.cis455.searchalgo;

public class ResultSet {

	public String title="";
	public String url="";
	public String summary="";
	public int docType=0;
	public String pageUrl="";
	public String location="";
	public double score=0;
	public double prankscore=0;
	public double tfidf=0;
	
	public ResultSet(){
		
	}
	
	public void setLocation(String location){
		this.location=location;
	}
	
	public void setTitle(String title){
		this.title=title;
	}
	
	public void setUrl(String url){
		this.url=url;
	}
	
	public void setSummary(String summary){
		this.summary=summary;
	}
	
	public void setDocType(int docType){
		this.docType=docType;
	}
	
	public void setPageUrl(String pageUrl){
		this.pageUrl=pageUrl;
	}
	
	public void setScore(double score){
		this.score=score;
	}
	
	public void setprankScore(double score){
		this.prankscore=score;
	}
	
	public void settfidf(double score){
		this.tfidf=score;
	}
	
}
