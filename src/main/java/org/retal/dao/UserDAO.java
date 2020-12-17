package org.retal.dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

import javax.transaction.Transactional;

import org.hibernate.Session;
import org.retal.domain.User;
import org.retal.service.HibernateSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class UserDAO implements DAO<User>
{
	@Override
	@Transactional
	public void save(User user)
	{
		
		
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
	@Transactional
	public void delete(User user)
	{
		
	}
}
