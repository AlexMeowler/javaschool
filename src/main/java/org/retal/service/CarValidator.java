package org.retal.service;

import org.apache.log4j.Logger;
import org.retal.domain.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@Qualifier("carValidator")
public class CarValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Car.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		//logging
		log.info("Validating car");
		Car car = (Car)target;
		String id = car.getRegistrationId();
		if (id.length() != 7 && !id.matches("[A-Z]{2}\\d{5}")) {
			String property = "registrationId";
			String message = "ID must have form of XXYYYYY, where X is A-Z letter and Y is 0-9 digit";
			log.info(property + " : " + message);
			errors.reject(property, message);
		}
		Integer shiftLength = car.getShiftLength();
		if (shiftLength != null && shiftLength < 1) {
			String property = "shiftLength";
			String message = "Shift length must be positive integer";
			log.info(property + " : " + message);
			errors.reject(property, message);
		}
		Float capacityTons = car.getCapacityTons();
		if (capacityTons != null && capacityTons <= 0) {
			String property = "capacityTons";
			String message = "Capacity must be positive decimal";
			log.info(property + " : " + message);
			errors.reject(property, message);
		}
	}
	
	@Autowired
	private static final Logger log = Logger.getLogger(CarValidator.class);
}
