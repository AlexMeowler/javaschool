package org.retal.service;

import org.apache.log4j.Logger;
import org.retal.dao.UserDAO;
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
			newStatus = newStatus.replace("_", " ");
			driver.getUserInfo().setStatus(newStatus);
			userDAO.update(driver);
		}
	}
	
	@Autowired
	private SessionInfo sessionInfo;
	
	@Autowired
	private UserDAO userDAO;
	
	private static final Logger log = Logger.getLogger(DriverService.class);
}

