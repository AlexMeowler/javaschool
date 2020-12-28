package org.retal.service;

import java.util.List;

import org.retal.dao.CargoDAO;
import org.retal.domain.Cargo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CargoAndOrdersService {
	
	public List<Cargo> getAllCargo() {
		return cargoDAO.readAll();
	}
	
	@Autowired
	private CargoDAO cargoDAO;
}
