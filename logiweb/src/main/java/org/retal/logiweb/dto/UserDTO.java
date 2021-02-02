package org.retal.logiweb.dto;

import org.retal.logiweb.domain.entity.User;

/**
 * DTO for entity {@linkplain org.retal.logiweb.domain.entity.User User}.
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
