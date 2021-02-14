package org.retal.logiweb.dao.interfaces;

/**
 * Interface, indicating that DAO implementing it is able to return the amount of rows in associated
 * table.
 * 
 * @author Alexander Retivov
 *
 */
public interface CountableRows {

  /**
   * Returns the amount of rows in table.
   * @return the amount of rows
   */
  public int getRowsAmount();
}
