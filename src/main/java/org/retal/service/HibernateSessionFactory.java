package org.retal.service;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.retal.model.User;

public class HibernateSessionFactory 
{
	private HibernateSessionFactory()
	{
		
	}
	
	public static SessionFactory getSessionFactory()
	{
		if(sessionFactory == null)
		{
			Configuration config = new Configuration();
			config.configure();
			config.addAnnotatedClass(User.class);
			sessionFactory = config.buildSessionFactory();
		}
		return sessionFactory;
	}
	
	private static SessionFactory sessionFactory = null;
}
