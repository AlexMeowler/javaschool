package org.retal.table.ejb.jms.receiver;

import javax.annotation.PostConstruct;
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
import org.retal.table.ejb.ws.GetLatestOrdersResponse;
import org.retal.table.ejb.ws.Statistics;
import org.retal.table.ejb.ws.StatisticsService;

@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "notificationsQueue")})
public class NotificationReceiver implements MessageListener, Receiver {

  @Resource
  private MessageDrivenContext context;

  private static final Logger log = Logger.getLogger(NotificationReceiver.class);

  @Override
  public boolean messageBodySupported(Class<?> messageClass) {
    return messageClass.isAssignableFrom(NotificationMessage.class);
  }

  @Override
  public void onMessage(Message message) {
    try {
      ObjectMessage objectMessage = (ObjectMessage) message;
      if (messageBodySupported(objectMessage.getObject().getClass())) {
        NotificationMessage msg = (NotificationMessage) objectMessage.getObject();
        log.info("Received notification of type " + msg.getType().toString());
      } else {
        log.warn("Incompatible message type for receiver " + this.getClass().getName());
      }
    } catch (JMSException e) {
      log.error(e, e);
    }
  }

}
