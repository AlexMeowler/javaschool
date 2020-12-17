package org.retal.dao;

import java.util.List;

public interface DAO <T>
{
	public void save (T t);
	public T read(int primaryKey);
	public default List<T> readAll()
	{
		throw new RuntimeException("Method undefined");
	}
	public void delete(T t);
	public void update(T t, String... args);
}
