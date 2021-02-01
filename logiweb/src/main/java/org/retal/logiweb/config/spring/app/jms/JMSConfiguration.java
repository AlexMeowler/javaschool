package org.retal.logiweb.config.spring.app.jms;

import java.util.Properties;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class JMSConfiguration {
  
  private static final Logger log = Logger.getLogger(JMSConfiguration.class);

  @Bean
  public ActiveMQConnectionFactory senderActiveMQConnectionFactory() {
    ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
    Properties properties = new Properties();
    try {
      properties.load(JMSConfiguration.class.getResourceAsStream("/activemq.properties"));
    } catch (Exception e) {
      log.error("ActiveMQ config file not found");
      log.error(e, e);
      return null;
    }
    activeMQConnectionFactory.setBrokerURL(properties.getProperty("brokerUrl"));
    activeMQConnectionFactory.setUserName(properties.getProperty("username"));
    activeMQConnectionFactory.setPassword(properties.getProperty("password"));
    return activeMQConnectionFactory;
  }

  @Bean
  public SingleConnectionFactory singleConnectionFactory() {
    return new SingleConnectionFactory(senderActiveMQConnectionFactory());
  }

  @Bean
  public JmsTemplate jmsTemplate() {
    JmsTemplate template = new JmsTemplate(singleConnectionFactory());
    template.setDefaultDestinationName("notificationsQueue");
    return template;
  }
}
