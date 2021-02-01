package org.retal.table.ejb.jms.message;

public class NotificationMessage {
  
  private final NotificationType type;
  
  public NotificationMessage(NotificationType type) {
    this.type = type;
  }
  
  public NotificationType getType() {
    return type;
  }
}
