package org.retal.logiweb.dao;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.logiweb.domain.entity.Car;
import org.springframework.stereotype.Component;

@Component
public class CarDAO implements DAO<Car> {

  private static final Logger log = Logger.getLogger(CarDAO.class);

  @Override
  public void add(Car car) {
    log.info("Attempt to add new car");
    Session session = DAO.start();
    session.save(car);
    session.flush();
    DAO.end(session);
  }

  @Override
  public Car read(Object... keys) {
    DAO.validatePrimaryKeys(new Class<?>[] {String.class}, keys);
    String id = (String) keys[0];
    Session session = DAO.start();
    Car car = session.get(Car.class, id);
    DAO.end(session);
    return car;
  }

  @Override
  public List<Car> readAll() {
    Session session = DAO.start();
    List<Car> cars = session.createNativeQuery("SELECT * FROM CARS", Car.class).getResultList();
    log.info(cars.size() + " cars retrieved");
    DAO.end(session);
    return cars;
  }

  @Override
  public void delete(Car car) {
    Session session = DAO.start();
    session.delete(car);
    session.flush();
    DAO.end(session);
    log.info(car.toString() + " deleted");
  }

  @Override
  public void update(Car car) {
    log.info("Updating car");
    Session session = DAO.start();
    session.update(car);
    session.flush();
    DAO.end(session);
  }

}
