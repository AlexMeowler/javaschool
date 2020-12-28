package org.retal.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.domain.City;
import org.retal.domain.User;
import org.retal.service.HibernateSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class CityDAO implements DAO<City> {

	@Override
	public void add(City city) {
		log.info("Attempt to add city '" + city.getCity() + "'");
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.saveOrUpdate(city);
		session.flush();
		transaction.commit();
		session.close();
	}

	@Override
	public City read(Object... keys) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	@Transactional
	public List<City> readAll() {

		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		List<City> cities = session.createNativeQuery("SELECT * FROM map_country_cities", City.class).getResultList();
		session.close();
		return cities;
	}

	@Override
	public City find(String... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(City t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(City a) {
		// TODO Auto-generated method stub
		
	}
	
	private static final Logger log = Logger.getLogger(CityDAO.class);
}
