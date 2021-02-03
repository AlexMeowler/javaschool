package org.retal.logiweb.service.jms;

import org.apache.log4j.Logger;
import org.retal.table.ejb.jms.message.NotificationMessage;
import org.retal.table.ejb.jms.message.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationSender implements Sender {

  private final JmsTemplate template;

  private static final Logger log = Logger.getLogger(NotificationSender.class);

  @Autowired
  public NotificationSender(JmsTemplate template) {
    this.template = template;
  }

  @Override
  public void send(Object message) {
    if (message instanceof NotificationMessage) {
      try {
        template.send(s -> s.createObjectMessage((NotificationMessage) message)); 
      } catch(JmsException e) {
        log.error("Could not send message: ");
        log.error(e, e);
      }
    } else {
      log.warn("Input message is not an instance of " + NotificationMessage.class.getName());
    }

  }

  public void send(NotificationType type) {
    send(new NotificationMessage(type));
  }
}
