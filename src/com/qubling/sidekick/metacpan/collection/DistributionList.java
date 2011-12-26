/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.metacpan.collection;

import com.qubling.sidekick.metacpan.result.Distribution;

/**
 * An ordered, unique collection of {@link Distribution} objects.
 * 
 * @author sterling
 *
 */
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
