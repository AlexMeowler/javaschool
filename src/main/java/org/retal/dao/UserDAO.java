package org.retal.dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
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
	@Transactional
	public void add(User user)
	{
		log.info("Attempt to add user: name='" + user.getLogin() + "', role='" + user.getRole() + "'");
		MessageDigest mg = null;
		try
		{
			mg = MessageDigest.getInstance("sha-512");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		byte[] password = user.getPassword().getBytes(StandardCharsets.UTF_8);
		String hashedPassword = Base64.getEncoder().encodeToString(mg.digest(password));
		user.setPassword(hashedPassword);
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		session.save(user);
		session.close();
	}
	
	@Override
	@Transactional
	public User read(int id)
	{
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		User user = session.get(User.class, id);
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
		session.close();
		return users;
	}
	
	@Override
	@Transactional
	public void update(User user, String... args)
	{
		
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
