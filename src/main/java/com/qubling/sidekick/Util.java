package com.qubling.sidekick;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;

public class Util {
	private static final String BUGSENSE_API_KEY = null;

	public static void setupBugSense(Context context) {
		if (Util.BUGSENSE_API_KEY != null)
			BugSenseHandler.setup(context, Util.BUGSENSE_API_KEY);
	}
	
	private Util() { }
}
