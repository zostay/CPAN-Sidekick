package com.qubling.sidekick.ui.release;

import com.qubling.sidekick.R;
import com.qubling.sidekick.instance.Release;
import com.qubling.sidekick.ui.module.ModuleViewFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class ReleaseViewActivity extends FragmentActivity {
    public static final String EXTRA_RELEASE = "com.qubling.sidekick.intent.extra.RELEASE";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        
        setContentView(R.layout.release_view);
        
        Intent intent = getIntent();
        Release release = (Release) intent.getParcelableExtra(EXTRA_RELEASE);
        
        setTitle(release.getName() + "-" + release.getVersion());

        FragmentManager fragmentManager = getSupportFragmentManager();
        ReleaseInfoFragment fragment = (ReleaseInfoFragment) fragmentManager.findFragmentById(R.id.release_info_fragment);
        fragment.setRelease(release);
    }
}
