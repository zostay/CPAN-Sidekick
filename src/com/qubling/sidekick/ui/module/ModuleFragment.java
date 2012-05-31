package com.qubling.sidekick.ui.module;

import android.support.v4.app.Fragment;

/**
 * Base class for all module search/view fragments. These fragments must always
 * be used within a {@link ModuleActivity}.
 *
 * @author sterling
 */
public class ModuleFragment extends Fragment {
	protected static final float GRAVATAR_DP_SIZE = 61f;
    
    protected ModuleActivity getModuleActivity() {
    	return (ModuleActivity) getActivity();
    }
}
