package org.retal.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.domain.User;
import org.retal.service.HibernateSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class UserDAO implements DAO<User> {
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
		// TODO add checking for keys
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Integer id = (Integer)keys[0];
		User user = session.get(User.class, id);
		if(user != null) {
			String info = user.getUserInfo() != null ? user.getUserInfo().toString() : "null";
			log.debug("for user id='" + user.getId() + "' user info = " + info);
		} else {
			log.debug("user not found");
		}
		session.close();
		return user;
	}

	@Transactional
	public User findUser(String username) // check arguments
	{
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

	@Transactional
	public List<User> readAllWithRole(String role) {
		role = role.toLowerCase();
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		List<User> users = session.createNativeQuery("SELECT * FROM USERS WHERE role = '" + role + "'", User.class)
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
		log.info("Attempt to delete user: " + user.toString() + "'");
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.delete(user);
		session.flush();
		transaction.commit();
		session.close();
	}

	private static final Logger log = Logger.getLogger(UserDAO.class);
}
