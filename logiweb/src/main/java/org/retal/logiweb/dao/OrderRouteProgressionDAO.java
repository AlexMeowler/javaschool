package org.retal.logiweb.dao;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.logiweb.domain.entity.OrderRouteProgression;
import org.springframework.stereotype.Component;

@Component
public class OrderRouteProgressionDAO implements DAO<OrderRouteProgression> {

  private static final Logger log = Logger.getLogger(OrderRouteProgressionDAO.class);
  
  @Override
  public void add(OrderRouteProgression t) {
    Session session = DAO.start();
    session.save(t);
    session.flush();
    DAO.end(session);
  }

  @Override
  public OrderRouteProgression read(Object... keys) {
    DAO.validatePrimaryKeys(new Class<?>[] {Integer.class}, keys);
    Integer id = (Integer) keys[0];
    Session session = DAO.start();
    OrderRouteProgression orderRouteProgression = session.get(OrderRouteProgression.class, id);
    DAO.end(session);
    return orderRouteProgression;
  }

  @Override
  public void delete(OrderRouteProgression t) {
    Session session = DAO.start();
    session.delete(t);
    session.flush();
    DAO.end(session);
  }

  @Override
  public void update(OrderRouteProgression t) {
    log.debug("Updating route progression for order id = " + t.getId());
    Session session = DAO.start();
    session.update(t);
    session.flush();
    DAO.end(session);
  }

}
