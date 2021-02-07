package org.retal.table.jms;

/**
 * Interface for JMS senders.
 * @author Alexander Retivov
 *
 */
public interface Sender {
  
  /**
   * Sends message to MQ server.
   * @param message to be sent
   */
  public void send(Object message);
  
}
