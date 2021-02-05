package org.retal.table.jms;

import java.io.Serializable;

public class NotificationMessage implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  private final NotificationType type;
  
  public NotificationMessage(NotificationType type) {
    this.type = type;
  }
  
  public NotificationType getType() {
    return type;
  }
}
