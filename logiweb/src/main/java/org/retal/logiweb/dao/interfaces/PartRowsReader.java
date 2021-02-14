package org.retal.logiweb.dao.interfaces;

import java.util.List;

/**
 * Interface, indicating that DAO implementing it is able to read the given amount of rows from
 * given position.
 * 
 * @author Alexander Retivov
 *
 */
public interface PartRowsReader<T> {

  /**
   * Reads part of table and returns it as list.
   * @param from number of row from which reading is started (the row itself is excluded)
   * @param amount amount of rows to be read from database
   * @return list of read rows
   */
  public List<T> readRows(int from, int amount);
}
