package org.retal.dao;

import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.domain.Cargo;
import org.retal.domain.MethodUndefinedException;
import org.springframework.stereotype.Component;

@Component
public class CargoDAO implements DAO<Cargo> {

  private static final Logger log = Logger.getLogger(CargoDAO.class);

  @Override
  public void add(Cargo cargo) {
    log.info(
        "Attempt to add cargo: name='" + cargo.getName() + "', mass='" + cargo.getMass() + "'");
    Session session = DAO.start();
    session.save(cargo);
    session.flush();
    DAO.end(session);
  }

  @Override
  public Cargo read(Object... keys) {
    DAO.validatePrimaryKeys(new Class<?>[] {Integer.class}, keys);
    Integer id = (Integer) keys[0];
    Session session = DAO.start();
    Cargo cargo = session.get(Cargo.class, id);
    log.debug(cargo != null ? cargo.toString() : "Cargo not found");
    DAO.end(session);
    return cargo;
  }

  @Override
  public List<Cargo> readAll() {
    Session session = DAO.start();
    List<Cargo> cargos =
        session.createNativeQuery("SELECT * FROM CARGO", Cargo.class).getResultList();
    log.info(cargos.size() + " cargos retrieved");
    DAO.end(session);
    return cargos;
  }

  @Override
  public void delete(Cargo t) {
    throw new MethodUndefinedException();
  }

  @Override
  public void update(Cargo t) {
    Session session = DAO.start();
    session.update(t);
    session.flush();
    DAO.end(session);
  }

}
