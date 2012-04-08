/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.cpan.result;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.qubling.sidekick.cpan.collection.AuthorList;
import com.qubling.sidekick.cpan.collection.DistributionList;
import com.qubling.sidekick.cpan.collection.ModuleList;

/**
 * Information for a single Perl module loaded from CPAN.
 *
 * @author sterling
 *
 */
public class Module extends Model {

    private String name;
    private String moduleAbstract;

    private Distribution distribution;

    public static Module fromModuleSearch(JSONObject json, AuthorList authors, DistributionList distributions) {

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

        Author author = authors.load(authorPauseId);
        Distribution distribution = distributions.load(distributionName, distributionVersion, author);

        return new Module(name, moduleAbstract, distribution);
    }

    public Module(String name) {
        this.name = name;
        this.moduleAbstract = "...";
        this.distribution = null;
    }

    public Module(String name, String moduleAbstract, Distribution distribution) {
        this.name           = name;
        this.moduleAbstract = moduleAbstract;
        this.distribution   = distribution;
    }

    public Module(String name, Distribution distribution) {
        this.name           = name;
        this.moduleAbstract = "...";
        this.distribution   = distribution;
    }

    public Module(Parcel in) {
        name           = in.readString();
        moduleAbstract = in.readString();
        distribution   = in.readParcelable(Module.class.getClassLoader());
    }

    @Override
    public String getPrimaryID() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof Module) && ((Module) object).name.equals(name);
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

    public Author getAuthor() {
        return distribution.getAuthor();
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public boolean isModuleFetchNeeded() {
        return "...".equals(this.moduleAbstract)
            || distribution.getAuthor() == null
            || distribution == null;
    }

    public void setDistribution(Distribution distribution) {
        this.distribution = distribution;
    }

    public static final Parcelable.Creator<Module> CREATOR
            = new Parcelable.Creator<Module>() {
        @Override
        public Module createFromParcel(Parcel in) {
            return new Module(in);
        }

        @Override
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
        dest.writeParcelable(distribution, flags);
    }

    public ModuleList toModuleList() {
        ModuleList moduleList = new ModuleList();
        moduleList.add(this);
        return moduleList;
    }

}
