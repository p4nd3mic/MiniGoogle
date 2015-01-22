package edu.upenn.cis455.indexer;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


@Entity
public class LocDateEntity {
	@PrimaryKey
	private String postCode;
	private String cityName;
	private String stateName;
	private String stateAbbr;
	private String latitude;
	private String longitude;
	
	private LocDateEntity() {
	}
	
	public LocDateEntity(String postCode, String cityName, String stateName,
			String stateAbbr, String latitude, String longitude) {
		this.postCode = postCode;
		this.cityName = cityName;
		this.stateName = stateName;
		this.stateAbbr = stateAbbr;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getPostCode() {
		return postCode;
	}

	public String getCityName() {
		return cityName;
	}

	public String getStateName() {
		return stateName;
	}

	public String getStateAbbr() {
		return stateAbbr;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}
	
}
