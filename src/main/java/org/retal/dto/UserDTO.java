package org.retal.dto;

import org.retal.domain.User;

/**
 * DTO for entity {@linkplain org.retal.domain.User User}.
 * @author Alexander Retivov
 *
 */
public class UserDTO extends User {

  private String password;

  @Override
  public void setPassword(String password) {
    this.password = password;
  }
  
  @Override
  public String getPassword() {
    return password;
  }
}
