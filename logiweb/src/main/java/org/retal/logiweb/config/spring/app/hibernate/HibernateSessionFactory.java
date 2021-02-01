package org.retal.logiweb.config.spring.app.hibernate;

import java.util.Properties;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.retal.logiweb.domain.Car;
import org.retal.logiweb.domain.Cargo;
import org.retal.logiweb.domain.City;
import org.retal.logiweb.domain.CityDistance;
import org.retal.logiweb.domain.Order;
import org.retal.logiweb.domain.OrderRouteProgression;
import org.retal.logiweb.domain.RoutePoint;
import org.retal.logiweb.domain.User;
import org.retal.logiweb.domain.UserInfo;

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
      } catch (Exception e) {
        log.error("Hibernate config file not found");
        log.error(e, e);
        return null;
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
