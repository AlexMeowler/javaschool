package org.retal.logiweb.dao;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.logiweb.domain.entity.MethodUndefinedException;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.RoutePoint;
import org.springframework.stereotype.Component;

/**
 * Since {@link RoutePoint} class is in Many-To-One relationship with {@link Order}, it is required
 * to create {@link Session} and call {@link #setSession(Session)} before working with this DAO.
 * After session is closed, {@link #setSession(Session)} with {@code null} argument should be
 * called.
 */
@Component
public class RoutePointDAO implements DAO<RoutePoint> {

  private Session session;

  private static final Logger log = Logger.getLogger(RoutePointDAO.class);
  
  @Override
  public void add(RoutePoint t) {
    if (session == null) {
      log.error("Session is null");
      throw new NullPointerException("Session is null");
    }
    log.info("Attempt to save " + t.toString());
    session.save(t);
  }

  @Override
  public RoutePoint read(Object... keys) {
    throw new MethodUndefinedException();
  }

  @Override
  public void delete(RoutePoint t) {
    throw new MethodUndefinedException();
  }

  @Override
  public void update(RoutePoint t) {
    throw new MethodUndefinedException();
  }

  public void setSession(Session session) {
    this.session = session;
  }
}
