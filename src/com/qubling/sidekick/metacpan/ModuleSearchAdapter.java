package com.qubling.sidekick.metacpan;

import com.qubling.sidekick.R;
import com.qubling.sidekick.metacpan.result.Module;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ModuleSearchAdapter extends ArrayAdapter<Module> {
	
	private int layout;
	private LayoutInflater inflater;

	public ModuleSearchAdapter(Context context, int layout, Module[] items) {
		super(context, layout, items);
		
		this.layout = layout;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			row = inflater.inflate(layout, null);
		}
		
		Module item = getItem(position);
		TextView moduleName = (TextView) row.findViewById(R.id.module_name);
		moduleName.setText(item.getName());
		
		TextView distributionName = (TextView) row.findViewById(R.id.module_author_distribution);
		distributionName.setText(
				item.getAuthorPauseId()
				+ "/" + item.getDistributionName()
				+ "-" + item.getDistributionVersion());
		
		return row;
	}
	
}
