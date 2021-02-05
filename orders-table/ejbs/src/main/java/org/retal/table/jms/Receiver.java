package org.retal.table.jms;

public interface Receiver {

  public boolean messageBodySupported(Class<?> messageClass);

}
