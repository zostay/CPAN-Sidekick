package com.qubling.sidekick.ui.release;

import com.qubling.sidekick.R;
import com.qubling.sidekick.Util;
import com.qubling.sidekick.instance.Module;
import com.qubling.sidekick.instance.Release;
import com.qubling.sidekick.ui.module.ModuleActivity;
import com.qubling.sidekick.ui.module.ModuleViewActivity;
import com.qubling.sidekick.ui.module.ModuleViewFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class ReleaseViewActivity extends ModuleActivity {
    public static final String EXTRA_RELEASE = "com.qubling.sidekick.intent.extra.RELEASE";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        
        setContentView(R.layout.release_view);

        // Setup BugSense
        Util.setupBugSense(this);
        
        Intent intent = getIntent();
        Release release = (Release) intent.getParcelableExtra(EXTRA_RELEASE);
        
        setTitle(release.getName() + "-" + release.getVersion());

        FragmentManager fragmentManager = getSupportFragmentManager();
        ReleaseInfoFragment fragment = (ReleaseInfoFragment) fragmentManager.findFragmentById(R.id.release_info_fragment);
        fragment.setRelease(release);
    }

    @Override
    public void onModuleClick(Module clickedModule) {
        Intent moduleViewIntent = new Intent(this, ModuleViewActivity.class);
        moduleViewIntent.putExtra(ModuleViewActivity.EXTRA_MODULE, clickedModule);
        startActivity(moduleViewIntent);
    }
}
