package org.retal.dao;

import java.util.List;

public interface DAO<T> {
	public void add(T t);

	public T read(Object... keys);

	public default List<T> readAll() {
		throw new MethodUndefinedException();
	}

	public void delete(T t);

	public void update(T t);
}
