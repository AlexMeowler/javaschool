package org.retal.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.domain.Car;
import org.retal.service.HibernateSessionFactory;
import org.springframework.stereotype.Component;

@Component
public class CarDAO implements DAO<Car> 
{

	@Override
	public void add(Car t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Car read(int primaryKey) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	@Transactional
	public List<Car> readAll()
	{
		
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		List<Car> cars = session.createNativeQuery("SELECT * FROM CARS", Car.class).getResultList();
		/*for(Car c : cars)
		{
			log.info(c.getShiftlength() != null);
			log.info(c.getCapacitytonns() != null);
		}*/
		session.close();
		return cars;
	}

	@Override
	public Car find(String... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Car t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteById(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Car t, String... args) {
		// TODO Auto-generated method stub
		
	}
	
	private static final Logger log = Logger.getLogger(CarDAO.class);
	
}
