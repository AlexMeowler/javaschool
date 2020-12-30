package org.retal.dao;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.domain.Order;
import org.retal.domain.RoutePoint;
import org.springframework.stereotype.Component;

/**
 * Since {@link RoutePoint} class is in Many-To-One relationship with {@link Order},
 * it is required to create {@link Session} and call {@link #setSession(Session)} before working with this DAO.
 * After session is closed, {@link #setSession(Session)} with {@code null} argument should be called.
 */
@Component
public class RoutePointDAO implements DAO<RoutePoint> {

	@Override
	public void add(RoutePoint t) {
		if(session == null) {
			log.error("Session is null");
			return ;
		}
		log.info("Attempt to save route point"); //TODO with ToString()
		session.save(t);
	}

	@Override
	public RoutePoint read(Object... keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(RoutePoint t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(RoutePoint t) {
		// TODO Auto-generated method stub
		
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	private Session session;
	
	private static final Logger log = Logger.getLogger(RoutePointDAO.class);
}
