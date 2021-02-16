package org.retal.logiweb.dao.impl;

import java.math.BigInteger;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.logiweb.dao.interfaces.CountableRows;
import org.retal.logiweb.dao.interfaces.DAO;
import org.retal.logiweb.dao.interfaces.PartRowsReader;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.service.validators.UserValidator;
import org.springframework.stereotype.Component;

@Component
public class UserDAO implements DAO<User>, CountableRows, PartRowsReader<User> {

  private static final Logger log = Logger.getLogger(UserDAO.class);

  private static final String TABLE_NAME = "users";

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
    String query = String.format("SELECT * FROM %s WHERE login= '%s'", TABLE_NAME, username);
    List<User> users = session.createNativeQuery(query, User.class).getResultList();
    DAO.end(session);
    return users.size() == 1 ? users.get(0) : null;
  }

  @Override
  public List<User> readAll() {
    Session session = DAO.start();
    List<User> users =
        session.createNativeQuery("SELECT * FROM " + TABLE_NAME, User.class).getResultList();
    DAO.end(session);
    log.info(users.size() + " users retrieved");
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
        session.createNativeQuery("SELECT * FROM " + TABLE_NAME + " WHERE role = '" + role + "'",
            User.class).getResultList();
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

  /**
   * Reads part of table and returns it as list. Only drivers are accounted.
   * 
   * @param from number of row from which reading is started (the row itself is excluded)
   * @param amount amount of rows to be read from database
   * @return list of read rows
   */
  @Override
  public List<User> readRows(int from, int amount) {
    Session session = DAO.start();
    List<User> users =
        session.createNativeQuery(String.format("SELECT * FROM %s WHERE role = '%s' LIMIT %d, %d",
            TABLE_NAME, "driver", from, amount), User.class).getResultList();
    log.info(users.size() + " users retrieved");
    DAO.end(session);
    return users;
  }

  /**
   * Returns the amount of rows in table. Only drivers are accounted.
   * 
   * @return the amount of rows
   */
  @Override
  public int getRowsAmount() {
    Session session = DAO.start();
    BigInteger amount = (BigInteger) session
        .createNativeQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE role = 'driver'")
        .getSingleResult();
    DAO.end(session);
    return amount.intValue();
  }
}
