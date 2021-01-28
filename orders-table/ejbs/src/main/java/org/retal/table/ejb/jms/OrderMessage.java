package org.retal.table.ejb.jms;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.apache.log4j.Logger;
import org.retal.table.ejb.jms.MessageSender.MessageEntity;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/test")})
public class OrderMessage implements MessageListener {

  @Resource
  private MessageDrivenContext context;

  private static final Logger log = Logger.getLogger(OrderMessage.class);

  @Override
  public void onMessage(Message message) {

    log.info("---MESSAGE RECEIVED---");
    try {
      log.info("Message was: \"" + ((MessageEntity) ((ObjectMessage) message).getObject()).getText()
          + "\"");
    } catch (JMSException e) {
      log.error(e, e);
    }
  }

}
