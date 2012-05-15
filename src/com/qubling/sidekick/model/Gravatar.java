package com.qubling.sidekick.model;

import android.graphics.Bitmap;

public class Gravatar extends Instance<Gravatar> {
	private String url;
	private Bitmap bitmap;
	
	public Gravatar(Model<Gravatar> model, String url) {
		super(model);
		
		this.url    = url;
	}
	
	public String getKey() {
		return url;
	}
	
	public String getUrl() {
		return url;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
}
