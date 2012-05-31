package com.qubling.sidekick.fetch.cpan;

import com.qubling.sidekick.fetch.AbstractFetcher;
import com.qubling.sidekick.model.Instance;
import com.qubling.sidekick.model.Model;

public abstract class CPANFetcher<SomeInstance extends Instance<SomeInstance>> extends AbstractFetcher<SomeInstance> {
	public static final String METACPAN_API_URL = "http://api.metacpan.org/";
    public static final String METACPAN_API_POD_URL = METACPAN_API_URL + "pod/";
    public static final String METACPAN_API_MODULE_URL = METACPAN_API_URL + "module/";
    public static final String METACPAN_API_RELEASE_URL = METACPAN_API_URL + "release/";

    public static final String METACPAN_URL = "http://metacpan.org/";
    public static final String METACPAN_MODULE_URL = METACPAN_URL + "module/";
    public static final String METACPAN_RELEASE_URL = METACPAN_URL + "release/";
    
    public CPANFetcher(Model<SomeInstance> model) {
    	super(model);
    }
}
