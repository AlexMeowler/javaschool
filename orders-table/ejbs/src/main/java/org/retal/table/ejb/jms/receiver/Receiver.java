package org.retal.table.ejb.jms.receiver;

public interface Receiver {

  public boolean messageBodySupported(Class<?> messageClass);

}
