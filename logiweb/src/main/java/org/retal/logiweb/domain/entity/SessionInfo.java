package org.retal.logiweb.domain.entity;

import org.retal.logiweb.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Class containing {@linkplain org.retal.logiweb.domain.entity.User User} object which refers to
 * currently logged in user.
 * 
 * @author Alexander Retivov
 *
 */
@Component
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionInfo {

  private User user;

  private UserDAO userDAO;

  @Autowired
  public SessionInfo(UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  /**
   * Method for getting {@linkplain org.retal.logiweb.domain.entity.User User} object of logged in
   * user, based on session info.
   * 
   * @return user {@linkplain org.retal.logiweb.domain.entity.User User} who is associated with the
   *         session.
   */
  public User getCurrentUser() {
    if (user == null) {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      user = userDAO.findUser(auth.getName());
    }
    return user;
  }

  /**
   * Updates user information by re-reading entity from database. If no
   * {@linkplain org.retal.logiweb.domain.entity.User User} if associated with this class instance,
   * then nothing will happen.
   */
  public void refreshUser() {
    if (user != null) {
      user = userDAO.read(user.getId());
    }
  }
}
