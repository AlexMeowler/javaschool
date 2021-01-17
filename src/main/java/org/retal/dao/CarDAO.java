package org.retal.dao;

import java.util.List;
import javax.transaction.Transactional;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.domain.Car;
import org.retal.domain.HibernateSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class CarDAO implements DAO<Car> {

  private static final Logger log = Logger.getLogger(CarDAO.class);

  @Override
  public void add(Car car) {
    log.info("Attempt to add new car");
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();
    session.save(car);
    session.flush();
    transaction.commit();
    session.close();
  }

  @Override
  public Car read(Object... keys) throws IllegalArgumentException {
    validatePrimaryKeys(new Class<?>[] {String.class}, keys);
    String id = (String) keys[0];
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();
    Car car = session.get(Car.class, id);
    //session.flush();
    transaction.commit();
    session.close();
    return car;
  }

  @Override
  @Transactional
  public List<Car> readAll() {
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    List<Car> cars = session.createNativeQuery("SELECT * FROM CARS", Car.class).getResultList();
    log.info(cars.size() + " cars retrieved");
    session.close();
    return cars;
  }

  @Override
  public void delete(Car car) {
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();
    session.delete(car);
    session.flush();
    transaction.commit();
    session.close();
    log.info(car.toString() + " deleted");
  }

  @Override
  public void update(Car car) {
    log.info("Updating car");
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();
    session.update(car);
    session.flush();
    transaction.commit();
    session.close();
  }

}
