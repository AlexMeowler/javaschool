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
import org.retal.dao.CargoDAO;
import org.retal.dao.CityDAO;
import org.retal.dao.CityDistanceDAO;
import org.retal.dao.DAO;
import org.retal.dao.OrderDAO;
import org.retal.dao.RoutePointDAO;
import org.retal.dao.TestCase1DAO;
import org.retal.dao.UserDAO;
import org.retal.domain.Car;
import org.retal.domain.Cargo;
import org.retal.domain.City;
import org.retal.domain.Order;
import org.retal.domain.RoutePoint;
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
public class TestCase6CargoAndOrdersPageController {

  private MockMvc mockMvc;

  private UserDAO userDAO;

  private CarDAO carDAO;

  private OrderDAO orderDAO;

  private CargoDAO cargoDAO;

  private RoutePointDAO routePointDAO;

  private CityDAO cityDAO;

  private static final Logger log = Logger.getLogger(TestCase6CargoAndOrdersPageController.class);

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

  @Autowired
  public void setMockMvc(WebApplicationContext wac) {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
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
    new UserService(new UserDAO(), null, null, cityService).addDriversFromFile();
    new CarService(new CarDAO(), null, cityService, null).generateCarForEachCity();
    CargoDAO cargoDAO = new CargoDAO();
    for (int i = 0; i < 5; i++) {
      addCargo(cargoDAO, "Cargo" + i, 200 + i * 100, "Desc" + i);
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

  private MultiValueMap<String, String> generateOrderParameters(String[] names, String[] values) {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    for (int i = 0; i < names.length; i++) {
      params.add(names[i], values[i]);
    }
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
  public void testA1GetAllCitiesAndCargoInfo() throws Exception {
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
    Cargo cargo = cargoDAO.readAll().get(0);
    RoutePoint rp = new RoutePoint();
    City city = cityDAO.readAll().get(0);
    rp.setCargo(cargo);
    rp.setCity(city);
    rp.setIsLoading(true);
    rp.setOrder(order);
    session = DAO.start();
    routePointDAO.setSession(session);
    routePointDAO.add(rp);
    rp = new RoutePoint();
    rp.setCargo(cargo);
    rp.setIsLoading(false);
    rp.setOrder(order);
    rp.setCity(city);
    routePointDAO.add(rp);
    DAO.end(session);
    routePointDAO.setSession(null);
    MvcResult result =
        mockMvc.perform(get("/getCityAndCargoInfo")).andExpect(status().isOk()).andReturn();
    String response = result.getResponse().getContentAsString();
    assertFalse(response.isEmpty());
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA2AddNewOrderFormEasyNoErrors() throws Exception {
    int n = 2;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    Integer id = cargoDAO.readAll().stream().filter(c -> c.getPoints().size() == 0)
        .collect(Collectors.toList()).get(0).getId();
    for (int i = 0; i < n * 3; i += 3) {
      names[i] = "list[" + (i / 3) + "].cityName";
      names[i + 1] = "list[" + (i / 3) + "].cargoId";
      names[i + 2] = "list[" + (i / 3) + "].isLoading";
      values[i] = TestCase1DAO.CITY_NAMES[i / 3];
      values[i + 1] = id.toString();
      values[i + 2] = Boolean.valueOf(i / 3 == 0).toString();
    }
    MultiValueMap<String, String> params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", nullValue()))
        .andExpect(flash().attribute("counter_value", nullValue()));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA3AddNewOrderFormEmptyInput() throws Exception {
    MultiValueMap<String, String> params =
        generateOrderParameters(new String[] {}, new String[] {});
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", notNullValue()))
        .andExpect(flash().attribute("counter_value", notNullValue()));
  }
}
