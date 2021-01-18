package org.retal.service;

import org.apache.log4j.Logger;
import org.retal.dao.CarDAO;
import org.retal.dao.CityDAO;
import org.retal.dao.UserDAO;
import org.retal.domain.Car;
import org.retal.domain.City;
import org.retal.domain.Order;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.enums.DriverStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

/**
 * Service, containing business-logic methods regarding {@linkplain org.retal.domain.User User}
 * entities with role 'driver'.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class DriverService {

  private final SessionInfo sessionInfo;

  private final UserDAO userDAO;

  private final CityDAO cityDAO;

  private final CarDAO carDAO;

  private final CargoAndOrdersService cargoAndOrdersService;

  private static final Logger log = Logger.getLogger(DriverService.class);

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public DriverService(SessionInfo sessionInfo, UserDAO userDAO, CityDAO cityDAO, CarDAO carDAO,
      CargoAndOrdersService cargoAndOrdersService) {
    this.sessionInfo = sessionInfo;
    this.userDAO = userDAO;
    this.cityDAO = cityDAO;
    this.carDAO = carDAO;
    this.cargoAndOrdersService = cargoAndOrdersService;
  }

  /**
   * Updates driver status.
   * 
   * @param newStatus new status
   * @param bindingResult object to store validation result
   */
  public void changeStatus(String newStatus, BindingResult bindingResult) {
    User driver = sessionInfo.getCurrentUser();
    newStatus = newStatus.toLowerCase().replace(" ", "_");
    DriverStatus status = null;
    try {
      status = DriverStatus.valueOf(newStatus.toUpperCase());
    } catch (IllegalArgumentException e) {
      bindingResult.reject("argument",
          "Illegal argument for status, please don't try to change page code.");
      log.error("Illegal argument for driver status");
    }
    if (status != null) {
      switch (status) {
        case DRIVING:
          if (driver.getUserInfo().getOrder() != null) {
            Car car = driver.getUserInfo().getOrder().getCar();
            if (car.getDriver() == null || car.getDriver().getId() == driver.getId()) {
              driver.getUserInfo().setCar(car);
            } else {
              bindingResult.reject("car",
                  "Could not select car, assigned car is being drived by someone else.");
            }
          } else {
            bindingResult.reject("car",
                "Could not change status, no order (and therefore, no car) is assigned"
                    + " to you at this time.");
          }
          break;
        case LOADING_AND_UNLOADING_CARGO:
          break;
        case ON_SHIFT:
          driver.getUserInfo().setCar(null);
          break;
        case RESTING:
          unassignDriverIfPossible(driver);
          driver.getUserInfo().setCar(null);
          break;
        default:
          break;
      }
      if (!bindingResult.hasErrors()) {
        newStatus = newStatus.replace("_", " ");
        driver.getUserInfo().setStatus(newStatus);
        userDAO.update(driver);
      }
    }
  }

  /**
   * Changes location of driver.
   * 
   * @param city name of city to which location should be changed
   * @param bindingResult object for storing validating result
   */
  public void changeLocation(String city, BindingResult bindingResult) {
    User driver = sessionInfo.getCurrentUser();
    String userCity = driver.getUserInfo().getCity().getCurrentCity();
    String[] cities = driver.getUserInfo().getOrder().getRoute().split(Order.ROUTE_DELIMETER);
    int index = -1;
    for (int i = 0; i < cities.length - 1; i++) {
      if (cities[i + 1].equalsIgnoreCase(city) && cities[i].equalsIgnoreCase(userCity)) {
        index = i + 1;
      }
    }
    int length =
        index != -1 ? cargoAndOrdersService.lengthBetweenTwoCities(userCity, cities[index]) : 0;
    length = (int) Math.round((double) length / CargoAndOrdersService.AVERAGE_CAR_SPEED);
    Integer hoursDrived = driver.getUserInfo().getHoursDrived();
    if (hoursDrived != null) {
      hoursDrived += length;
    } else {
      hoursDrived = length;
    }
    Integer hoursWorked = driver.getUserInfo().getHoursWorked() + length;
    if (index != -1 && hoursDrived <= driver.getUserInfo().getOrder().getCar().getShiftLength()) {
      changeStatus(DriverStatus.DRIVING.toString(), bindingResult);
      City newCity = cityDAO.read(city);
      driver.getUserInfo().setCity(newCity);
      driver.getUserInfo().setHoursWorked(hoursWorked);
      // FIXME month checking
      driver.getUserInfo().setHoursDrived(hoursDrived);
      userDAO.update(driver);
      Car car = driver.getUserInfo().getCar();
      car.setLocation(newCity);
      carDAO.update(car);
    } else {
      bindingResult.reject("city",
          "Illegal next city argument. Please don't try to change page code.");
      log.warn("Illegal next city on route of order id=" + driver.getUserInfo().getOrder().getId());
    }
  }

  private void unassignDriverIfPossible(User driver) {
    String userCity = driver.getUserInfo().getCity().getCurrentCity();
    String[] cities = driver.getUserInfo().getOrder().getRoute().split(Order.ROUTE_DELIMETER);
    int index = -1;
    for (int i = 0; i < cities.length - 1; i++) {
      if (cities[i].equalsIgnoreCase(userCity)) {
        index = i + 1;
      }
    }
    int length =
        index != -1 ? cargoAndOrdersService.lengthBetweenTwoCities(userCity, cities[index]) : 0;
    length = (int) Math.round((double) length / CargoAndOrdersService.AVERAGE_CAR_SPEED);
    Integer hoursDrived = driver.getUserInfo().getHoursDrived();
    if (hoursDrived != null) {
      hoursDrived += length;
    } else {
      hoursDrived = length;
    }
    if (index != -1 && hoursDrived > driver.getUserInfo().getOrder().getCar().getShiftLength()) {
      driver.getUserInfo().setOrder(null);
      driver.getUserInfo().setHoursDrived(null);
    }
  }
}

