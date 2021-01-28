package org.retal.orders_table.ejb.jms;

import javax.annotation.Resource;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven
public class OrderMessage implements MessageListener {
  
  @Resource
  private MessageDrivenContext context;

  @Override
  public void onMessage(Message message) {
    // TODO Auto-generated method stub
    
  }

}
