package org.retal.logiweb.service;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.retal.logiweb.config.spring.web.RootConfig;
import org.retal.logiweb.config.spring.web.WebConfig;
import org.retal.logiweb.service.jms.NotificationSender;
import org.retal.table.ejb.jms.message.NotificationType;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, RootConfig.class})
@WebAppConfiguration
public class JmsTest {

  static class ContextConfiguration {

    private JmsTemplate template(String url) {
      ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
      activeMQConnectionFactory.setBrokerURL(url);
      SingleConnectionFactory factory = new SingleConnectionFactory(activeMQConnectionFactory);
      factory.setReconnectOnException(true);
      JmsTemplate template = new JmsTemplate(factory);
      template.setDefaultDestinationName("testQueue");
      return template;
    }

    public JmsTemplate jmsTemplateCorrect() {
      return template("vm://localhost?broker.persistent=false");
    }

    public JmsTemplate jmsTemplateWrong() {
      return template("abc");
    }
  }
  
  @Test
  public void testNotificationSenderNoConnection() {
    NotificationSender sender =
        new NotificationSender(new ContextConfiguration().jmsTemplateWrong());
    sender.send(NotificationType.ORDERS_UPDATE);
  }

  @Test
  public void testNotificationSenderIllegalClass() {
    NotificationSender sender =
        new NotificationSender(new ContextConfiguration().jmsTemplateCorrect());
    sender.send(new Object());
  }
}
