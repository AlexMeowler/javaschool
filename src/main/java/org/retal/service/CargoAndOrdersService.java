package org.retal.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.retal.dao.CarDAO;
import org.retal.dao.CargoDAO;
import org.retal.dao.CityDAO;
import org.retal.dao.OrderDAO;
import org.retal.dao.RoutePointDAO;
import org.retal.domain.Cargo;
import org.retal.domain.Order;
import org.retal.domain.RoutePoint;
import org.retal.dto.RoutePointDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class CargoAndOrdersService {
	//TODO Cargo and Order Validation
	public List<Cargo> getAllCargo() {
		return cargoDAO.readAll();
	}
	
	public List<Order> getAllOrders() {
		return orderDAO.readAll();
	}
	
	public void addNewCargo(Cargo cargo, BindingResult bindingResult) {
		//validation
		cargoDAO.add(cargo);
	}
	
	public List<RoutePoint> mapRoutePointDTOsToEntities(List<RoutePointDTO> list) {
		List<RoutePoint> entityList = new ArrayList<>();
		for(RoutePointDTO rpDTO : list) {
			RoutePoint rp = new RoutePoint();
			rp.setIsLoading(rpDTO.getIsLoading());
			rp.setCity(cityDAO.read(rpDTO.getCityName()));
			rp.setCargo(cargoDAO.read(rpDTO.getCargoId()));
			entityList.add(rp);
		}
		return entityList;
	}
	
	public void createOrderWithRoutePoints(List<RoutePoint> list) {
		//TODO validate route points
		Set<RoutePoint> points = new HashSet<>(list);
		Order order = new Order();
		order.setCar(carDAO.read("CX10000")); //FIXME
		order.setPoints(points);
		order.setIsCompleted(false);
		Session session = HibernateSessionFactory.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		orderDAO.setSession(session);
		routePointDAO.setSession(session);
		orderDAO.add(order);
		transaction.commit();
		transaction = session.beginTransaction();
		for(RoutePoint rp : points) {
			rp.setOrder(order);
			routePointDAO.add(rp);
		}
		transaction.commit();
		orderDAO.setSession(null);
		routePointDAO.setSession(null);
		//session.flush();
		session.close();
	}
	
	@Autowired
	private CargoDAO cargoDAO;
	
	@Autowired
	private CityDAO cityDAO;
	
	@Autowired
	private CarDAO carDAO;
	
	@Autowired
	private OrderDAO orderDAO;
	
	@Autowired
	private RoutePointDAO routePointDAO;
}
