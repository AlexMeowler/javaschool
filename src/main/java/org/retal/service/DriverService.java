package org.retal.service;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.dao.CarDAO;
import org.retal.dao.CityDAO;
import org.retal.dao.OrderRouteProgressionDAO;
import org.retal.dao.UserDAO;
import org.retal.domain.Car;
import org.retal.domain.City;
import org.retal.domain.HibernateSessionFactory;
import org.retal.domain.Order;
import org.retal.domain.OrderRouteProgression;
import org.retal.domain.RoutePoint;
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

  private final OrderRouteProgressionDAO orderRouteProgressionDAO;

  private final CargoAndOrdersService cargoAndOrdersService;

  private static final Logger log = Logger.getLogger(DriverService.class);

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public DriverService(SessionInfo sessionInfo, UserDAO userDAO, CityDAO cityDAO, CarDAO carDAO,
      OrderRouteProgressionDAO orderRouteProgressionDAO,
      CargoAndOrdersService cargoAndOrdersService) {
    this.sessionInfo = sessionInfo;
    this.userDAO = userDAO;
    this.cityDAO = cityDAO;
    this.carDAO = carDAO;
    this.orderRouteProgressionDAO = orderRouteProgressionDAO;
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
        /*
         * case LOADING_AND_UNLOADING_CARGO: driver.getUserInfo().setCar(null); break;
         */
        case ON_SHIFT:
          driver.getUserInfo().setCar(null);
          break;
        case RESTING:
          unassignDriverIfPossible(driver);
          driver.getUserInfo().setCar(null);
          break;
        case LOADING_AND_UNLOADING_CARGO:
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
   * @param bindingResult object for storing validating result
   */
  public void changeLocation(BindingResult bindingResult) {
    User driver = sessionInfo.getCurrentUser();
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    boolean canChangeLocation = true;
    City driverCity = driver.getUserInfo().getCity();
    session.persist(driverCity);
    for (RoutePoint rp : driverCity.getPoints()) {
      String statusToAvoid = rp.getIsLoading() ? "prepared" : "loaded";
      canChangeLocation &= !rp.getCargo().getStatus().equalsIgnoreCase(statusToAvoid);
    }
    session.close();
    if (!canChangeLocation) {
      bindingResult.reject("cargoManagement",
          "Not all assigned to current city cargo is loaded/unloaded, please double-check.");
    }
    String userCity = driver.getUserInfo().getCity().getCurrentCity();
    String[] cities = driver.getUserInfo().getOrder().getRoute().split(Order.ROUTE_DELIMETER);
    int index = driver.getUserInfo().getOrder().getOrderRouteProgression().getRouteCounter() + 1;
    int length = index < cities.length
        ? cargoAndOrdersService.lengthBetweenTwoCities(userCity, cities[index])
        : 0;
    length = (int) Math.round((double) length / CargoAndOrdersService.AVERAGE_CAR_SPEED);
    Integer hoursDrived = driver.getUserInfo().getHoursDrived();
    if (hoursDrived != null) {
      hoursDrived += length;
    } else {
      hoursDrived = length;
    }
    Integer hoursWorked = driver.getUserInfo().getHoursWorked() + length;
    Calendar currentDate = new GregorianCalendar();
    Calendar modifiedDate = (Calendar) currentDate.clone();
    modifiedDate.add(Calendar.HOUR_OF_DAY, length);
    if (currentDate.get(Calendar.MONTH) != modifiedDate.get(Calendar.MONTH)) {
      hoursWorked = modifiedDate.get(Calendar.HOUR_OF_DAY);
    }
    if (index < cities.length
        && hoursDrived <= driver.getUserInfo().getOrder().getCar().getShiftLength()) {
      String previousStatus = driver.getUserInfo().getStatus();
      changeStatus(DriverStatus.DRIVING.toString(), bindingResult);
      if (!bindingResult.hasErrors()) {
        City newCity = cityDAO
            .read(driver.getUserInfo().getOrder().getRoute().split(Order.ROUTE_DELIMETER)[index]);
        driver.getUserInfo().setCity(newCity);
        driver.getUserInfo().setHoursWorked(hoursWorked);
        driver.getUserInfo().setHoursDrived(hoursDrived);
        userDAO.update(driver);
        Car car = driver.getUserInfo().getCar();
        car.setLocation(newCity);
        carDAO.update(car);
        OrderRouteProgression orderRouteProgression =
            driver.getUserInfo().getOrder().getOrderRouteProgression();
        orderRouteProgression.incrementCounter();
        orderRouteProgressionDAO.update(orderRouteProgression);
      } else {
        changeStatus(previousStatus, bindingResult);
      }
    } else {
      bindingResult.reject("city",
          "Illegal next city argument. Please don't try to change page code.");
      log.warn("Illegal next city on route of order id=" + driver.getUserInfo().getOrder().getId());
    }
  }

  private void unassignDriverIfPossible(User driver) {
    String userCity = driver.getUserInfo().getCity().getCurrentCity();
    String[] cities = driver.getUserInfo().getOrder().getRoute().split(Order.ROUTE_DELIMETER);
    int index = driver.getUserInfo().getOrder().getOrderRouteProgression().getRouteCounter() + 1;
    int length = index < cities.length
        ? cargoAndOrdersService.lengthBetweenTwoCities(userCity, cities[index])
        : 0;
    length = (int) Math.round((double) length / CargoAndOrdersService.AVERAGE_CAR_SPEED);
    Integer hoursDrived = driver.getUserInfo().getHoursDrived();
    if (hoursDrived != null) {
      hoursDrived += length;
    } else {
      hoursDrived = length;
    }
    if (index < cities.length
        && hoursDrived > driver.getUserInfo().getOrder().getCar().getShiftLength()) {
      driver.getUserInfo().setOrder(null);
      driver.getUserInfo().setHoursDrived(null);
    }
  }
}

