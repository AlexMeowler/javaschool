package org.retal.logiweb.domain.entity;

/**
 * An exception which shows that invoked method has no realization. It is recommended to throw this
 * exception instead of leaving methods empty to avoid bugs.
 * 
 * @author Alexander Retivov
 *
 */
public class MethodUndefinedException extends RuntimeException {

  public MethodUndefinedException() {
    super("Method undefined");
  }

  private static final long serialVersionUID = 1L;
}
