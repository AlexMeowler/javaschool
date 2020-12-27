package org.retal.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.domain.Car;
import org.retal.domain.User;
import org.retal.service.HibernateSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class CarDAO implements DAO<Car> {

	@Override
	public void add(Car car) {
		log.info("Attempt to add new car");
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.save(car);
		session.flush();
		transaction.commit();
		session.close();
	}

	@Override
	public Car read(Object... keys) {
		//check for PK
		String id = (String)keys[0];
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		Car car = session.get(Car.class, id);
		session.flush();
		transaction.commit();
		session.close();
		//logging
		return car;
	}

	@Override
	@Transactional
	public List<Car> readAll() {

		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		List<Car> cars = session.createNativeQuery("SELECT * FROM CARS", Car.class).getResultList();
		/*
		 * for(Car c : cars) { log.info(c.getShiftlength() != null);
		 * log.info(c.getCapacitytonns() != null); }
		 */
		session.close();
		return cars;
	}

	@Override
	@Transactional
	public Car find(String... args) // check arguments
	{
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		String query = String.format("SELECT * FROM CARS WHERE registration_id= '%s'", args[0]);
		List<Car> cars = session.createNativeQuery(query, Car.class).getResultList();
		session.close();
		return cars.size() == 1 ? cars.get(0) : null;
	}

	@Override
	public void delete(Car car) {
		// logging
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.delete(car);
		session.flush();
		transaction.commit();
		session.close();
	}

	@Override
	public void deleteById(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Car car) {
		log.info("Finishing editing car");
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.update(car);
		session.flush();
		transaction.commit();
		session.close();
	}

	private static final Logger log = Logger.getLogger(CarDAO.class);

}
