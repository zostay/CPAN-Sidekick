package com.qubling.sidekick.metacpan.result;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Module implements Parcelable {
	
	private String moduleName;
	private String moduleAbstract;
	
	private String authorPauseId;
	private Bitmap authorGravatarBitmap;
	
	private String distributionName;
	private String distributionVersion;
	private int distributionRatingCount;
	private double distributionRating;
	
	private int distributionFavoriteCount;
	private boolean distributionMyFavorite;
	
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
	
	public Module(Parcel in) {
		moduleName                = in.readString();
		moduleAbstract            = in.readString();
		authorPauseId             = in.readString();
		authorGravatarBitmap      = in.readParcelable(null);
		distributionName          = in.readString();
		distributionVersion       = in.readString();
		distributionRatingCount   = in.readInt();
		distributionRating        = in.readDouble();
		distributionFavoriteCount = in.readInt();
		distributionMyFavorite    = in.readByte() != 0 ? true : false;
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

	public Bitmap getAuthorGravatarBitmap() {
		return authorGravatarBitmap;
	}

	public void setAuthorGravatarBitmap(Bitmap authorGravatarBitmap) {
		this.authorGravatarBitmap = authorGravatarBitmap;
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

	public int getDistributionFavoriteCount() {
		return distributionFavoriteCount;
	}

	public void setDistributionFavoriteCount(int distributionFavoriteCount) {
		this.distributionFavoriteCount = distributionFavoriteCount;
	}

	public boolean isDistributionMyFavorite() {
		return distributionMyFavorite;
	}

	public void setDistributionMyFavorite(boolean distributionMyFavorite) {
		this.distributionMyFavorite = distributionMyFavorite;
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
		dest.writeString(moduleName);
		dest.writeString(moduleAbstract);
		dest.writeString(authorPauseId);
		dest.writeParcelable(authorGravatarBitmap, 0);
		dest.writeString(distributionName);
		dest.writeString(distributionVersion);
		dest.writeInt(distributionRatingCount);
		dest.writeDouble(distributionRating);
		dest.writeInt(distributionFavoriteCount);
		dest.writeByte((byte) (distributionMyFavorite ? 1 : 0));
	}

}
