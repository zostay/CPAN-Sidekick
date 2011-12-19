package com.qubling.sidekick.metacpan.collection;

import com.qubling.sidekick.metacpan.result.Distribution;

public class DistributionList extends ModelList<Distribution> {
	public synchronized Distribution load(String name, String version) {
		Distribution distribution = find(Distribution.makePrimaryID(name, version));
		
		if (distribution == null) {
			distribution = new Distribution(name, version);
			add(distribution);
		}
		
		return distribution;
	}

}
