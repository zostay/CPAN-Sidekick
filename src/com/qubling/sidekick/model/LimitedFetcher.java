package com.qubling.sidekick.model;

public interface LimitedFetcher<SomeInstance extends Instance<SomeInstance>> extends Fetcher<SomeInstance> {
	public int getFrom();
	public void setFrom(int from);
	
	public int getSize();
	public void setSize(int size);
}
