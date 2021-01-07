package org.retal.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.retal.dao.CargoDAO;
import org.retal.dao.CityDAO;
import org.retal.domain.Cargo;
import org.retal.dto.RoutePointDTO;
import org.retal.dto.RoutePointListWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@Qualifier("routePointsValidator")
public class RoutePointsValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return RoutePointListWrapper.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		log.info("Validating route points");
		RoutePointListWrapper wrapper = (RoutePointListWrapper)target;
		List<RoutePointDTO> points = wrapper.getList();
		for(int i = 0; i < points.size(); i++) {
			RoutePointDTO rp = points.get(i);
			if(cityDAO.read(rp.getCityName()) == null) {
				rp.setError("Invalid value(s). Please don't try to change page code");
			}
			if(cargoDAO.read(rp.getCargoId()) == null) {
				rp.setError("Invalid value(s). Please don't try to change page code");
			}
			if (rp.getIsLoading() == null) {
				rp.setError("Invalid value(s). Please don't try to change page code");
			}
		}
		if(!errors.hasErrors()) {
			Map<Integer, Integer[]> cargoEntries = new HashMap<>(); 
			//Integer[0] - rank, load +1 unload -1; Integer[1] - total encounters, must be 2
			Map<Integer, Set<String>> cargoCitiesEntries = new HashMap<>();
			for(RoutePointDTO rp : points) {
				Integer cargo = rp.getCargoId();
				int sign = rp.getIsLoading() ? 1 : -1;
				if(cargoEntries.containsKey(cargo)) {
					Integer[] values = cargoEntries.get(cargo);
					values[0] += sign;
					values[1]++;
					if(!cargoCitiesEntries.get(cargo).contains(rp.getCityName())) {
						cargoCitiesEntries.get(cargo).add(rp.getCityName());
					} else {
						errors.reject("globalCity", "Sorry, but we don't do logistics within the same city");
					}
				} else {
					cargoEntries.put(cargo, new Integer[] {sign , 1});
					Set<String> set =  new HashSet<>();
					set.add(rp.getCityName());
					cargoCitiesEntries.put(cargo, set);
				}
			}
			cargoEntries.forEach((a, b) -> log.debug("[" + a.toString() + "; " + Arrays.toString(b) + "]"));
			boolean allCargosAreLoadingAndUnloading = true;
			for(Map.Entry<Integer, Integer[]> e : cargoEntries.entrySet()) {
				Integer[] values = e.getValue();
				allCargosAreLoadingAndUnloading &= values[0] == 0 && values[1] == 2;
			}
			if(!allCargosAreLoadingAndUnloading) {
				errors.reject("globalCargo", "All cargo must be loaded somewhere and unloaded somewhere else");
			}
		}
	}
	
	@Autowired
	private CityDAO cityDAO;
	
	@Autowired
	private CargoDAO cargoDAO;
	
	private static final Logger log = Logger.getLogger(RoutePointsValidator.class);
}
