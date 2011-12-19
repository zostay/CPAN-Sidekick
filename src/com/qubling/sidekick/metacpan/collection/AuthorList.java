package com.qubling.sidekick.metacpan.collection;

import com.qubling.sidekick.metacpan.result.Author;

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
