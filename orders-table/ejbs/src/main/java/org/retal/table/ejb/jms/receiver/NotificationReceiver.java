package org.retal.table.ejb.jms.receiver;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.apache.log4j.Logger;
import org.retal.table.ejb.jms.message.NotificationMessage;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "notificationsQueue")})
public class NotificationReceiver implements MessageListener {

  @Resource
  private MessageDrivenContext context;

  private static final Logger log = Logger.getLogger(NotificationReceiver.class);

  @Override
  public void onMessage(Message message) {
    try {
      NotificationMessage msg = (NotificationMessage) ((ObjectMessage) message).getObject();
      log.info("Received notification of type " + msg.getType().toString());
    } catch (JMSException e) {
      log.error(e, e);
    }
  }

}
