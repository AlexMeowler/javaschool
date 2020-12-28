package org.retal.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.domain.Cargo;
import org.retal.service.HibernateSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class CargoDAO implements DAO<Cargo> {

	@Override
	public void add(Cargo t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Cargo read(Object... keys) {
		// TODO Auto-generated method stub
		return null;
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
	public Cargo find(String... args) {
		// TODO Auto-generated method stub
		return null;
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
