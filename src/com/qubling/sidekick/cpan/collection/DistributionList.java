/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.cpan.collection;

import com.qubling.sidekick.cpan.result.Author;
import com.qubling.sidekick.cpan.result.Distribution;

/**
 * An ordered, unique collection of {@link Distribution} objects.
 *
 * @author sterling
 *
 */
public class DistributionList extends ModelList<Distribution> {
    public synchronized Distribution load(String name, String version, Author author) {
        Distribution distribution = find(Distribution.makePrimaryID(name, version));

        if (distribution == null) {
            distribution = new Distribution(name, version, author);
            add(distribution);
        }

        return distribution;
    }

}
