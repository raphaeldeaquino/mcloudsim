package br.ufg.inf.mcloudsim.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class QueryableIterator<E> implements Iterator<E> {

	private E[] collection;
	private int indexNext;
	private int indexCurr;

	@SuppressWarnings("unchecked")
	public QueryableIterator(Collection<E> collection) {
		if (collection == null)
			throw new IllegalArgumentException();

		this.collection = (E[]) new Object[collection.size()];
		this.collection = collection.toArray(this.collection);
		this.indexNext = 0;
		this.indexCurr = -1;
	}

	@Override
	public boolean hasNext() {
		return indexNext < collection.length;
	}

	@Override
	public E next() {
		if (!hasNext())
			throw new NoSuchElementException();

		this.indexCurr++;

		return collection[indexNext++];
	}

	public E queryCurrent() {
		if (this.indexCurr < 0)
			return null;

		return collection[indexCurr];
	}

	public E queryNext() {
		if (!hasNext())
			return null;

		return collection[indexNext];
	}

	public E queryFollowingOfNext() {
		if (indexNext + 1 >= collection.length)
			return null;

		return collection[indexNext + 1];
	}

}
