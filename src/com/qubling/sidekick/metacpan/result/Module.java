package com.qubling.sidekick.metacpan.result;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Module extends Model {
	
	private String name;
	private String moduleAbstract;
	
	private Author author;
	private Distribution distribution;
	
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
		this.name           = name;
		this.moduleAbstract = moduleAbstract;
		this.author         = new Author(authorPauseId);
		this.distribution   = new Distribution(distributionName, distributionVersion);
	}
	
	public Module(Parcel in) {
		name           = in.readString();
		moduleAbstract = in.readString();
		author         = in.readParcelable(Module.class.getClassLoader());
		distribution   = in.readParcelable(Module.class.getClassLoader());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbstract() {
		return moduleAbstract;
	}

	public void setAbstract(String moduleAbstract) {
		this.moduleAbstract = moduleAbstract;
	}

	public String getAuthorPauseId() {
		return author.getPauseId();
	}

	public void setAuthorPauseId(String authorPauseId) {
		author.setPauseId(authorPauseId);
	}

	public String getAuthorGravatarURL() {
		return author.getGravatarURL();
	}

	public void setAuthorGravatarURL(String authorGravatarURL) {
		author.setGravatarURL(authorGravatarURL);
	}

	public Bitmap getAuthorGravatarBitmap() {
		return author.getGravatarBitmap();
	}

	public void setAuthorGravatarBitmap(Bitmap authorGravatarBitmap) {
		author.setGravatarBitmap(authorGravatarBitmap);
	}

	public String getDistributionName() {
		return distribution.getName();
	}

	public void setDistributionName(String distributionName) {
		distribution.setName(distributionName);
	}

	public String getDistributionVersion() {
		return distribution.getVersion();
	}

	public void setDistributionVersion(String distributionVersion) {
		distribution.setVersion(distributionVersion);
	}

	public int getDistributionRatingCount() {
		return distribution.getRatingCount();
	}

	public void setDistributionRatingCount(int distributionRatingCount) {
		distribution.setRatingCount(distributionRatingCount);
	}

	public double getDistributionRating() {
		return distribution.getRating();
	}

	public void setDistributionRating(double distributionRating) {
		distribution.setRating(distributionRating);
	}

	public int getDistributionFavoriteCount() {
		return distribution.getFavoriteCount();
	}

	public void setDistributionFavoriteCount(int distributionFavoriteCount) {
		this.distribution.setFavoriteCount(distributionFavoriteCount);
	}

	public boolean isDistributionMyFavorite() {
		return distribution.isMyFavorite();
	}

	public void setDistributionMyFavorite(boolean distributionMyFavorite) {
		distribution.setMyFavorite(distributionMyFavorite);
	}
	
    public static final Parcelable.Creator<Module> CREATOR
            = new Parcelable.Creator<Module>() {
        public Module createFromParcel(Parcel in) {
        	return new Module(in);
        }

        public Module[] newArray(int size) {
            return new Module[size];
        }
    };

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(moduleAbstract);
		dest.writeParcelable(author, flags);
		dest.writeParcelable(distribution, flags);
	}

}
