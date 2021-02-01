package org.retal.logiweb.service.logic;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.retal.logiweb.dao.CarDAO;
import org.retal.logiweb.dao.OrderDAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.service.validators.CarValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

/**
 * Service, containing business-logic methods regarding {@linkplain org.retal.logiweb.domain.entity.Car
 * Car} entities.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class CarService {

  private final CarDAO carDAO;

  private final OrderDAO orderDAO;

  private final CityService cityService;

  private final Validator carValidator;

  private final Random rand = new Random();

  private static final Logger log = Logger.getLogger(CarService.class);

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public CarService(CarDAO carDAO, OrderDAO orderDAO, CityService cityService,
      CarValidator carValidator) {
    this.carDAO = carDAO;
    this.orderDAO = orderDAO;
    this.cityService = cityService;
    this.carValidator = carValidator;
  }

  public List<Car> getAllCars() {
    return carDAO.readAll();
  }

  public Car getCar(String primaryKey) {
    return carDAO.read(primaryKey);
  }

  /**
   * Validates and adds new car to database.
   * 
   * @param car {@linkplain org.retal.logiweb.domain.entity.Car Car} to be added
   * @param bindingResult object to store validation result
   * @param capacity car capacity (user input) to be parsed
   * @param shiftlength shift length (user input) to be parsed
   */
  public void addNewCar(Car car, BindingResult bindingResult, String capacity, String shiftlength) {
    doInitialDataValidation(car, bindingResult, capacity, shiftlength);
    Car correlationDB = carDAO.read(car.getRegistrationId());
    if (correlationDB != null) {
      bindingResult.reject("uniqueCarId", "Car ID must be unique");
    }
    if (!bindingResult.hasErrors()) {
      carDAO.add(car);
    }
  }

  /**
   * Deletes car from database. Can not delete car if it's assigned to order.
   * 
   * @param car car {@linkplain org.retal.logiweb.domain.entity.Car Car} to be deleted
   * @return true if deletion was successful, false otherwise
   */
  public boolean deleteCar(Car car) {
    boolean status = false;
    if (car.getOrder() == null && car.getDriver() == null) {
      status = true;
      carDAO.delete(car);
    }
    return status;
  }

  /**
   * Updates car in database.
   * 
   * @param car car {@linkplain org.retal.logiweb.domain.entity.Car Car} to be updated
   */
  public void updateCar(Car car, BindingResult bindingResult, String capacity, String shiftlength) {
    log.info("Attempt to update car ID = " + car.getRegistrationId());
    doInitialDataValidation(car, bindingResult, capacity, shiftlength);
    Car persistedCar = carDAO.read(car.getRegistrationId());
    if (persistedCar.getOrder() != null || persistedCar.getDriver() != null) {
      bindingResult.reject("carUnavailable",
          "Car could not be updated due to assigned order or being driven by someone");
    }
    if (!bindingResult.hasErrors()) {
      carDAO.update(car);
    }
  }

  /**
   * Parses String objects and validates other input.
   * 
   * @param car {@linkplain org.retal.logiweb.domain.entity.Car Car} to be validated
   * @param bindingResult object to store validation result
   * @param capacity required capacity string to be parsed
   * @param shiftlength shift length string to be parsed
   */
  private void doInitialDataValidation(Car car, BindingResult bindingResult, String capacity,
      String shiftlength) {
    try {
      Integer shiftLength = Integer.parseInt(shiftlength);
      car.setShiftLength(shiftLength);
    } catch (NumberFormatException e) {
      bindingResult.reject("shiftLength", "Shift length must be positive integer");
    }
    try {
      Float capacityTons = Float.parseFloat(capacity);
      car.setCapacityTons(capacityTons);
    } catch (NumberFormatException e) {
      bindingResult.reject("capacityTons", "Capacity must be positive decimal");
    }
    carValidator.validate(car, bindingResult);
  }

  /**
   * Generates one car for each city and adds it to database.
   */
  public void generateCarForEachCity() {
    List<City> cities = cityService.getAllCities();
    for (City c : cities) {
      Car car = new Car();
      car.setIsWorking(true);
      car.setLocation(c);
      car.setCapacityTons((float) (1 + rand.nextInt(41) * 1.0 / 10));
      String registrationLetters = c.getCurrentCity().substring(0, 2).toUpperCase();
      String registrationNumber = "" + (10000 + rand.nextInt(90000));
      car.setRegistrationId(registrationLetters + registrationNumber);
      car.setShiftLength(12 + rand.nextInt(13));
      carDAO.add(car);
    }
  }

  /**
   * Gets all available cars for given order ID.
   * 
   * @param id {@linkplain org.retal.logiweb.domain.entity.Order Order} related ID
   * @return list of all cars which can be re-assigned to that order or null if order is completed
   */
  public List<Car> getAllAvailableCarsForOrderId(Integer id) {
    Order order = orderDAO.read(id);
    List<Car> availableCars = order.getIsCompleted() ? null
        : carDAO.readAll().stream().filter(c -> c.getIsWorking()).filter(c -> c.getOrder() == null)
            .filter(c -> c.getCapacityTons() >= order.getRequiredCapacity())
            .filter(c -> c.getLocation().equals(order.getCar().getLocation()))
            .filter(c -> c.getShiftLength() >= order.getRequiredShiftLength())
            .collect(Collectors.toList());
    String message =
        availableCars != null ? availableCars.size() + " cars are fit for order ID=" + order.getId()
            : "null";
    log.debug(message);
    return availableCars;
  }
}
