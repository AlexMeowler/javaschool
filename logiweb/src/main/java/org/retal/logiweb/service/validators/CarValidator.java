package org.retal.logiweb.service.validators;

import org.apache.log4j.Logger;
import org.retal.logiweb.dao.CityDAO;
import org.retal.logiweb.domain.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Custom {@linkplain org.retal.logiweb.domain.Car Car} validator.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class CarValidator implements Validator {
  
  private final CityDAO cityDAO;

  private static final Logger log = Logger.getLogger(CarValidator.class);
  
  @Autowired
  public CarValidator(CityDAO cityDAO) {
    this.cityDAO = cityDAO;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return Car.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    log.info("Validating car");
    Car car = (Car) target;
    String id = car.getRegistrationId();
    if (!id.matches("[A-Z]{2}\\d{5}")) {
      throwError(errors, "registrationId",
          "ID must have form of XXYYYYY, where X is A-Z letter and Y is 0-9 digit");
    }
    Integer shiftLength = car.getShiftLength();
    if (shiftLength != null && shiftLength < 1) {
      throwError(errors, "shiftLength", "Shift length must be positive integer");
    }
    Float capacityTons = car.getCapacityTons();
    if (capacityTons != null && capacityTons <= 0) {
      throwError(errors, "capacityTons", "Capacity must be positive decimal");
    }
    if (car.getIsWorking() == null) {
      throwError(errors, "isWorking", "Invalid value. Please don't try to change page code");
    }
    if (cityDAO.read(car.getLocation().getCurrentCity()) == null) {
      throwError(errors, "location", "Invalid value. Please don't try to change page code");
    }
  }

  private void throwError(Errors errors, String property, String message) {
    log.debug(property + " : " + message);
    errors.reject(property, message);
  }

}
