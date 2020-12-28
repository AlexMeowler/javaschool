package org.retal.service;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.retal.domain.Car;
import org.retal.domain.Cargo;
import org.retal.domain.City;
import org.retal.domain.RoutePoint;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.domain.Order;

public class HibernateSessionFactory {
	private HibernateSessionFactory() {

	}

	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			Configuration config = new Configuration();
			Properties properties = new Properties();
			try {
				properties.load(HibernateSessionFactory.class.getResourceAsStream("/db.properties"));
			} catch (IOException e) {
				log.info("Hibernate config file not found");
			}
			config.configure().addProperties(properties);
			config.addAnnotatedClass(User.class).addAnnotatedClass(UserInfo.class).addAnnotatedClass(Car.class)
			.addAnnotatedClass(City.class).addAnnotatedClass(Cargo.class).addAnnotatedClass(RoutePoint.class).addAnnotatedClass(Order.class);
			ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(config.getProperties())
					.build();
			sessionFactory = config.buildSessionFactory(serviceRegistry);
		}
		return sessionFactory;
	}

	private static final Logger log = Logger.getLogger(HibernateSessionFactory.class);
	private static SessionFactory sessionFactory = null;
}
