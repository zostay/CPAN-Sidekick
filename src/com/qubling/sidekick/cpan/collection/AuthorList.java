/*
 * Copyright 2011 Qubling Software, LLC.
 *
 * This software may be distributed under the terms of the Artistic License 2.0.
 */
package com.qubling.sidekick.cpan.collection;

import com.qubling.sidekick.api.ModelList;
import com.qubling.sidekick.cpan.result.Author;

/**
 * An ordered, unique collection of {@link Author} objects.
 *
 * @author sterling
 *
 */
public class AuthorList extends ModelList<Author> {
    public synchronized Author load(String pauseId) {
        Author author = find(pauseId);

        if (author == null) {
            author = new Author(pauseId);
            add(author);
        }

        return author;
    }
}
