package org.retal.logiweb.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.logiweb.domain.entity.Cargo;
import org.retal.logiweb.domain.entity.MethodUndefinedException;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.RoutePoint;
import org.springframework.stereotype.Component;

/**
 * Since {@link Order} class is in One-To-Many relationship with {@link RoutePoint}, it is required
 * to create {@link Session} and call {@link #setSession(Session)} before working with this DAO
 * (except {@link #read(Object...)} and {@link #readAll()} methods). After session is closed,
 * {@link #setSession(Session)} with {@code null} argument should be called.
 */
@Component
public class OrderDAO implements DAO<Order> {

  private Session session;

  private static final Logger log = Logger.getLogger(OrderDAO.class);

  @Override
  public void add(Order t) {
    if (session == null) {
      throw new NullPointerException("Session is null");
    }
    log.info("Attempt to add order " + t.toString());
    session.save(t);
  }

  @Override
  public Order read(Object... keys) {
    DAO.validatePrimaryKeys(new Class<?>[] {Integer.class}, keys);
    Integer id = (Integer) keys[0];
    Session session = DAO.start();
    Order order = session.get(Order.class, id);
    DAO.end(session);
    if (order != null) {
      Set<Cargo> cargo = new HashSet<>();
      for (RoutePoint rp : order.getPoints()) {
        cargo.add(rp.getCargo());
      }
      order.setCargo(cargo);
      return order;
    } else {
      return null;
    }
  }

  @Override
  public List<Order> readAll() {
    Session session = DAO.start();
    List<Order> orders =
        session.createNativeQuery("SELECT * FROM ORDERS", Order.class).getResultList();
    log.info(orders.size() + " orders retrieved");
    for (Order o : orders) {
      Set<Cargo> cargo = new HashSet<>();
      for (RoutePoint rp : o.getPoints()) {
        cargo.add(rp.getCargo());
      }
      o.setCargo(cargo);
    }
    DAO.end(session);
    return orders;
  }

  @Override
  public void delete(Order t) {
    throw new MethodUndefinedException();
  }

  @Override
  public void update(Order t) {
    if (session == null) {
      throw new NullPointerException("Session is null");
    }
    log.info("Updating order " + t.toString());
    session.update(t);
  }

  public void setSession(Session session) {
    this.session = session;
  }
}
