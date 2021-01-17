package org.retal.dto;

import org.retal.domain.User;

/**
 * Wrapper for {@linkplain org.retal.domain.User User}. Used in validation process.
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