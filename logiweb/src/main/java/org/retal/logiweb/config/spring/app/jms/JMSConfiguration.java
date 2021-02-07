package org.retal.logiweb.config.spring.app.jms;

import java.util.Arrays;
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

  /**
   * Configures and returns ActiveMQ ConnectionFactory instance. If resource file
   * "activemq.properties" is not found, then null is returned.
   * 
   * @return ActiveMQConnectionFactory
   */
  @Bean
  public ActiveMQConnectionFactory senderActiveMQConnectionFactory() {
    ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
    activeMQConnectionFactory.setTrustedPackages(Arrays.asList("org.retal.table.jms"));
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


  /**
   * SingleConnectionFactory bean. Uses {@link #senderActiveMQConnectionFactory()} for
   * configuration.
   * 
   * @return SingleConnectionFactory instance
   */
  @Bean
  public SingleConnectionFactory singleConnectionFactory() {
    SingleConnectionFactory factory =
        new SingleConnectionFactory(senderActiveMQConnectionFactory());
    factory.setReconnectOnException(true);
    return factory;
  }

  /**
   * JMS template which is used by other classes. Provides access to JMS.
   * Uses {@link #singleConnectionFactory()} for configuration.
   * @return JmsTemplate
   */
  @Bean
  public JmsTemplate jmsTemplate() {
    JmsTemplate template = new JmsTemplate(singleConnectionFactory());
    template.setDefaultDestinationName("notificationsQueue");
    return template;
  }
}
