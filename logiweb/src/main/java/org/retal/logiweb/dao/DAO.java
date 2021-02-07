package org.retal.logiweb.dao;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.logiweb.config.spring.app.hibernate.HibernateSessionFactory;
import org.retal.logiweb.domain.entity.MethodUndefinedException;

/**
 * Generic interface for basic CRUD operations. If method is not implemented properly due to some
 * reasons, it should throw {@link MethodUndefinedException}.
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
   * {@link DAO#validatePrimaryKeys(Class[], Object...)}, since they are given as
   * {@linkplain java.lang.Object Object} instances.
   * 
   * @param keys set of primary keys for identifying entity
   * @return found entity object or null
   * @throws IllegalArgumentException is keys input is invalid
   */
  public T read(Object... keys);

  /**
   * Reads all entities from database. This method is optional to override, but if it is called with
   * its default implementation, an exception will be thrown.
   * 
   * @return List of all entities read from database
   * @throws MethodUndefinedException if default method implementation is called
   * 
   */
  public default List<T> readAll() {
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
  public static void validatePrimaryKeys(Class<?>[] classes, Object... objects) {
    boolean isValid = objects.length == classes.length;
    for (int i = 0; i < classes.length; i++) {
      isValid &= classes[i].isInstance(objects[i]);
    }
    if (!isValid) {
      StringBuilder builder = new StringBuilder();
      
      for (Class<?> c : classes) {
        builder.append(c.getName() + ", ");
      }
      String message = builder.toString();
      message = message.substring(0, message.length() - 2);
      throw new IllegalArgumentException("Primary key must be " + message);
    }
  }

  /**
   * Utility method which starts session and begins transaction for that session.
   * 
   * @return open {@linkplain org.hibernate.Session Session}
   */
  public static Session start() {
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    session.beginTransaction();
    return session;
  }

  /**
   * Utility method which commits transaction (if transaction is started) for given session and
   * closes given it.
   * 
   * @param session open {@linkplain org.hibernate.Session Session} to close
   */
  public static void end(Session session) {
    Transaction transaction = session.getTransaction();
    if (transaction.isActive()) {
      transaction.commit();
    }
    session.close();
  }
}
