package org.retal.table.ejb.jsf;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Named;

@Named("ordersStorage")
@Singleton
@Startup
public class OrdersStorage {

  private String message = "Orders Storage Message";
  
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
