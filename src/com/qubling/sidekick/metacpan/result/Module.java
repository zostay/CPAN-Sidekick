package com.qubling.sidekick.metacpan.result;

import org.json.JSONException;
import org.json.JSONObject;

public class Module {
	
	private String moduleName;
	private String moduleAbstract;
	
	private String authorPauseId;
	
	private String distributionName;
	private String distributionVersion;
	private int distributionRatingCount;
	private double distributionRating;
	
	public static Module fromModuleSearch(JSONObject json) {
		
		String name = null;
		try {
			if (json.has("module")) {
				name = json.getJSONArray("module").getJSONObject(0).getString("name");
			}
			else {
				name = json.getString("name");
			}
		}
		catch (JSONException e) {
			name = "Unknown Module Name";
		}
		
		String moduleAbstract      = null;
		String authorPauseId       = null;
		String distributionName    = null;
		String distributionVersion = null;
		
		try { moduleAbstract      = json.getString("abstract");     } catch (JSONException e) {}
		try { authorPauseId       = json.getString("author");       } catch (JSONException e) {}
		try { distributionName    = json.getString("distribution"); } catch (JSONException e) {}
		try { distributionVersion = json.getString("version");      } catch (JSONException e) {}
		
		return new Module(name, moduleAbstract, authorPauseId, distributionName, distributionVersion);
	}

	public Module(String name, String moduleAbstract, String authorPauseId, String distributionName, String distributionVersion) {
		this.moduleName          = name;
		this.moduleAbstract      = moduleAbstract;
		this.authorPauseId       = authorPauseId;
		this.distributionName    = distributionName;
		this.distributionVersion = distributionVersion;
	}
	
	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String name) {
		this.moduleName = name;
	}

	public String getModuleAbstract() {
		return moduleAbstract;
	}

	public void setModuleAbstract(String moduleAbstract) {
		this.moduleAbstract = moduleAbstract;
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
