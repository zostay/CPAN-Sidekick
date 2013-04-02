package com.qubling.sidekick.util;

import java.util.Collection;
import java.util.LinkedList;

public class LinkedListStack<E> extends LinkedList<E> implements Stack<E> {

    private static final long serialVersionUID = 5516264857414221487L;

	public LinkedListStack() {}

	public LinkedListStack(Collection<? extends E> collection) {
		super(collection);
	}
	
	public void push(E e) {
		addFirst(e);
	}
	
	public E pop() {
		return removeFirst();
	}

}
