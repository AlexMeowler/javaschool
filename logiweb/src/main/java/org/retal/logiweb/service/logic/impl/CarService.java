package org.retal.logiweb.service.logic.impl;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.retal.logiweb.dao.impl.CarDAO;
import org.retal.logiweb.dao.impl.OrderDAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.service.jms.NotificationSender;
import org.retal.logiweb.service.logic.interfaces.CarServices;
import org.retal.logiweb.service.validators.CarValidator;
import org.retal.table.jms.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

/**
 * Implementation of {@link CarServices} interface.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class CarService implements CarServices {

  private final CarDAO carDAO;

  private final OrderDAO orderDAO;

  private final CityService cityService;

  private final Validator carValidator;

  private final NotificationSender sender;

  private final Random rand = new Random();

  private static final Logger log = Logger.getLogger(CarService.class);

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public CarService(CarDAO carDAO, OrderDAO orderDAO, CityService cityService,
      CarValidator carValidator, NotificationSender sender) {
    this.carDAO = carDAO;
    this.orderDAO = orderDAO;
    this.cityService = cityService;
    this.carValidator = carValidator;
    this.sender = sender;
  }

  public List<Car> getAllCars() {
    return carDAO.readAll();
  }
  
  /**
   * Calls {@link CarDAO#readRows(int, int)} method.
   * @see org.retal.logiweb.dao.interfaces.PartRowsReader#readRows(int, int)
   */
  public List<Car> getPartCars(int from, int amount) {
    return carDAO.readRows(from, amount);
  }
  
  /**
   * Calls {@link CarDAO#getRowsAmount()} method.
   * @see org.retal.logiweb.dao.interfaces.CountableRows#getRowsAmount()
   */
  public int getRowsAmount() {
    return carDAO.getRowsAmount();
  }

  public Car getCar(String primaryKey) {
    return carDAO.read(primaryKey);
  }
  
  

  @Override
  public void addNewCar(Car car, BindingResult bindingResult, String capacity, String shiftlength) {
    doInitialDataValidation(car, bindingResult, capacity, shiftlength);
    Car correlationDB = carDAO.read(car.getRegistrationId());
    if (correlationDB != null) {
      bindingResult.reject("uniqueCarId", "Car ID must be unique");
    }
    if (!bindingResult.hasErrors()) {
      carDAO.add(car);
      sender.send(NotificationType.CARS_UPDATE);
    }
  }

  @Override
  public boolean deleteCar(Car car) {
    boolean status = false;
    if (car.getOrder() == null && car.getDriver() == null) {
      status = true;
      carDAO.delete(car);
      sender.send(NotificationType.CARS_UPDATE);
    }
    return status;
  }

  @Override
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
      sender.send(NotificationType.CARS_UPDATE);
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
      String registrationLetters = c.getCurrentCity().substring(0, 2).toUpperCase();
      String registrationNumber = "" + (10000 + rand.nextInt(90000));
      Car car = new Car(registrationLetters + registrationNumber, 12 + rand.nextInt(13),
          (float) (1 + rand.nextInt(41) * 1.0 / 10), true, c);
      carDAO.add(car);
    }
  }

  @Override
  public List<Car> getAllAvailableCarsForOrderId(Integer id) {
    Order order = orderDAO.read(id);
    List<Car> availableCars = order.getIsCompleted() ? null
        : carDAO.readAll().stream().filter(c -> c.getIsWorking()).filter(c -> c.getOrder() == null)
            .filter(c -> c.getCapacityTons() >= order.getRequiredCapacity())
            .filter(c -> c.getLocation().equals(order.getCar().getLocation()))
            .filter(c -> c.getShiftLength() >= order.getRequiredShiftLength())
            .collect(Collectors.toList());
    String message = (availableCars != null ? availableCars.size() : "null")
        + " cars are fit for order ID=" + order.getId();
    log.debug(message);
    return availableCars;
  }
}
