package com.qubling.sidekick.metacpan.result;

import org.json.JSONException;
import org.json.JSONObject;

public class Module {
	
	private String name;
	private String authorPauseId;
	
	private String distributionName;
	private String distributionVersion;
	private int distributionRatingCount;
	private double distributionRating;
	
	public static Module fromModuleSearch(JSONObject json) throws JSONException {
		
		String name;
		if (json.has("module")) {
			name = json.getJSONArray("module").getJSONObject(0).getString("name");
		}
		else {
			name = json.getString("name");
		}
		
		String authorPauseId       = json.getString("author");
		String distributionName    = json.getString("distribution");
		String distributionVersion = json.getString("version");
		
		return new Module(name, authorPauseId, distributionName, distributionVersion);
	}

	public Module(String name, String authorPauseId, String distributionName, String distributionVersion) {
		this.name                = name;
		this.authorPauseId       = authorPauseId;
		this.distributionName    = distributionName;
		this.distributionVersion = distributionVersion;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthorPauseId() {
		return authorPauseId;
	}

	public void setAuthorPauseId(String authorPauseId) {
		this.authorPauseId = authorPauseId;
	}

	public String getDistributionName() {
		return distributionName;
	}

	public void setDistributionName(String distributionName) {
		this.distributionName = distributionName;
	}

	public String getDistributionVersion() {
		return distributionVersion;
	}

	public void setDistributionVersion(String distributionVersion) {
		this.distributionVersion = distributionVersion;
	}

	public int getDistributionRatingCount() {
		return distributionRatingCount;
	}

	public void setDistributionRatingCount(int distributionRatingCount) {
		this.distributionRatingCount = distributionRatingCount;
	}

	public double getDistributionRating() {
		return distributionRating;
	}

	public void setDistributionRating(double distributionRating) {
		this.distributionRating = distributionRating;
	}

}
