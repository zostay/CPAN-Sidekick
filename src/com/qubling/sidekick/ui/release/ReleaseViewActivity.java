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
            
            releaseFragment.selectModule(module);
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
    
    @Override
    public void onReleaseClick(Module clickedModule) {
        // Do nothing. We only allow modules that belong to the current release.
    }
    
    @Override
    public boolean isModuleAcceptableForThisActivity(Module module) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ReleaseInfoFragment releaseFragment = (ReleaseInfoFragment) fragmentManager.findFragmentById(R.id.release_info_fragment);
        Release currentRelease = releaseFragment.getRelease();
        
        // TODO In the future, it might be nice to keep the release activity 
        // we have up and just manage our own history similar to what the module 
        // view fragment does, but this is good enough for now.
        
        // The release does not match, start a new activity
        if (!module.getReleaseName().equals(currentRelease.getName())
                || !module.getRelease().getVersion().equals(currentRelease.getVersion())) {
            
            Release otherRelease = module.getRelease();
            Intent moduleReleaseIntent = new Intent(this, ReleaseViewActivity.class);
            moduleReleaseIntent.putExtra(ReleaseViewActivity.EXTRA_RELEASE, otherRelease);
            moduleReleaseIntent.putExtra(ReleaseViewActivity.EXTRA_MODULE, module);
            startActivity(moduleReleaseIntent);
            
            return false;
        }
        
        // Fine, this module is in the same release, load the module in the current activity
        else {
            return true;
        }
    }
}
