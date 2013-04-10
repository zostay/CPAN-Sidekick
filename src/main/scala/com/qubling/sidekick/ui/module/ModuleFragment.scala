package com.qubling.sidekick.ui.module

import android.support.v4.app.Fragment

/**
 * Base class for all module search/view fragments. These fragments must always
 * be used within a {@link ModuleActivity}.
 *
 * @author sterling
 */
object ModuleFragment {
  val GravatarDpSize = 61f
}

class ModuleFragment extends Fragment {
  def getModuleActivity : ModuleActivity = getActivity.asInstanceOf[ModuleActivity]
}
