package org.retal.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.domain.Cargo;
import org.retal.service.HibernateSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class CargoDAO implements DAO<Cargo> {

	@Override
	public void add(Cargo cargo) {
		log.info("Attempt to add cargo: name='" + cargo.getName() + "', mass='" + cargo.getMass() + "'");
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.save(cargo);
		session.flush();
		transaction.commit();
		session.close();
	}

	@Override
	public Cargo read(Object... keys) {
		// TODO key validation
		Integer id = (Integer)keys[0];
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Cargo cargo = session.get(Cargo.class, id);
		session.close();
		// TODO logging
		return cargo;
	}
	
	@Override
	public List<Cargo> readAll() {
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		List<Cargo> cargos = session.createNativeQuery("SELECT * FROM CARGO", Cargo.class).getResultList();
		log.info(cargos.size() + " cargos retrieved");
		session.close();
		return cargos;
	}

	@Override
	public void delete(Cargo t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Cargo t) {
		// TODO Auto-generated method stub
		
	}
	
	private static final Logger log = Logger.getLogger(CargoDAO.class);
}
