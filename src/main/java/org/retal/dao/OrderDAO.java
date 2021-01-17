package org.retal.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.domain.Cargo;
import org.retal.domain.HibernateSessionFactory;
import org.retal.domain.Order;
import org.retal.domain.RoutePoint;
import org.springframework.stereotype.Component;

/**
 * Since {@link Order} class is in One-To-Many relationship with {@link RoutePoint}, it is required
 * to create {@link Session} and call {@link #setSession(Session)} before working with this DAO.
 * After session is closed, {@link #setSession(Session)} with {@code null} argument should be
 * called.
 */
@Component
public class OrderDAO implements DAO<Order> {
  
  private Session session;

  private static final Logger log = Logger.getLogger(OrderDAO.class);
  
  @Override
  public void add(Order t) {
    if (session == null) {
      log.error("Session is null");
      return;
    }
    log.info("Attempt to add order"); // TODO with ToString
    session.save(t);
  }

  @Override
  public Order read(Object... keys) {
    validatePrimaryKeys(new Class<?>[] {Integer.class}, keys);
    Integer id = (Integer) keys[0];
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    List<Order> orders =
        session.createNativeQuery("SELECT * FROM ORDERS WHERE id='" + id + "'", Order.class)
            .getResultList();
    session.close();
    if (orders.size() == 1) {
      Order o = orders.get(0);
      Set<Cargo> cargo = new HashSet<>();
      for (RoutePoint rp : o.getPoints()) {
        cargo.add(rp.getCargo());
      }
      o.setCargo(cargo);
      return o;
    } else {
      return null;
    }

  }

  @Override
  public List<Order> readAll() {
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
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
    session.close();
    return orders;
  }

  @Override
  public void delete(Order t) {
    // TODO Auto-generated method stub

  }

  @Override
  public void update(Order t) {
    if (session == null) {
      log.error("Session is null");
      return;
    }
    log.info("Updating order"); // TODO with ToString
    session.update(t);

  }

  public void setSession(Session session) {
    this.session = session;
  }
}
