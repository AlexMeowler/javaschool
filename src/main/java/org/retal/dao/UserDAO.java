package org.retal.dao;

import java.util.List;
import javax.transaction.Transactional;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.domain.HibernateSessionFactory;
import org.retal.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserDAO implements DAO<User> {

  private static final Logger log = Logger.getLogger(UserDAO.class);

  @Override
  public void add(User user) {
    log.info("Attempt to add user: name='" + user.getLogin() + "', role='" + user.getRole() + "'");
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();
    session.save(user);
    session.flush();
    transaction.commit();
    session.close();
  }

  @Override
  @Transactional
  public User read(Object... keys) {
    validatePrimaryKeys(new Class<?>[] {Integer.class}, keys);
    Integer id = (Integer) keys[0];
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    User user = session.get(User.class, id);
    if (user != null) {
      log.debug("for user id='" + user.getId() + "' user info = " + user.getUserInfo().toString());
    } else {
      log.debug("user not found");
    }
    session.close();
    return user;
  }

  /**
   * Method for finding users by their login ({@link UserDAO#read(Object...)} method won't work
   * because login is not primary key).
   * 
   * @param username user login
   * @return found user if there is one and only one user with that login, null otherwise
   */
  @Transactional
  public User findUser(String username) {
    // FIXME check arguments
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    String query = String.format("SELECT * FROM USERS WHERE login= '%s'", username);
    List<User> users = session.createNativeQuery(query, User.class).getResultList();
    session.close();
    return users.size() == 1 ? users.get(0) : null;
  }

  @Override
  @Transactional
  public List<User> readAll() {
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    List<User> users = session.createNativeQuery("SELECT * FROM USERS", User.class).getResultList();
    session.close();
    return users;
  }

  /**
   * Method for fetching all users with given role.
   * 
   * @param role user role in lower case
   * @return List of user entities with matching role
   */
  @Transactional
  public List<User> readAllWithRole(String role) {
    role = role.toLowerCase();
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    List<User> users =
        session.createNativeQuery("SELECT * FROM USERS WHERE role = '" + role + "'", User.class)
            .getResultList();

    log.debug("Retrieved " + users.size() + " users with role '" + role + "'");
    session.close();
    return users;
  }

  @Override
  public void update(User newUser) {
    log.info("Finishing editing user");
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();
    session.update(newUser);
    session.flush();
    transaction.commit();
    session.close();
  }

  @Override
  public void delete(User user) {
    log.info("Attempt to delete user: " + user.toString());
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();
    session.delete(user);
    session.flush();
    transaction.commit();
    session.close();
  }
}
