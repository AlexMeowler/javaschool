package org.retal.dao;

import org.hibernate.Session;
import org.retal.model.User;
import org.retal.service.HibernateSessionFactory;

public class UserDAO 
{
	public void save(User user)
	{
		
		
	}
	
	public User read(int id)
	{
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		session.beginTransaction();
		User user = session.get(User.class, id);
		session.flush();
		session.close();
		return user;
	}
	
	public void modify()
	{
		
	}
	
	public void delete()
	{
		
	}
}
