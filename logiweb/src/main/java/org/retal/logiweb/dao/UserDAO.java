package org.retal.logiweb.dao;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.logiweb.domain.User;
import org.retal.logiweb.service.UserValidator;
import org.springframework.stereotype.Component;

@Component
public class UserDAO implements DAO<User> {

  private static final Logger log = Logger.getLogger(UserDAO.class);

  @Override
  public void add(User user) {
    log.info("Attempt to add user: name='" + user.getLogin() + "', role='" + user.getRole() + "'");
    Session session = DAO.start();
    session.save(user);
    session.flush();
    DAO.end(session);
  }

  @Override
  public User read(Object... keys) {
    DAO.validatePrimaryKeys(new Class<?>[] {Integer.class}, keys);
    Integer id = (Integer) keys[0];
    Session session = DAO.start();
    User user = session.get(User.class, id);
    if (user != null) {
      log.debug("for user id='" + user.getId() + "' user info = " + user.getUserInfo().toString());
    } else {
      log.debug("user not found");
    }
    DAO.end(session);
    return user;
  }

  /**
   * Method for finding users by their login ({@link UserDAO#read(Object...)} method won't work
   * because login is not primary key).
   * 
   * @param username user login
   * @return found user if there is one and only one user with that login, null otherwise
   */
  public User findUser(String username) {
    if (!UserValidator.checkForMaliciousInput(username).isEmpty()) {
      log.warn("Attempt to inject malicious input");
      return null;
    }
    Session session = DAO.start();
    String query = String.format("SELECT * FROM USERS WHERE login= '%s'", username);
    List<User> users = session.createNativeQuery(query, User.class).getResultList();
    DAO.end(session);
    return users.size() == 1 ? users.get(0) : null;
  }

  @Override
  public List<User> readAll() {
    Session session = DAO.start();
    List<User> users = session.createNativeQuery("SELECT * FROM USERS", User.class).getResultList();
    DAO.end(session);
    return users;
  }

  /**
   * Method for fetching all users with given role.
   * 
   * @param role user role in lower case
   * @return List of user entities with matching role
   */
  public List<User> readAllWithRole(String role) {
    role = role.toLowerCase();
    if (!UserValidator.checkForMaliciousInput(role).isEmpty()) {
      log.warn("Attempt to inject malicious input");
      return null;
    }
    Session session = DAO.start();
    List<User> users =
        session.createNativeQuery("SELECT * FROM USERS WHERE role = '" + role + "'", User.class)
            .getResultList();
    log.debug("Retrieved " + users.size() + " users with role '" + role + "'");
    DAO.end(session);
    return users;
  }

  @Override
  public void update(User newUser) {
    log.info("Finishing editing user id = " + newUser.getId());
    Session session = DAO.start();
    session.update(newUser);
    session.flush();
    DAO.end(session);
  }

  @Override
  public void delete(User user) {
    log.info("Attempt to delete user: " + user.toString());
    Session session = DAO.start();
    session.delete(user);
    session.flush();
    DAO.end(session);
  }
}
