package org.retal.logiweb.dao;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.MethodUndefinedException;
import org.springframework.stereotype.Component;

@Component
public class CityDAO implements DAO<City> {

  private static final Logger log = Logger.getLogger(CityDAO.class);
  
  @Override
  public void add(City city) {
    log.info("Attempt to add city '" + city.getCurrentCity() + "'");
    Session session = DAO.start();
    session.saveOrUpdate(city);
    session.flush();
    DAO.end(session);
  }

  @Override
  public City read(Object... keys) {
    DAO.validatePrimaryKeys(new Class<?>[] {String.class}, keys);
    String cityName = (String) keys[0];
    Session session = DAO.start();
    City city = session.get(City.class, cityName);
    DAO.end(session);
    String text = city != null ? "'" + city.getCurrentCity() + "'" : "not";
    log.info("City " + text + " found");
    return city;
  }

  @Override
  public List<City> readAll() {

    Session session = DAO.start();
    List<City> cities =
        session.createNativeQuery("SELECT * FROM map_country_cities", City.class).getResultList();
    DAO.end(session);
    log.info("Retrieved " + cities.size() + " cities");
    return cities;
  }

  @Override
  public void delete(City t) {
    throw new MethodUndefinedException();
  }

  @Override
  public void update(City a) {
    throw new MethodUndefinedException();
  }
}
