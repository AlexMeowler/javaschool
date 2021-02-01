package org.retal.logiweb.dto;

import org.retal.logiweb.domain.entity.User;

/**
 * Wrapper for {@linkplain org.retal.logiweb.domain.entity.User User}. Used in validation process.
 * 
 * @author Alexander Retivov
 *
 */
public class UserWrapper {
  public UserWrapper(User user, String password) {
    this.user = user;
    this.password = password;
  }

  public User getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  private User user;
  private String password;
}