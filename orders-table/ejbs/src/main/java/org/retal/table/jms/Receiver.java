package org.retal.table.jms;

/**
 * Interface-marker for JMS receivers.
 * 
 * @author Alexander Retivov
 *
 */
public interface Receiver {

  /**
   * Checks whether received message is supported by receiver implementing this interface.
   * 
   * @param messageClass received message
   * @return true if message class is supported (depending on implementation), false otherwise
   */
  public boolean messageBodySupported(Class<?> messageClass);

}
