package org.retal.logiweb.dao.impl;

import org.hibernate.Session;
import org.retal.logiweb.dao.interfaces.DAO;
import org.retal.logiweb.domain.entity.CompletedOrderInfo;
import org.retal.logiweb.domain.entity.MethodUndefinedException;
import org.springframework.stereotype.Component;

@Component
public class CompletedOrderInfoDAO implements DAO<CompletedOrderInfo> {

  @Override
  public void add(CompletedOrderInfo t) {
    Session session = DAO.start();
    session.save(t);
    session.flush();
    DAO.end(session);
  }

  @Override
  public CompletedOrderInfo read(Object... keys) {
    DAO.validatePrimaryKeys(new Class<?>[] {Integer.class}, keys);
    Integer id = (Integer) keys[0];
    Session session = DAO.start();
    CompletedOrderInfo completedOrderInfo = session.get(CompletedOrderInfo.class, id);
    DAO.end(session);
    return completedOrderInfo;
  }

  @Override
  public void delete(CompletedOrderInfo t) {
    throw new MethodUndefinedException();
  }

  @Override
  public void update(CompletedOrderInfo t) {
    throw new MethodUndefinedException();
  }

}
