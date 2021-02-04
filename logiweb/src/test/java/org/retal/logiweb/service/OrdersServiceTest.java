package org.retal.logiweb.service;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.hibernate.Session;
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
import org.retal.logiweb.dao.DAO;
import org.retal.logiweb.dao.DAOTest;
import org.retal.logiweb.dao.OrderDAO;
import org.retal.logiweb.dao.RoutePointDAO;
import org.retal.logiweb.dao.UserDAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.Cargo;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.RoutePoint;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.service.logic.CityService;
import org.retal.logiweb.service.logic.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, RootConfig.class})
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OrdersServiceTest {

  private UserDAO userDAO;

  private CarDAO carDAO;

  private OrderDAO orderDAO;

  private CargoDAO cargoDAO;

  private RoutePointDAO routePointDAO;

  private CityDAO cityDAO;

  private static final Logger log = Logger.getLogger(OrdersServiceTest.class);

  private static int counter = 1;


  @Configuration
  static class ContextConfiguration {

    @Bean
    public UserDAO getUserDAO() {
      return new UserDAO();
    }

    @Bean
    public CarDAO getCarDAO() {
      return new CarDAO();
    }

    @Bean
    public OrderDAO getOrderDAO() {
      return new OrderDAO();
    }

    @Bean
    public CargoDAO getCargoDAO() {
      return new CargoDAO();
    }

    @Bean
    public RoutePointDAO getRoutePointDAO() {
      return new RoutePointDAO();
    }

    @Bean
    public CityDAO getCityDAO() {
      return new CityDAO();
    }
    
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
    this.cargoDAO = cargoDAO;
  }

  @Autowired
  public void setRoutePointDAO(RoutePointDAO routePointDAO) {
    this.routePointDAO = routePointDAO;
  }

  @Autowired
  public void setCityDAO(CityDAO cityDAO) {
    this.cityDAO = cityDAO;
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
  public void testA1() {
    
  }
}

