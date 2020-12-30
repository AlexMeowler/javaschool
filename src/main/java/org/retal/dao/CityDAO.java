package org.retal.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.domain.City;
import org.retal.service.HibernateSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class CityDAO implements DAO<City> {

	@Override
	public void add(City city) {
		log.info("Attempt to add city '" + city.getCurrentCity() + "'");
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.saveOrUpdate(city);
		session.flush();
		transaction.commit();
		session.close();
	}

	@Override
	public City read(Object... keys) {
		// TODO key validation
		String cityName = (String)keys[0];
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		City city = session.get(City.class, cityName);
		session.close();
		String text = city != null ? "'" + city.getCurrentCity() + "'" : "not";
		log.info("City " + text + " found");
		return city;
	}
	
	@Override
	@Transactional
	public List<City> readAll() {

		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		List<City> cities = session.createNativeQuery("SELECT * FROM map_country_cities", City.class).getResultList();
		session.close();
		log.info("Retrieved " + cities.size() + " cities");
		return cities;
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
