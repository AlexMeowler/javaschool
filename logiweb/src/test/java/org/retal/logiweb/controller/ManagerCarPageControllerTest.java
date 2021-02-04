package org.retal.logiweb.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import org.retal.logiweb.dao.CarDAO;
import org.retal.logiweb.dao.CityDAO;
import org.retal.logiweb.dao.CityDistanceDAO;
import org.retal.logiweb.dao.DAO;
import org.retal.logiweb.dao.DAOTest;
import org.retal.logiweb.dao.OrderDAO;
import org.retal.logiweb.dao.UserDAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.service.logic.CarService;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, RootConfig.class})
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ManagerCarPageControllerTest {

  private MockMvc mockMvc;

  private UserDAO userDAO;

  private CarDAO carDAO;

  private OrderDAO orderDAO;

  private static final Logger log = Logger.getLogger(ManagerCarPageControllerTest.class);

  private static int counter = 1;

  private Random random = new Random();


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
  public void setMockMvc(WebApplicationContext wac) {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  /**
   * Creates user account and fills DB before tests.
   */
  @BeforeClass
  public static void createAdminAndManagerUserAndFillDataBase() {
    AdminPageControllerTest.createAdminAndDriverUsers();
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
    user.setUserInfo(userInfo);
    new UserDAO().add(user);
    CityService cityService = new CityService(new CityDAO(), new CityDistanceDAO());
    cityService.addCitiesFromFile();
    cityService.addDistancesFromFile();
    new UserService(new UserDAO(), null, null, cityService, null).addDriversFromFile();
    new CarService(new CarDAO(), null, cityService, null, null).generateCarForEachCity();
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

  private MultiValueMap<String, String> generateUserParameters() {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("login", "testusername");
    params.add("password", "testpassword");
    params.add("role", "driver");
    params.add("name", "testname");
    params.add("surname", "testsurname");
    params.add("status", "resting");
    params.add("currentCity", "Moscow");
    return params;
  }

  private MultiValueMap<String, String> generateCarParameters() {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    char firstLetter = (char) ('A' + (char) random.nextInt(26));
    char secondLetter = (char) ('A' + (char) random.nextInt(26));
    params.add("registrationId", Character.toString(firstLetter) + Character.toString(secondLetter)
        + (10000 + random.nextInt(90000)));
    params.add("shift", Integer.toString(15 + random.nextInt(31)));
    params.add("capacity", Float.toString((float) (10 + random.nextInt(41)) / 10));
    params.add("isWorking", "true");
    params.add("currentCity", "Moscow");
    return params;
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA1AddNewCarForm() throws Exception {
    MultiValueMap<String, String> params = generateCarParameters();
    mockMvc.perform(post("/addNewCar").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", nullValue()));
    assertNotNull(carDAO.read(params.getFirst("registrationId")));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA2AddNewCarFormValidationFail() throws Exception {
    MultiValueMap<String, String> params = generateCarParameters();
    params.set("registrationId", "asdhnwqe");
    params.set("shift", "-2");
    params.set("capacity", "-15");
    params.set("isWorking", "after");
    params.set("currentCity", "abc");
    MvcResult result = mockMvc.perform(post("/addNewCar").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "car", notNullValue()))
        .andReturn();
    assertNull(carDAO.read(params.getFirst("registrationId")));
    mockMvc
        .perform(
            get(ManagerCarPageController.MANAGER_CARS_PAGE + "/1").flashAttrs(result.getFlashMap()))
        .andExpect(status().isOk());
    //
    params = generateCarParameters();
    params.set("registrationId", carDAO.readAll().get(0).getRegistrationId());
    params.set("shift", "-2.1");
    params.set("capacity", "-15dd");
    mockMvc.perform(post("/addNewCar").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "car", notNullValue()))
        .andReturn();
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA3DeleteCar() throws Exception {
    String id = carDAO.readAll().stream().filter(c -> c.getOrder() == null && c.getDriver() == null)
        .collect(Collectors.toList()).get(0).getRegistrationId();
    mockMvc.perform(get("/deleteCar/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("error_carDeletionFailed", nullValue()));
    assertNull(carDAO.read(id));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA4DeleteCarAssignedToOrder() throws Exception {
    mockMvc.perform(post("/addNewDriver").params(generateUserParameters()).with(csrf()));
    Car car = carDAO.readAll().get(0);
    Session session = DAO.start();
    orderDAO.setSession(session);
    Order order = new Order();
    order.setCar(car);
    order.setIsCompleted(false);
    order.setRoute(DAOTest.CITY_NAMES[0] + Order.ROUTE_DELIMETER + DAOTest.CITY_NAMES[1]);
    order.setRequiredCapacity(0.3f);
    order.setRequiredShiftLength(3);
    orderDAO.add(order);
    DAO.end(session);
    orderDAO.setSession(null);
    User user = userDAO.findUser(generateUserParameters().getFirst("login"));
    user.getUserInfo().setOrder(order);
    userDAO.update(user);
    car = carDAO.readAll().stream().filter(c -> c.getOrder() == null && c.getDriver() == null)
        .collect(Collectors.toList()).get(0);
    order = orderDAO.readAll().get(0);
    order.setCar(car);
    session = DAO.start();
    orderDAO.setSession(session);
    orderDAO.update(order);
    orderDAO.setSession(null);
    DAO.end(session);
    mockMvc.perform(get("/deleteCar/" + car.getRegistrationId()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("error_carDeletionFailed", notNullValue()));
    assertNotNull(carDAO.read(car.getRegistrationId()));
    order.setCar(null);
    session = DAO.start();
    orderDAO.setSession(session);
    orderDAO.update(order);
    orderDAO.setSession(null);
    DAO.end(session);
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA5DeleteCarBeingDriven() throws Exception {
    Car car = carDAO.readAll().stream().filter(c -> c.getOrder() == null && c.getDriver() == null)
        .collect(Collectors.toList()).get(0);
    User user = userDAO.readAllWithRole("driver").get(0);
    user.getUserInfo().setCar(car);
    userDAO.update(user);
    mockMvc.perform(get("/deleteCar/" + car.getRegistrationId()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("error_carDeletionFailed", notNullValue()));
    assertNotNull(carDAO.read(car.getRegistrationId()));
    user.getUserInfo().setCar(null);
    userDAO.update(user);
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA6EditCarButton() throws Exception {
    String id = carDAO.readAll().stream().filter(c -> c.getOrder() == null && c.getDriver() == null)
        .collect(Collectors.toList()).get(0).getRegistrationId();
    MvcResult result = mockMvc.perform(get("/editCar/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/editCar")).andExpect(flash().attribute("car", notNullValue()))
        .andReturn();
    mockMvc.perform(get("/editCar").flashAttrs(result.getFlashMap())).andExpect(status().isOk())
        .andExpect(model().attribute("cityList", notNullValue()))
        .andExpect(forwardedUrl("/pages/editCar.jsp"));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA7UpdateCar() throws Exception {
    String id = carDAO.readAll().stream().filter(c -> c.getOrder() == null && c.getDriver() == null)
        .collect(Collectors.toList()).get(0).getRegistrationId();
    MultiValueMap<String, String> params = generateCarParameters();
    params.set("registrationId", id);
    params.set("isWorking", "false");
    mockMvc.perform(post("/submitEditedCar").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ManagerCarPageController.MANAGER_CARS_PAGE))
        .andExpect(flash().attribute("car", nullValue()));
    assertFalse(carDAO.read(id).getIsWorking());
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA8UpdateCarValidationFail() throws Exception {
    String id = carDAO.readAll().stream()
        .filter(c -> c.getOrder() == null && c.getDriver() == null && c.getIsWorking())
        .collect(Collectors.toList()).get(0).getRegistrationId();
    MultiValueMap<String, String> params = generateCarParameters();
    params.set("registrationId", id);
    params.set("shift", "abc");
    params.set("isWorking", "false");
    mockMvc.perform(post("/submitEditedCar").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/editCar"))
        .andExpect(flash().attribute("car", notNullValue()));
    assertTrue(carDAO.read(id).getIsWorking());
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA9UpdateCarAssignedToOrder() throws Exception {
    Car car = carDAO.readAll().stream()
        .filter(c -> c.getOrder() == null && c.getDriver() == null && c.getIsWorking())
        .collect(Collectors.toList()).get(0);
    Order order = orderDAO.readAll().get(0);
    order.setCar(car);
    Session session = DAO.start();
    orderDAO.setSession(session);
    orderDAO.update(order);
    orderDAO.setSession(null);
    DAO.end(session);
    MultiValueMap<String, String> params = generateCarParameters();
    params.set("registrationId", car.getRegistrationId());
    params.set("isWorking", "false");
    mockMvc.perform(post("/submitEditedCar").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/editCar"))
        .andExpect(flash().attribute("car", notNullValue()));
    assertTrue(carDAO.read(car.getRegistrationId()).getIsWorking());
    order.setCar(null);
    session = DAO.start();
    orderDAO.setSession(session);
    orderDAO.update(order);
    orderDAO.setSession(null);
    DAO.end(session);
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB1UpdateCarBeingDriven() throws Exception {
    Car car = carDAO.readAll().stream()
        .filter(c -> c.getOrder() == null && c.getDriver() == null && c.getIsWorking())
        .collect(Collectors.toList()).get(0);
    User user = userDAO.readAllWithRole("driver").get(0);
    user.getUserInfo().setCar(car);
    userDAO.update(user);
    MultiValueMap<String, String> params = generateCarParameters();
    params.set("registrationId", car.getRegistrationId());
    params.set("isWorking", "false");
    mockMvc.perform(post("/submitEditedCar").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/editCar"))
        .andExpect(flash().attribute("car", notNullValue()));
    assertTrue(carDAO.read(car.getRegistrationId()).getIsWorking());
    user.getUserInfo().setCar(null);
    userDAO.update(user);
  }
  
  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB2EditNonExistentCar() throws Exception {
    String id = "abcdef";
    mockMvc.perform(get("/editCar/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(GlobalExceptionHandler.ERROR_PAGE));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB3GetHomePage() throws Exception {
    mockMvc.perform(get(ManagerCarPageController.MANAGER_CARS_PAGE))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ManagerCarPageController.MANAGER_CARS_PAGE + "/1"));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB4GetHomePageBigNumber() throws Exception {
    String page = "1000";
    MvcResult result = mockMvc.perform(get(ManagerCarPageController.MANAGER_CARS_PAGE + "/" + page))
        .andExpect(status().is3xxRedirection()).andReturn();
    assertNotEquals(result.getResponse().getRedirectedUrl()
        .substring(ManagerCarPageController.MANAGER_CARS_PAGE.length()).replace("/", ""), page);
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB5GetHomePageNegativeNumber() throws Exception {
    String page = "-10";
    MvcResult result = mockMvc.perform(get(ManagerCarPageController.MANAGER_CARS_PAGE + "/" + page))
        .andExpect(status().is3xxRedirection()).andReturn();
    assertNotEquals(result.getResponse().getRedirectedUrl()
        .substring(ManagerCarPageController.MANAGER_CARS_PAGE.length()).replace("/", ""), page);
  }
}
