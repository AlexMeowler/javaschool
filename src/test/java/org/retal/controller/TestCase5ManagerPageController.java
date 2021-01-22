package org.retal.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
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
import org.retal.config.spring.RootConfig;
import org.retal.config.spring.WebConfig;
import org.retal.controller.TestCase4AdminPageController.RunnableWithExceptionThrows;
import org.retal.dao.CarDAO;
import org.retal.dao.CityDAO;
import org.retal.dao.CityDistanceDAO;
import org.retal.dao.DAO;
import org.retal.dao.OrderDAO;
import org.retal.dao.TestCase1DAO;
import org.retal.dao.UserDAO;
import org.retal.domain.Car;
import org.retal.domain.City;
import org.retal.domain.Order;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.service.CarService;
import org.retal.service.CityService;
import org.retal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class TestCase5ManagerPageController {

  private MockMvc mockMvc;

  private UserDAO userDAO;

  private CarDAO carDAO;

  private OrderDAO orderDAO;

  private static final Logger log = Logger.getLogger(TestCase5ManagerPageController.class);

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
    TestCase4AdminPageController.createAdminAndDriverUsers();
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
    new UserService(new UserDAO(), null, null, cityService).addDriversFromFile();
    new CarService(new CarDAO(), null, cityService, null).generateCarForEachCity();
  }

  /**
   * Cleaning DB after all tests are performed.
   */
  @AfterClass
  public static void cleanup() {
    TestCase4AdminPageController.cleanup();
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
  public void testA1AddNewDriverForm() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    mockMvc.perform(post("/addNewDriver").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", nullValue()));
    assertNotNull(userDAO.findUser(params.getFirst("login")));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA2AddNewDriverFormValidationFailAndRedirectToAdminPageWithFlashAttributes()
      throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    params.set("login", "a");
    params.set("name", "bb123");
    params.set("surname", "dd123");
    params.set("currentCity", "Abc");
    params.set("password", "a1");
    params.set("status", "flexing");
    MvcResult result = mockMvc.perform(post("/addNewDriver").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ManagerPageController.MANAGER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()))
        .andReturn();
    assertNull(userDAO.findUser(params.getFirst("login")));
    mockMvc.perform(get(ManagerPageController.MANAGER_PAGE).flashAttrs(result.getFlashMap()))
        .andExpect(status().isOk());
    params = generateUserParameters();
    params.set("login", "a");
    params.set("name", "");
    params.set("surname", "DROP TABLE USERS");
    params.set("currentCity", "Abc");
    params.set("password", "");
    params.set("status", "flexing");
    result = mockMvc.perform(post("/addNewDriver").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ManagerPageController.MANAGER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()))
        .andReturn();
    assertNull(userDAO.findUser(params.getFirst("login")));
    mockMvc.perform(get(ManagerPageController.MANAGER_PAGE).flashAttrs(result.getFlashMap()))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA3DeleteDriverAssignedToOrderOrCar() throws Exception {
    Car car = carDAO.readAll().get(0);
    Session session = DAO.start();
    orderDAO.setSession(session);
    Order order = new Order();
    order.setCar(car);
    order.setIsCompleted(false);
    order.setRoute(TestCase1DAO.CITY_NAMES[0] + Order.ROUTE_DELIMETER + TestCase1DAO.CITY_NAMES[1]);
    order.setRequiredCapacity(0.3f);
    order.setRequiredShiftLength(3);
    orderDAO.add(order);
    DAO.end(session);
    orderDAO.setSession(null);
    User user = userDAO.findUser(generateUserParameters().getFirst("login"));
    user.getUserInfo().setOrder(order);
    userDAO.update(user);
    Integer id = user.getId();
    RunnableWithExceptionThrows tryToDelete = () -> {
      mockMvc.perform(get("/deleteDriver/" + id)).andExpect(status().is3xxRedirection())
          .andExpect(flash().attribute("error_userDeletionFailed", notNullValue()));
      assertNotNull(userDAO.read(id));
    };
    tryToDelete.run();
    user.getUserInfo().setOrder(null);
    user.getUserInfo().setCar(car);
    userDAO.update(user);
    tryToDelete.run();
    user.getUserInfo().setCar(null);
    userDAO.update(user);
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA4AddExistingDriver() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    mockMvc.perform(post("/addNewDriver").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA5EditDriver() throws Exception {
    Integer id = userDAO.findUser(generateUserParameters().getFirst("login")).getId();
    MvcResult result = mockMvc.perform(get("/editDriver/" + id))
        .andExpect(status().is3xxRedirection()).andExpect(flash().attribute("user", notNullValue()))
        .andExpect(flash().attribute("we", notNullValue())).andReturn();
    mockMvc.perform(get("/editDriver").flashAttrs(result.getFlashMap())).andExpect(status().isOk())
        .andExpect(forwardedUrl("/pages/editUser.jsp"));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA6SubmitEditedDriver() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    String name = "changedName";
    params.set("name", name);
    params.set("login", "changedLogin");
    params.set("password", "");
    params.set("id",
        Integer.toString(userDAO.findUser(generateUserParameters().getFirst("login")).getId()));
    mockMvc.perform(post("/submitEditedDriver").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ManagerPageController.MANAGER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", nullValue()));
    User user = userDAO.findUser(params.getFirst("login"));
    assertEquals(user.getUserInfo().getName(), name);
    user.setLogin(generateUserParameters().getFirst("login"));
    userDAO.update(user);
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA7SubmitEditedDriverAssignedToOrderOrCar() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    Order order = orderDAO.readAll().get(0);
    User user = userDAO.findUser(params.getFirst("login"));
    user.getUserInfo().setOrder(order);
    userDAO.update(user);
    String name = "changedAgainName";
    params.set("name", name);
    params.set("id", Integer.toString(userDAO.findUser(params.getFirst("login")).getId()));
    RunnableWithExceptionThrows r = () -> {
      mockMvc.perform(post("/submitEditedDriver").params(params).with(csrf()))
          .andExpect(redirectedUrl("/editDriver")).andExpect(status().is3xxRedirection())
          .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()));
      assertNotEquals(userDAO.findUser(params.getFirst("login")).getUserInfo().getName(), name);
    };
    r.run();
    Car car =
        carDAO.readAll().stream().filter(c -> c.getLocation().equals(user.getUserInfo().getCity()))
            .collect(Collectors.toList()).get(0);
    user.getUserInfo().setCar(car);
    user.getUserInfo().setOrder(null);
    userDAO.update(user);
    r.run();
    user.getUserInfo().setCar(null);
    userDAO.update(user);
    assertNull(user.getUserInfo().getOrder());
    assertNull(user.getUserInfo().getCar());
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA8SubmitEditedDriverValidationFail() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    String name = "ab2";
    params.set("name", name);
    params.set("id", Integer.toString(userDAO.findUser(params.getFirst("login")).getId()));
    mockMvc.perform(post("/submitEditedDriver").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/editDriver"))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()));
    assertNotEquals(userDAO.findUser(params.getFirst("login")).getUserInfo().getName(), name);
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA9DeleteUserWithHigherAutority() throws Exception {
    Integer id = 1;
    mockMvc.perform(get("/deleteDriver/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/403/manager"));
    assertNotNull(userDAO.read(id));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB1EditUserWithHigherAutority() throws Exception {
    Integer id = 1;
    MultiValueMap<String, String> map = generateUserParameters();
    map.set("id", id.toString());
    map.set("login", "newlogin");
    mockMvc.perform(post("/submitEditedDriver").params(map).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/403/manager"));
    assertNotEquals(userDAO.read(id).getRole(), "driver");
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB2EditDriverAdmin() throws Exception {
    Integer id = 1;
    mockMvc.perform(get("/editDriver/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/403/manager"));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB3DeleteUser() throws Exception {
    Integer id = userDAO.findUser(generateUserParameters().getFirst("login")).getId();
    mockMvc.perform(get("/deleteDriver/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("error_userDeletionFailed", nullValue()));
    assertNull(userDAO.read(id));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB4AddNewCarForm() throws Exception {
    MultiValueMap<String, String> params = generateCarParameters();
    mockMvc.perform(post("/addNewCar").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", nullValue()));
    assertNotNull(carDAO.read(params.getFirst("registrationId")));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB5AddNewCarFormValidationFail() throws Exception {
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
    mockMvc.perform(get(ManagerPageController.MANAGER_PAGE).flashAttrs(result.getFlashMap()))
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
  public void testB6DeleteCar() throws Exception {
    String id = carDAO.readAll().stream().filter(c -> c.getOrder() == null && c.getDriver() == null)
        .collect(Collectors.toList()).get(0).getRegistrationId();
    mockMvc.perform(get("/deleteCar/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("error_carDeletionFailed", nullValue()));
    assertNull(carDAO.read(id));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB7DeleteCarAssignedToOrder() throws Exception {
    Car car = carDAO.readAll().stream().filter(c -> c.getOrder() == null && c.getDriver() == null)
        .collect(Collectors.toList()).get(0);
    Order order = orderDAO.readAll().get(0);
    order.setCar(car);
    Session session = DAO.start();
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
  public void testB8DeleteCarBeingDriven() throws Exception {
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
  public void testB9EditCarButton() throws Exception {
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
  public void testC1UpdateCar() throws Exception {
    String id = carDAO.readAll().stream().filter(c -> c.getOrder() == null && c.getDriver() == null)
        .collect(Collectors.toList()).get(0).getRegistrationId();
    MultiValueMap<String, String> params = generateCarParameters();
    params.set("registrationId", id);
    params.set("isWorking", "false");
    mockMvc.perform(post("/submitEditedCar").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ManagerPageController.MANAGER_PAGE))
        .andExpect(flash().attribute("car", nullValue()));
    assertFalse(carDAO.read(id).getIsWorking());
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testC2UpdateCarValidationFail() throws Exception {
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
  public void testC3UpdateCarAssignedToOrder() throws Exception {
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
  public void testC4UpdateCarBeingDriven() throws Exception {
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
  public void testE1GetHomePage() throws Exception {
    mockMvc.perform(get(ManagerPageController.MANAGER_PAGE)).andExpect(status().isOk())
        .andExpect(model().attribute("current_user_name", "manager manager"));
  }

}
