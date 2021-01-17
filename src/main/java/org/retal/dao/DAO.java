package org.retal.dao;

import java.util.List;
import org.retal.domain.MethodUndefinedException;

/**
 * Generic interface for basic CRUD operations.
 * 
 * @author Alexander Retivov
 *
 * @param <T> Entity class for which interface is implemented
 */
public interface DAO<T> {
  /**
   * Adds entity to database.
   * 
   * @param t entity object to add
   */
  public void add(T t);

  /**
   * Reads entity from database using its primary key (composite keys are supported). If no entity
   * found with given primary key, null is returned. It is recommended to validate keys input with
   * {@link DAO#validatePrimaryKeys(Object...)}, since they are given as
   * {@linkplain java.lang.Object Object} instances.
   * 
   * @param keys set of primary keys for identifying entity
   * @return found entity object or null
   * @throws IllegalArgumentException is keys input is invalid
   */
  public T read(Object... keys) throws IllegalArgumentException;

  /**
   * Reads all entities from database. This method is not necessary to override, but if it is called
   * with its default implementation, an exception will be thrown.
   * 
   * @return List of all entities read from database
   * @throws MethodUndefinedException if default method implementation is called
   * 
   */
  public default List<T> readAll() throws MethodUndefinedException {
    throw new MethodUndefinedException();
  }

  /**
   * Deletes given entity from database.
   * 
   * @param t entity to be deleted
   */
  public void delete(T t);

  /**
   * Updates entity in database with corresponding primary key to given entity.
   * 
   * @param t given entity to update database with
   */
  public void update(T t);

  /**
   * Validates primary key input in {@link DAO#read(Object...)}. If input is invalid, an exception
   * is thrown. This method <b>should not</b> be overridden.
   * 
   * @param classes - primary key classes.
   * @param objects - primary key.
   * @throws IllegalArgumentException if the input is invalid
   * 
   */
  public default void validatePrimaryKeys(Class<?>[] classes, Object... objects)
      throws IllegalArgumentException {
    boolean isValid = objects.length == classes.length;
    for (int i = 0; i < classes.length; i++) {
      isValid &= classes[i].isInstance(objects[i]);
    }
    if (!isValid) {
      String message = "";
      for (Class<?> c : classes) {
        message += c.getName() + ", ";
      }
      message = message.substring(0, message.length() - 2);
      throw new IllegalArgumentException("Primary key must be " + message);
    }
  }
}
