package org.retal.service;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.retal.dao.CityDAO;
import org.retal.dao.UserDAO;
import org.retal.domain.Car;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.enums.DriverStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DriverService {
	
	public void changeStatus(String newStatus) {
		User driver = sessionInfo.getCurrentUser();
		DriverStatus status = null;
		try {
			status = DriverStatus.valueOf(newStatus.toUpperCase());
		} catch(IllegalArgumentException e) {
			log.error("Illegal argument for driver status");
		}
		if(status != null) {
			boolean isError = false;
			switch(status) {
			case DRIVING:
				Car car = driver.getUserInfo().getOrder().getCar();
				if(car.getDriver() == null) {
					driver.getUserInfo().setCar(car);
				} else {
					isError = true;
				}
				break;
			case LOADING_AND_UNLOADING_CARGO:
				break;
			case ON_SHIFT:
				driver.getUserInfo().setCar(null);
				break;
			case RESTING:
				driver.getUserInfo().setCar(null);
				break;
			default:
				break;
			}
			if(!isError) {
				newStatus = newStatus.replace("_", " ");
				driver.getUserInfo().setStatus(newStatus);
				userDAO.update(driver);
			}
		}
	}
	
	public void changeLocation(String city) {
		//TODO BINDING RESULT
		User driver = sessionInfo.getCurrentUser();
		String userCity = driver.getUserInfo().getCurrentCity().getCurrentCity();
		String[] cities = driver.getUserInfo().getOrder().getRoute().split(";");
		int index = -1;
		for(int i = 0; i < cities.length; i++) {
			if(i < cities.length - 1 && cities[i + 1].equalsIgnoreCase(city) 
								&& cities[i].equalsIgnoreCase(userCity)) {
				index = i + 1;
			}
		}
		if(index != -1) {
			changeStatus(DriverStatus.DRIVING.toString());
			int length = cargoAndOrdersService.lengthBetweenTwoCities(userCity, cities[index]);
			Integer hoursDrived = driver.getUserInfo().getHoursDrived();
			Integer hoursWorked = driver.getUserInfo().getHoursWorked() + length;
			if(hoursDrived != null) {
				hoursDrived += length;
			} else {
				hoursDrived = length;
			}
			driver.getUserInfo().setCurrentCity(cityDAO.read(city));
			driver.getUserInfo().setHoursWorked(hoursWorked);
			//FIXME month checking
			driver.getUserInfo().setHoursDrived(hoursDrived);
			userDAO.update(driver);
			//TODO drived hours,
		} else {
			log.warn("Illegal next city on route of order id=" + driver.getUserInfo().getOrder().getId());
		}
	}
	
	@Autowired
	private SessionInfo sessionInfo;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private CityDAO cityDAO;
	
	@Autowired
	private CargoAndOrdersService cargoAndOrdersService;
	
	private static final Logger log = Logger.getLogger(DriverService.class);
}

