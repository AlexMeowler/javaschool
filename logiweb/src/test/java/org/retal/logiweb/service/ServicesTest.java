package org.retal.logiweb.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.retal.logiweb.config.spring.web.RootConfig;
import org.retal.logiweb.config.spring.web.WebConfig;
import org.retal.logiweb.controller.AdminPageControllerTest;
import org.retal.logiweb.dao.UserDAO;
import org.retal.logiweb.domain.Car;
import org.retal.logiweb.domain.Cargo;
import org.retal.logiweb.domain.User;
import org.retal.logiweb.dto.RoutePointListWrapper;
import org.retal.logiweb.dto.UserWrapper;
import org.retal.logiweb.service.logic.CityService;
import org.retal.logiweb.service.logic.UserService;
import org.retal.logiweb.service.validators.CarValidator;
import org.retal.logiweb.service.validators.CargoValidator;
import org.retal.logiweb.service.validators.RoutePointsValidator;
import org.retal.logiweb.service.validators.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, RootConfig.class})
@WebAppConfiguration
public class ServicesTest {

  /*
   * this whole test class is done only to cover some specifics which could not be covered when
   * testing controllers.
   */

  private UserService userService;

  private CityService cityService;

  private CargoValidator cargoValidator;

  private UserValidator userValidator;

  private CarValidator carValidator;

  private RoutePointsValidator routePointsValidator;

  private UserDAO userDAO;

  private static final Logger log = Logger.getLogger(ServicesTest.class);

  private static int counter = 1;

  private final Random random = new Random();

  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  @Autowired
  public void setCityService(CityService cityService) {
    this.cityService = cityService;
  }

  @Autowired
  public void setCargoValidator(CargoValidator cargoValidator) {
    this.cargoValidator = cargoValidator;
  }

  @Autowired
  public void setUserValidator(UserValidator userValidator) {
    this.userValidator = userValidator;
  }

  @Autowired
  public void setCarValidator(CarValidator carValidator) {
    this.carValidator = carValidator;
  }

  @Autowired
  public void setRoutePointsValidator(RoutePointsValidator routePointsValidator) {
    this.routePointsValidator = routePointsValidator;
  }

  @Autowired
  public void setUserDAO(UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  @Before
  public void startTest() {
    log.info("---Starting test " + counter + "---");
  }

  @After
  public void endTest() {
    log.info("---Ending test " + counter + "---");
    counter++;
  }

  @AfterClass
  public static void cleanup() {
    AdminPageControllerTest.cleanup();
  }

  /*
   * this is done only to get additional coverage, because those methods are never called during
   * standard validation procedures.
   */
  @Test
  public void testCargoValidatorSupports() {
    assertTrue(cargoValidator.supports(Cargo.class));
    assertFalse(cargoValidator.supports(User.class));

  }

  @Test
  public void testUserValidatorSupports() {
    assertTrue(userValidator.supports(UserWrapper.class));
    assertFalse(userValidator.supports(User.class));
  }

  @Test
  public void testCarValidatorSupports() {
    assertTrue(carValidator.supports(Car.class));
    assertFalse(carValidator.supports(Cargo.class));
  }

  @Test
  public void testRoutePointsValidatorSupports() {
    assertTrue(routePointsValidator.supports(RoutePointListWrapper.class));
    assertFalse(carValidator.supports(List.class));
  }

  @Test(expected = ResponseStatusException.class)
  public void testCitiesFileNotFound() {
    cityService.addCitiesFromFile("abc");
  }

  @Test(expected = ResponseStatusException.class)
  public void testCityDistancesFileNotFound() {
    cityService.addDistancesFromFile("cde");
  }

  @Test(expected = ResponseStatusException.class)
  public void testUserNamesAndSurnamesFilesNotFound() {
    userService.addDriversFromFile("123", "456");
  }

  @Test
  public void testSetAllUserWorkedHoursToZero() {
    cityService.addCitiesFromFile();
    cityService.addDistancesFromFile();
    userService.addDriversFromFile();
    for (User user : userDAO.readAll()) {
      user.getUserInfo().setHoursWorked(15 + random.nextInt(150));
      userDAO.update(user);
    }
    userService.setUsersWorkedHoursToZero();
    for (User user : userDAO.readAll()) {
      assertEquals(0, user.getUserInfo().getHoursWorked().intValue());
    }
  }
}
