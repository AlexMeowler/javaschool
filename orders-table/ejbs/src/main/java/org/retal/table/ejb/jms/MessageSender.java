package org.retal.table.ejb.jms;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import org.apache.log4j.Logger;

@Singleton
@Startup
public class MessageSender {

  @Resource(mappedName = "java:jboss/DefaultJMSConnectionFactory")
  private ConnectionFactory connectionFactory;

  @Resource(mappedName = "java:jboss/exported/jms/queue/test")
  private Queue queue;
  
  private static final Logger log = Logger.getLogger(MessageSender.class);

  @PostConstruct
  public void test()  {
    try {
      log.info("----------STARTING INIT-------------");
      Connection connection = connectionFactory.createConnection();
      Session session = connection.createSession(Session.AUTO_ACKNOWLEDGE);
      MessageProducer messageProducer = session.createProducer(queue);
      ObjectMessage message = session.createObjectMessage();
      MessageEntity entity = new MessageEntity();
      entity.setText("test message");
      message.setObject(entity);
      messageProducer.send(message);
      messageProducer.close();
      connection.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }

  public static class MessageEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String text;

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }
  }
}
