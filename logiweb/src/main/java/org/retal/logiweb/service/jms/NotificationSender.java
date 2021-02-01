package org.retal.logiweb.service.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationSender implements Sender {
  
  private final JmsTemplate template;
  
  @Autowired
  public NotificationSender(JmsTemplate template) {
    this.template = template;
  }
  
  @Override
  public void send(Object message) {
    template.send(s -> s.createObjectMessage(new String("Recovery test")));
  }
}
