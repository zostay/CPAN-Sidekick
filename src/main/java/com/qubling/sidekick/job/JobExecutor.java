package com.qubling.sidekick.job;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;

public class JobExecutor extends AsyncTask<Runnable, Void, Void> {
	private Activity activity;
	private List<Runnable> plan;
	
	public JobExecutor(Activity activity) {
		this.activity = activity;
		this.plan = new ArrayList<Runnable>();
	}
	
	public void addCommand(Runnable command) {
		plan.add(command);
	}
	
	public void addCommand(Runnable command, final Runnable followup) {
		plan.add(command);
		
		if (followup != null) {
			plan.add(new Runnable() {
				
				@Override
				public void run() {
					activity.runOnUiThread(followup);
				}
				
				@Override
				public String toString() {
					return "UI Notify " + followup;
				}
			});
		}
	}

	@Override
	protected Void doInBackground(Runnable... finalCommands) {
//		Log.d("JobExecutor", "doInBackground() Starting Jobs");
		for (Runnable command : plan) {
//			Log.d("JobExecutor", "Start " + command);
			command.run();
//			Log.d("JobExecutor", "End " + command);
		}
//		Log.d("JobExecutor", "doInBackground() Ending Jobs");
		
//		Log.d("JobExecutor", "doInBackground() Starting Final Jobs");
		for (Runnable command : finalCommands) {
//			Log.d("JobExecutor", "Start Final " + command);
			command.run();
//			Log.d("JobExecutor", "End Final " + command);
		}
//		Log.d("JobExecutor", "doInBackground() Ending Final Jobs");
		
		return null;
	}

}
