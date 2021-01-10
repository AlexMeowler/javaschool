package org.retal.dao;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.domain.CityDistance;
import org.retal.domain.User;
import org.retal.service.HibernateSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class CityDistanceDAO implements DAO<CityDistance> {

	@Override
	public void add(CityDistance t) {
		log.info("Attempt to add distance between cities '" + t.getCityA() + "' and '" + t.getCityB() + "', distance = " + t.getDistance());
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.saveOrUpdate(t);
		session.flush();
		transaction.commit();
		session.close();
		
	}

	@Override
	public CityDistance read(Object... keys) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<CityDistance> readAll() {
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		List<CityDistance> distances = session.createNativeQuery("SELECT * FROM map_country_distance", CityDistance.class).getResultList();
		log.info("retrieved " + distances.size() + " distances");
		session.close();
		return distances;
	}

	@Override
	public void delete(CityDistance t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(CityDistance t) {
		// TODO Auto-generated method stub
		
	}
	
	private static final Logger log = Logger.getLogger(CityDistanceDAO.class);
}
