package org.retal.logiweb.service.logic.impl;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.retal.logiweb.config.spring.app.hibernate.HibernateSessionFactory;
import org.retal.logiweb.dao.impl.CarDAO;
import org.retal.logiweb.dao.impl.CityDAO;
import org.retal.logiweb.dao.impl.OrderRouteProgressionDAO;
import org.retal.logiweb.dao.impl.UserDAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.OrderRouteProgression;
import org.retal.logiweb.domain.entity.RoutePoint;
import org.retal.logiweb.domain.entity.SessionInfo;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.enums.DriverStatus;
import org.retal.logiweb.service.logic.interfaces.DriverServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

/**
 * Implementation of {@link DriverServices} interface.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class DriverService implements DriverServices {

  private final SessionInfo sessionInfo;

  private final UserDAO userDAO;

  private final CityDAO cityDAO;

  private final CarDAO carDAO;

  private final OrderRouteProgressionDAO orderRouteProgressionDAO;

  private final OrderService cargoAndOrdersService;

  private static final Logger log = Logger.getLogger(DriverService.class);

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public DriverService(SessionInfo sessionInfo, UserDAO userDAO, CityDAO cityDAO, CarDAO carDAO,
      OrderRouteProgressionDAO orderRouteProgressionDAO, OrderService cargoAndOrdersService) {
    this.sessionInfo = sessionInfo;
    this.userDAO = userDAO;
    this.cityDAO = cityDAO;
    this.carDAO = carDAO;
    this.orderRouteProgressionDAO = orderRouteProgressionDAO;
    this.cargoAndOrdersService = cargoAndOrdersService;
  }

  /**
   * Updates driver status. For successful status change, status value should be equal to String
   * representation for any value in {@linkplain org.retal.logiweb.domain.enums.DriverStatus
   * DriverStatus} enumeration. If status changed to {@link DriverStatus#RESTING}, then this method
   * will try to unassign driver from order, if his part is completed.
   * 
   * @param newStatus new status
   * @param bindingResult object to store validation result
   */
  @Override
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
        case ON_SHIFT:
          driver.getUserInfo().setCar(null);
          break;
        case RESTING:
          //unassignDriverIfPossible(driver);
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

  @Override
  public void changeLocation(BindingResult bindingResult) {
    User driver = sessionInfo.getCurrentUser();
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    boolean canChangeLocation = true;
    City driverCity = driver.getUserInfo().getCity();
    session.persist(driverCity);
    Set<RoutePoint> points = driverCity.getPoints().stream()
        .filter(rp -> rp.getOrder().getId() == driver.getUserInfo().getOrder().getId())
        .collect(Collectors.toSet());
    for (RoutePoint rp : points) {
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
    length = (int) Math.round((double) length / OrderService.AVERAGE_CAR_SPEED);
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
}

