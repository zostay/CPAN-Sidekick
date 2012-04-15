/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.api;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Base class for building ordered, unique collections of {@link Model} objects.
 *
 * @author sterling
 *
 * @param <SomeModel> The type of {@link Model} being collected.
 */
public abstract class ModelList<SomeModel extends Model> extends AbstractList<SomeModel> {

    public interface OnModelListUpdated<SomeModel extends Model> {
        public void onModelListUpdated(ModelList<SomeModel> modelList);
    }

    private ModelList<? extends Model> parent;

    private List<SomeModel> modelList;
    private Map<Object, SomeModel> modelSet;

    private List<OnModelListUpdated<SomeModel>> modelListUpdaters = new ArrayList<OnModelListUpdated<SomeModel>>();

    public ModelList() {
        modelSet  = new HashMap<Object, SomeModel>();
        modelList = new ArrayList<SomeModel>();
    }

    public ModelList(Collection<? extends SomeModel> collection) {
        modelSet  = new HashMap<Object, SomeModel>(collection.size());
        modelList = new ArrayList<SomeModel>(collection.size());

        addAll(collection);
    }

    public ModelList(int capacity) {
        modelSet  = new HashMap<Object, SomeModel>(capacity);
        modelList = new ArrayList<SomeModel>(capacity);
    }

    public ModelList<? extends Model> getParent() {
        return parent;
    }

    protected List<SomeModel> getModelList() {
        return modelList;
    }

    protected Map<Object, SomeModel> getModelMap() {
        return modelSet;
    }

    public synchronized void setParent(ModelList<? extends Model> parent) {
        this.parent = parent;
    }

    public synchronized void addModelListUpdatedListener(OnModelListUpdated<SomeModel> listener) {
        modelListUpdaters.add(listener);
    }

    public synchronized void removeModelListUpdatedListener(OnModelListUpdated<SomeModel> listener) {
        modelListUpdaters.remove(listener);
    }

    public synchronized void notifyModelListUpdated() {
//        Log.d("ModuleList", "notifyModelListUpdated");

        for (OnModelListUpdated<SomeModel> listener : modelListUpdaters) {
            listener.onModelListUpdated(this);
        }

        // Cascade the notifications up to the parent, if any
        if (parent != null)
            parent.notifyModelListUpdated();
    }

    @Override
    public synchronized void add(int location, SomeModel object) {
        if (modelSet.containsKey(object.getPrimaryID())) return;

        modelList.add(location, object);
        modelSet.put(object.getPrimaryID(), object);
    }

    @Override
    public synchronized boolean add(SomeModel object) {
        if (modelSet.containsKey(object.getPrimaryID())) return false;

        modelList.add(object);
        modelSet.put(object.getPrimaryID(), object);

        return true;
    }

    @Override
    public synchronized boolean addAll(int location, Collection<? extends SomeModel> collection) {
        int originalSize = modelSet.size();
        int scrollingLocation = location;

        for (SomeModel item : collection) {
            if (modelSet.containsKey(item.getPrimaryID())) continue;

            modelList.add(scrollingLocation++, item);
            modelSet.put(item.getPrimaryID(), item);
        }

        return modelSet.size() > originalSize;
    }

    @Override
    public synchronized boolean addAll(Collection<? extends SomeModel> collection) {
        int originalSize = modelSet.size();

        for (SomeModel item : collection) {
            if (modelSet.containsKey(item.getPrimaryID())) continue;

            modelList.add(item);
            modelSet.put(item.getPrimaryID(), item);
        }

        return modelSet.size() > originalSize;
    }

    @Override
    public synchronized void clear() {
        modelList.clear();
        modelSet.clear();
    }

    @Override
    public synchronized boolean equals(Object object) {
        if (!(object instanceof List)) return false;

        List<?> otherList = (List<?>) object;
        if (otherList.size() != modelList.size()) return false;

        for (int i = 0; i < modelList.size(); i++) {
            Object otherItem = otherList.get(i);
            Object myItem    = modelList.get(i);

            if (!otherItem.equals(myItem)) return false;
        }

        return true;
    }

    public synchronized SomeModel find(Object primaryID) {
        return modelSet.get(primaryID);
    }

    @Override
    public synchronized SomeModel get(int location) {
        return modelList.get(location);
    }

    @Override
    public synchronized int hashCode() {
        return modelList.hashCode();
    }

    @Override
    public synchronized int indexOf(Object object) {
        return modelList.indexOf(object);
    }

    @Override
    public synchronized Iterator<SomeModel> iterator() {
        return modelList.iterator();
    }

    @Override
    public synchronized int lastIndexOf(Object object) {
        return modelList.lastIndexOf(object);
    }

    @Override
    public synchronized SomeModel remove(int location) {
        SomeModel item = modelList.remove(location);
        modelSet.remove(item);
        return item;
    }

    @Override
    public synchronized SomeModel set(int location, SomeModel object) {
        SomeModel oldItem = modelList.set(location, object);
        modelSet.remove(oldItem);

        // Make sure that if this is a moving item, to rip out the original
        for (int i = 0; i < modelList.size(); i++) {
            if (i == location) continue;
            if (modelList.get(i).equals(object)) {
                modelList.remove(i);
                break;
            }
        }

        return oldItem;
    }

    @Override
    public synchronized boolean contains(Object object) {
        if (!(object instanceof Model)) return false;
        return modelSet.containsKey(((Model) object).getPrimaryID());
    }

    @Override
    public synchronized boolean isEmpty() {
        return modelList.isEmpty();
    }

    @Override
    public synchronized boolean remove(Object object) {
        modelSet.remove(object);
        return modelList.remove(object);
    }

    @Override
    public synchronized int size() {
        return modelList.size();
    }

    @Override
    public synchronized Object[] toArray() {
        return modelList.toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] contents) {
        return modelList.toArray(contents);
    }

}
