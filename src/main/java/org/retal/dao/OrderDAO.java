package org.retal.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.domain.Cargo;
import org.retal.domain.User;
import org.retal.domain.Order;
import org.retal.domain.RoutePoint;
import org.retal.domain.enums.UserRole;
import org.retal.service.HibernateSessionFactory;
import org.springframework.stereotype.Component;

/**
 * Since {@link Order} class is in One-To-Many relationship with {@link RoutePoint},
 * it is required to create {@link Session} and call {@link #setSession(Session)} before working with this DAO.
 * After session is closed, {@link #setSession(Session)} with {@code null} argument should be called.
 */
@Component
public class OrderDAO implements DAO<Order> {

	@Override
	public void add(Order t) {
		if(session == null) {
			log.error("Session is null");
			return ;
		}
		log.info("Attempt to add order"); //TODO with ToString
		session.save(t);
	}

	@Override
	public Order read(Object... keys) {
		// TODO Auto-generated method stub
		Integer id = (Integer)keys[0];
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		List<Order> orders = session.createNativeQuery("SELECT * FROM ORDERS WHERE id='" + id + "'", Order.class).getResultList();
		List<User> drivers = session.createNativeQuery("SELECT * FROM USERS WHERE role = '" 
				+ UserRole.DRIVER.toString().toLowerCase() + "'", User.class).getResultList();
		session.close();
		if(orders.size() == 1) {
			Order o = orders.get(0);
			Set<Cargo> cargo = new HashSet<>();
			for(RoutePoint rp : o.getPoints()) {
				cargo.add(rp.getCargo());
			}
			o.setCargo(cargo);
			Set<User> assignedDrivers = new HashSet<>();
			for(User driver : drivers) {
				Order order = driver.getUserInfo().getOrder();
				//TODO equals
				if(order != null && order.getId() == o.getId()) {
					assignedDrivers.add(driver);
				}
			}
			o.setDrivers(assignedDrivers);
			return o;
		} else {
			return null;
		}
		
	}
	
	@Override
	public List<Order> readAll() {
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		List<Order> orders = session.createNativeQuery("SELECT * FROM ORDERS", Order.class).getResultList();
		List<User> drivers = session.createNativeQuery("SELECT * FROM USERS WHERE role = '" 
							+ UserRole.DRIVER.toString().toLowerCase() + "'", User.class).getResultList();
		log.info(orders.size() + " orders retrieved");
		for(Order o : orders) {
			Set<Cargo> cargo = new HashSet<>();
			for(RoutePoint rp : o.getPoints()) {
				cargo.add(rp.getCargo());
			}
			o.setCargo(cargo);
			Set<User> assignedDrivers = new HashSet<>();
			for(User driver : drivers) {
				Order order = driver.getUserInfo().getOrder();
				//TODO equals
				if(order != null && order.getId() == o.getId()) {
					assignedDrivers.add(driver);
				}
			}
			o.setDrivers(assignedDrivers);
			//log.info("Added to set " + cargo.size() + " cargo units for order " + o.getId());
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
		// TODO Auto-generated method stub
		
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	private Session session;
	
	private static final Logger log = Logger.getLogger(OrderDAO.class);
}
