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
        Module module = (Module) intent.getParcelableExtra(EXTRA_MODULE);
        
        setTitle(release.getName() + "-" + release.getVersion());

        FragmentManager fragmentManager = getSupportFragmentManager();
        ReleaseInfoFragment releaseFragment = (ReleaseInfoFragment) fragmentManager.findFragmentById(R.id.release_info_fragment);
        releaseFragment.setRelease(release);
        
        ModuleViewFragment moduleFragment = (ModuleViewFragment) fragmentManager.findFragmentById(R.id.module_view_fragment);
        if (moduleFragment != null) {
            moduleFragment.setModule(module);
            moduleFragment.fetchModule();
        }
    }

    private ModuleViewFragment getModuleViewFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return (ModuleViewFragment) fragmentManager.findFragmentById(R.id.module_view_fragment);
    }

    @Override
    public void onModuleClick(Module clickedModule) {
        ModuleViewFragment fragment = getModuleViewFragment();

        // Tablet
        if (fragment != null) {
            fragment.setModule(clickedModule);
            fragment.fetchModule();
        }
        
        // Phone
        else {
            Intent moduleViewIntent = new Intent(this, ModuleViewActivity.class);
            moduleViewIntent.putExtra(ModuleActivity.EXTRA_MODULE, clickedModule);
            startActivity(moduleViewIntent);
        }
    }
}
