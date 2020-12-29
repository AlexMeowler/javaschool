package org.retal.service;

import java.util.List;

import org.retal.dao.CargoDAO;
import org.retal.domain.Cargo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
public class CargoAndOrdersService {
	//TODO Cargo and Order Validation
	public List<Cargo> getAllCargo() {
		return cargoDAO.readAll();
	}
	
	public void addNewCargo(Cargo cargo, BindingResult bindingResult) {
		//validation
		cargoDAO.add(cargo);
	}
	
	@Autowired
	private CargoDAO cargoDAO;
}
