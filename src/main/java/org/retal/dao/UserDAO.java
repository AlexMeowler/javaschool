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
public class UserDAO implements DAO<User>
{
	@Override
	public void add(User user)
	{
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
	public User read(int id)
	{
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		User user = session.get(User.class, id);
		String info = user.getUserInfo() != null ? user.getUserInfo().toString() : "null";
		log.info("for user id='" + user.getId() + "' user info = " + info);
		session.close();
		return user;
	}
	
	@Override
	@Transactional
	public User find(String... args) //check arguments
	{
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		String query = String.format("SELECT * FROM USERS WHERE login= '%s'", args[0]);
		List<User> users = session.createNativeQuery(query, User.class).getResultList();
		return users.size() == 1 ? users.get(0) : null;
	}
	
	@Override
	@Transactional
	public List<User> readAll()
	{
		
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		List<User> users = session.createNativeQuery("SELECT * FROM USERS", User.class).getResultList();
		for(User u : users)
		{
			String info = u.getUserInfo() != null ? u.getUserInfo().toString() : "null";
			log.info("for user id='" + u.getId() + "' user info = " + info);
		}
		session.close();
		return users;
	}
	
	@Transactional
	public List<User> readAllWithRole(String role)
	{
		role = role.toLowerCase();
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		List<User> users = session.createNativeQuery("SELECT * FROM USERS WHERE role = '" + role + "'", User.class).getResultList();
		for(User u : users)
		{
			log.info(role + " found: id='" + u.getId() + "'");
		}
		session.close();
		return users;
	}
	
	@Override
	public void update(User newUser)
	{
		log.info("Editing user");
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		//User oldUser = session.find(User.class, newUser.getId()); //add exception
		Transaction transaction = session.beginTransaction();
		//session.evict(oldUser);
		session.update(newUser);
		/*session.evict(oldUser);
		oldUser.setLogin(newUser.getLogin());
		oldUser.setPassword(newUser.getPassword());
		oldUser.setRole(newUser.getRole());
		oldUser.setUserInfo(newUser.getUserInfo());
		oldUser.getUserInfo().setUser(oldUser);
		session.persist(oldUser.getUserInfo());
		session.detach(oldUser.getUserInfo());
		session.update(oldUser);
		oldUser = (User) session.merge(oldUser);*/
		session.flush();
		transaction.commit();
		session.close();
	}
	
	@Override
	public void delete(User user)
	{
		log.info("Attempt to delete user: " + user.toString() + "'");
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.delete(user);
		session.flush();
		transaction.commit();
		session.close();
	}
	
	@Override
	public void deleteById(int id)
	{
		log.info("Attempt to delete user: id='" + id + "'");
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		User user = session.load(User.class, id);
		log.info("found user: " + user.toString());
		session.delete(user);
		session.flush();
		transaction.commit();
		session.close();
	}
	
	private static final Logger log = Logger.getLogger(UserDAO.class);
}
