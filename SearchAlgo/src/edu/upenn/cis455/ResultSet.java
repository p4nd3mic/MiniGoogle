package edu.upenn.cis455;

public class ResultSet {

	public String title="";
	public String url="";
	public String summary="";
	public int docType=0;
	
	public ResultSet(){
		
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
	
}
