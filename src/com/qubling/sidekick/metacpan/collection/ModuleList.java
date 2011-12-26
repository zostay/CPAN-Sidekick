/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.metacpan.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.qubling.sidekick.metacpan.result.Module;

/**
 * An ordered, unique list of {@link Module} objects.
 * 
 * @author sterling
 *
 */
public class ModuleList extends ModelList<Module> {

    public interface OnModuleListUpdated extends OnModelListUpdated<Module> { }

    public interface OnMoreItemsRequested {
        public void onMoreItemsRequested(ModuleList moduleList);
    }

    private int totalCount;

    private List<OnMoreItemsRequested> moreItemsRequested;

    public ModuleList() {
        this(0);
    }

    public ModuleList(int totalCount) {
        super();

        this.totalCount = totalCount;
        this.moreItemsRequested = new ArrayList<OnMoreItemsRequested>();
    }

    public ModuleList(Module[] modules, int totalCount) {
        super(modules.length);

        this.totalCount = totalCount;
        this.moreItemsRequested = new ArrayList<OnMoreItemsRequested>();

        Collections.addAll(this, modules);
    }

    public ModuleList(Collection<? extends Module> collection, int totalCount) {
        super(collection);

        this.totalCount = totalCount;
        this.moreItemsRequested = new ArrayList<OnMoreItemsRequested>();
    }

    @Override
    public void clear() {
        super.clear();
        setTotalCount(0);
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public synchronized void addMoreItemsRequestedListener(OnMoreItemsRequested listener) {
        moreItemsRequested.add(listener);
    }

    public synchronized void removeMoreItemsReqeustedListener(OnMoreItemsRequested listener) {
        moreItemsRequested.remove(listener);
    }

    public synchronized void requestMoreItems() {
        for (OnMoreItemsRequested listener : moreItemsRequested) {
            listener.onMoreItemsRequested(this);
        }
    }

    public AuthorList extractAuthorList() {
        AuthorList authorList = new AuthorList();
        for (Module module : this) {
            authorList.add(module.getAuthor());
        }

        authorList.setParent(this);
        return authorList;
    }

    public DistributionList extractDistributionList() {
        DistributionList distributionList = new DistributionList();
        for (Module module : this) {
            distributionList.add(module.getDistribution());
        }

        distributionList.setParent(this);
        return distributionList;
    }
}
