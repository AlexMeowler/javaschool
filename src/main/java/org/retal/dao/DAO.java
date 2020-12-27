package org.retal.dao;

import java.util.List;

public interface DAO<T> {
	public void add(T t);

	public T read(Object... keys);

	public default List<T> readAll() {
		throw new RuntimeException("Method undefined");
	}

	public T find(String... args);

	public void delete(T t);

	public void deleteById(int id);

	public void update(T a);
}
