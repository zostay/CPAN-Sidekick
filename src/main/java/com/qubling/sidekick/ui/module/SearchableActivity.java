package com.qubling.sidekick.ui.module;

import android.content.ComponentName;
import android.view.MenuInflater;
import android.view.View;

public interface SearchableActivity {
    public Object getSystemService(String name);
    public ComponentName getComponentName();
    public void doNewSearch(String query);
    public MenuInflater getMenuInflater();
    public View findViewById(int id);
}
