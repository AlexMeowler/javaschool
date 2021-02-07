package org.retal.table.jms;

import java.io.Serializable;

/**
 * Class representing {@link Serializable} message for JMS
 * @author Alexander Retivov
 *
 */
public class NotificationMessage implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  private final NotificationType type;
  
  /**
   * Create new instance of this class with given {@link NotificationType}
   * @param type of notification
   */
  public NotificationMessage(NotificationType type) {
    this.type = type;
  }
  
  public NotificationType getType() {
    return type;
  }
}
