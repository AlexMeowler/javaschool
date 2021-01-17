package org.retal.domain.enums;

/**
 * Enumeration for representing cargo status.
 * 
 * @author Alexander Retivov
 *
 */
public enum UserRole {
  // roles should be places in privileges ascension. For example:
  // DRIVER, MANAGER, ADMIN -> driver has less authority than manager and manager has less authority
  // than administrator
  DRIVER, MANAGER, ADMIN;
}
