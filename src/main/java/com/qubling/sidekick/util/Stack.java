package com.qubling.sidekick.util;

import java.util.Collection;

public interface Stack<E> extends Collection<E> {
	public E pop();
	public void push(E e);
	public E peek();
}
