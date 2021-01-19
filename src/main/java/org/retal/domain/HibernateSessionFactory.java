package org.retal.domain;

import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 * Class, responsible for configuring Hibernate and providing
 * {@linkplain org.hibernate.SessionFactory SessionFactory} instance.
 * 
 * @author Alexander Retivov
 *
 */
public class HibernateSessionFactory {

  private static final Logger log = Logger.getLogger(HibernateSessionFactory.class);
  private static SessionFactory sessionFactory = null;

  private HibernateSessionFactory() {

  }

  /**
   * Creates {@linkplain org.hibernate.SessionFactory SessionFactory} instance, if it hasn't been
   * created, configures and returns it. If instance has already been created, then it is returned.
   * 
   * @return {@linkplain org.hibernate.SessionFactory SessionFactory} singleton instance
   */
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
      config.addAnnotatedClass(User.class).addAnnotatedClass(UserInfo.class)
          .addAnnotatedClass(Car.class).addAnnotatedClass(City.class).addAnnotatedClass(Cargo.class)
          .addAnnotatedClass(RoutePoint.class).addAnnotatedClass(Order.class)
          .addAnnotatedClass(CityDistance.class).addAnnotatedClass(OrderRouteProgression.class);
      ServiceRegistry serviceRegistry =
          new StandardServiceRegistryBuilder().applySettings(config.getProperties()).build();
      sessionFactory = config.buildSessionFactory(serviceRegistry);
    }
    return sessionFactory;
  }
}
