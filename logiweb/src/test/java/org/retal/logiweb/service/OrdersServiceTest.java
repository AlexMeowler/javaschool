package org.retal.logiweb.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.retal.logiweb.config.spring.web.RootConfig;
import org.retal.logiweb.config.spring.web.WebConfig;
import org.retal.logiweb.controller.AdminPageControllerTest;
import org.retal.logiweb.dao.CarDAO;
import org.retal.logiweb.dao.CargoDAO;
import org.retal.logiweb.dao.CityDAO;
import org.retal.logiweb.dao.CityDistanceDAO;
import org.retal.logiweb.dao.OrderDAO;
import org.retal.logiweb.dao.RoutePointDAO;
import org.retal.logiweb.dao.UserDAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.Cargo;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.dto.RoutePointDTO;
import org.retal.logiweb.dto.RoutePointListWrapper;
import org.retal.logiweb.service.logic.CityService;
import org.retal.logiweb.service.logic.OrderService;
import org.retal.logiweb.service.logic.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, RootConfig.class})
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OrdersServiceTest {

  private UserDAO userDAO;

  private CarDAO carDAO;

  private OrderDAO orderDAO;

  private CityDAO cityDAO;

  private OrderService orderService;

  private static final Logger log = Logger.getLogger(OrdersServiceTest.class);

  private static int counter = 1;


  @Configuration
  static class ContextConfiguration {

    @Bean
    public JmsTemplate jmsTemplate() {
      ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
      activeMQConnectionFactory.setBrokerURL("vm://localhost?broker.persistent=false");
      SingleConnectionFactory factory = new SingleConnectionFactory(activeMQConnectionFactory);
      factory.setReconnectOnException(true);
      JmsTemplate template = new JmsTemplate(factory);
      template.setDefaultDestinationName("testQueue");
      return template;
    }
  }

  @Autowired
  public void setUserDAO(UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  @Autowired
  public void setOrderDAO(OrderDAO orderDAO) {
    this.orderDAO = orderDAO;
  }

  @Autowired
  public void setCarDAO(CarDAO carDAO) {
    this.carDAO = carDAO;
  }

  @Autowired
  public void setCargoDAO(CargoDAO cargoDAO) {
  }

  @Autowired
  public void setRoutePointDAO(RoutePointDAO routePointDAO) {
  }

  @Autowired
  public void setCityDAO(CityDAO cityDAO) {
    this.cityDAO = cityDAO;
  }

  @Autowired
  public void setOrderService(OrderService orderService) {
    this.orderService = orderService;
  }

  /**
   * Creates user account and fills DB before tests.
   */
  @BeforeClass
  public static void createManagerUserAndFillDataBase() {
    User user = new User();
    user.setLogin("manager");
    user.setPassword("managerpass");
    user.setRole("manager");
    UserInfo userInfo = new UserInfo();
    CityDAO cityDAO = new CityDAO();
    City city = new City();
    city.setCurrentCity("Moscow");
    cityDAO.add(city);
    userInfo.setCity(city);
    userInfo.setName("manager");
    userInfo.setSurname("manager");
    userInfo.setStatus("on shift");
    user.setUserInfo(userInfo);
    new UserDAO().add(user);
    CityService cityService = new CityService(new CityDAO(), new CityDistanceDAO());
    cityService.addCitiesFromFile();
    cityService.addDistancesFromFile();
    new UserService(new UserDAO(), null, null, cityService, null).addDriversFromFile();
    List<City> cities = new CityDAO().readAll();
    CarDAO carDAO = new CarDAO();
    Random random = new Random();
    for (City c : cities) {
      Car car = new Car();
      car.setIsWorking(true);
      car.setLocation(c);
      car.setCapacityTons((float) (1 + random.nextInt(41) * 1.0 / 10));
      String registrationLetters = c.getCurrentCity().substring(0, 2).toUpperCase();
      String registrationNumber = "" + (random.nextInt(10000));
      car.setRegistrationId(registrationLetters + registrationNumber);
      car.setShiftLength(12 + random.nextInt(13));
      carDAO.add(car);
    }
    CargoDAO cargoDAO = new CargoDAO();
    int n = 30;
    for (int i = 0; i < n; i++) {
      addCargo(cargoDAO, "Cargo" + i, 200 + i * 20, "Desc" + i);
    }
  }

  private static void addCargo(CargoDAO cargoDAO, String name, Integer weight, String description) {
    Cargo cargo = new Cargo();
    cargo.setName(name);
    cargo.setMass(weight);
    cargo.setDescription(description);
    cargo.setStatus("prepared");
    cargoDAO.add(cargo);
  }

  /**
   * Cleaning DB after all tests are performed.
   */
  @AfterClass
  public static void cleanup() {
    AdminPageControllerTest.cleanup();
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

  @Test
  public void testA1MonthChange() {
    final String[] cities = {"Moscow", "Yaroslavl"};
    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.MONTH, Calendar.JANUARY);
    calendar.set(Calendar.DAY_OF_MONTH, 31);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    orderService.setCalendarForSimulation(calendar);
    userDAO.readAllWithRole("driver").stream()
        .filter(u -> u.getUserInfo().getCity().getCurrentCity().equals(cities[0])).forEach(u -> {
          u.getUserInfo().setHoursWorked(OrderService.MONTH_HOURS_LIMIT - 1);
          userDAO.update(u);
        });

    List<RoutePointDTO> list = new ArrayList<>();
    list.add(new RoutePointDTO(cities[0], true, 1));
    list.add(new RoutePointDTO(cities[1], false, 1));
    RoutePointListWrapper wrapper = new RoutePointListWrapper();
    wrapper.setList(list);
    BindingResult bindingResult = new BindException(wrapper, "wrapper");
    int before = orderDAO.readAll().size();
    orderService.createOrderAndRoutePoints(wrapper, bindingResult);
    int after = orderDAO.readAll().size();
    assertEquals(before + 1, after);
  }

  @Test
  public void testA2ChainDeletion() {
    final String[] cities = {"Yaroslavl", "Cheboksary", "Kazan"};
    final int offset = 7;
    User lastDriver = userDAO.readAllWithRole("driver").stream()
        .filter(u -> u.getUserInfo().getCity().getCurrentCity().equalsIgnoreCase(cities[0]))
        .findFirst().orElse(null);
    userDAO.update(lastDriver);
    List<User> drivers = userDAO.readAllWithRole("driver").stream()
        .filter(u -> !u.getUserInfo().getCity().getCurrentCity().equalsIgnoreCase(cities[0]))
        .collect(Collectors.toList());
    User firstDriver = drivers.get(drivers.size() - 1);
    firstDriver.getUserInfo().setCity(cityDAO.read(cities[0]));
    firstDriver.getUserInfo().setHoursWorked(OrderService.MONTH_HOURS_LIMIT - offset);
    userDAO.update(firstDriver);
    assertNotEquals(firstDriver, lastDriver);
    drivers.stream()
        .filter(u -> u.getUserInfo().getCity().getCurrentCity().equalsIgnoreCase(cities[1]))
        .forEach(u -> {
          u.getUserInfo().setHoursWorked(OrderService.MONTH_HOURS_LIMIT);
          userDAO.update(u);
        });
    Car car = carDAO.readAll().stream()
        .filter(c -> c.getLocation().getCurrentCity().equalsIgnoreCase(cities[0])).findFirst()
        .orElse(null);
    car.setCapacityTons(10f);
    car.setShiftLength(22);
    carDAO.update(car);
    List<RoutePointDTO> list = new ArrayList<>();
    for (int i = 0; i < cities.length + 1; i++) {
      list.add(new RoutePointDTO(cities[(int) Math.round((double) i / 2)], i % 2 == 0, 2 + i / 2));
    }
    RoutePointListWrapper wrapper = new RoutePointListWrapper();
    wrapper.setList(list);
    BindingResult bindingResult = new BindException(wrapper, "wrapper");
    int before = orderDAO.readAll().size();
    orderService.createOrderAndRoutePoints(wrapper, bindingResult);
    int after = orderDAO.readAll().size();
    assertEquals(before + 1, after);
  }
}

