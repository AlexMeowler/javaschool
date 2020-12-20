package org.retal.service;

import java.io.IOException;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.retal.domain.User;
import org.retal.domain.UserInfo;

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
			Properties properties = new Properties();
			try 
			{
				properties.load(HibernateSessionFactory.class.getResourceAsStream("/db.properties"));
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			config.configure().addProperties(properties);
			config.addAnnotatedClass(User.class);
			config.addAnnotatedClass(UserInfo.class);
			ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(config.getProperties()).build();
			sessionFactory = config.buildSessionFactory(serviceRegistry);
		}
		return sessionFactory;
	}
	
	private static SessionFactory sessionFactory = null;
}
