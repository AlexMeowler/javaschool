package org.retal.logiweb.dao;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.logiweb.domain.entity.CityDistance;
import org.retal.logiweb.domain.entity.MethodUndefinedException;
import org.springframework.stereotype.Component;

@Component
public class CityDistanceDAO implements DAO<CityDistance> {

  private static final Logger log = Logger.getLogger(CityDistanceDAO.class);

  @Override
  public void add(CityDistance t) {
    log.info("Attempt to add distance between cities '" + t.getCityA() + "' and '" + t.getCityB()
        + "', distance = " + t.getDistance());
    Session session = DAO.start();
    session.saveOrUpdate(t);
    session.flush();
    DAO.end(session);
  }

  @Override
  public CityDistance read(Object... keys) {
    DAO.validatePrimaryKeys(new Class<?>[] {String.class, String.class}, keys);
    CityDistance.CityDistancePK primaryKey =
        new CityDistance.CityDistancePK((String) keys[0], (String) keys[1]);
    Session session = DAO.start();
    CityDistance cityDistance = session.get(CityDistance.class, primaryKey);
    DAO.end(session);
    return cityDistance;
  }

  @Override
  public List<CityDistance> readAll() {
    Session session = DAO.start();
    List<CityDistance> distances =
        session.createNativeQuery("SELECT * FROM map_country_distance", CityDistance.class)
            .getResultList();
    log.info("retrieved " + distances.size() + " distances");
    DAO.end(session);
    return distances;
  }

  @Override
  public void delete(CityDistance t) {
    throw new MethodUndefinedException();
  }

  @Override
  public void update(CityDistance t) {
    throw new MethodUndefinedException();
  }
}
