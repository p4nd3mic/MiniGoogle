package edu.upenn.cis455.mapreduce.job;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;



@Entity
public class Ranks {
	@PrimaryKey
	private String url;
	private String rank;
	
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setRank(String rank) {
		this.rank = rank;
	}
	
	public String getRank() {
		return rank;
	}

}
