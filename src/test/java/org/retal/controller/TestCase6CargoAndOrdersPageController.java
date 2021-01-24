package org.retal.controller;

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
    Integer id = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
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

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA4AddNewOrderFormValidationFail() throws Exception {
    int n = 2;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    names[0] = "list[0].cityName";
    values[0] = "a1";
    names[1] = "list[0].cargoId";
    values[1] = "123";
    names[2] = "list[0].isLoading";
    values[2] = "abc";
    names[3] = "list[1].cityName";
    values[3] = "Moscow";
    names[4] = "list[1].cargoId";
    values[4] = "100";
    names[5] = "list[1].isLoading";
    values[5] = "true";
    MultiValueMap<String, String> params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", notNullValue()))
        .andExpect(flash().attribute("counter_value", notNullValue()));
    n = 1;
    names = new String[n * 3];
    values = new String[n * 3];
    names[0] = "list[0].cityName";
    values[0] = "Moscow";
    names[1] = "list[0].cargoId";
    values[1] = "1";
    names[2] = "list[0].isLoading";
    values[2] = "abc";
    params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", notNullValue()))
        .andExpect(flash().attribute("counter_value", notNullValue()));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA5AddNewOrderFormWithinTheSameCity() throws Exception {
    int n = 2;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    Integer id = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList()).get(0).getId();
    for (int i = 0; i < n * 3; i += 3) {
      names[i] = "list[" + (i / 3) + "].cityName";
      names[i + 1] = "list[" + (i / 3) + "].cargoId";
      names[i + 2] = "list[" + (i / 3) + "].isLoading";
      values[i] = TestCase1DAO.CITY_NAMES[0];
      values[i + 1] = id.toString();
      values[i + 2] = Boolean.valueOf(i / 3 == 0).toString();
    }
    MultiValueMap<String, String> params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", notNullValue()))
        .andExpect(flash().attribute("counter_value", notNullValue()));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA6AddNewOrderFormLoadingAndUnloadingMismatch() throws Exception {
    int n = 2;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    Integer id = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList()).get(0).getId();
    for (int i = 0; i < n * 3; i += 3) {
      names[i] = "list[" + (i / 3) + "].cityName";
      names[i + 1] = "list[" + (i / 3) + "].cargoId";
      names[i + 2] = "list[" + (i / 3) + "].isLoading";
      values[i] = TestCase1DAO.CITY_NAMES[i / 3];
      values[i + 1] = id.toString();
      values[i + 2] = "true";
    }
    MultiValueMap<String, String> params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", notNullValue()))
        .andExpect(flash().attribute("counter_value", notNullValue()));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA7AddNewOrderFormMultipleLoadingAndUnloading() throws Exception {
    int n = 4;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    Integer id = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList()).get(0).getId();
    for (int i = 0; i < n * 3; i += 3) {
      names[i] = "list[" + (i / 3) + "].cityName";
      names[i + 1] = "list[" + (i / 3) + "].cargoId";
      names[i + 2] = "list[" + (i / 3) + "].isLoading";
      values[i] = TestCase1DAO.CITY_NAMES[i / 3];
      values[i + 1] = id.toString();
      values[i + 2] = Boolean.valueOf((i / 3) % 2 == 0).toString();
    }
    MultiValueMap<String, String> params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", notNullValue()))
        .andExpect(flash().attribute("counter_value", notNullValue()));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA8GetCargoAndOrdersPage() throws Exception {
    mockMvc.perform(get("/cargoAndOrders"))
        .andExpect(model().attribute("cargoList", notNullValue()))
        .andExpect(model().attribute("cityList", notNullValue()))
        .andExpect(model().attribute("ordersList", notNullValue()))
        .andExpect(model().attribute("current_user_name", notNullValue()))
        .andExpect(status().isOk()).andExpect(forwardedUrl("/pages/cargo_orders.jsp"));
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA9GetCargoAndOrdersPageWithReplacementCarsAvailable() throws Exception {
    City city = orderDAO.readAll().get(0).getCar().getLocation();
    Car car = new Car();
    car.setCapacityTons(1000f);
    car.setLocation(city);
    car.setIsWorking(true);
    car.setShiftLength(1000);
    car.setRegistrationId("ZZ12345");
    carDAO.add(car);
    MvcResult result = mockMvc.perform(get("/cargoAndOrders")).andExpect(status().isOk())
        .andExpect(forwardedUrl("/pages/cargo_orders.jsp")).andReturn();
    Map<String, Object> model = result.getModelAndView().getModel();
    assertTrue(((List<Boolean>) model.get("hasCarsAvailable")).get(0));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB1ReassignCarToOrderFail() throws Exception {
    MvcResult result = mockMvc.perform(get("/changeCarForOrder/2b_ZZ12345"))
        .andExpect(status().isOk()).andReturn();
    assertNotEquals(result.getResponse().getContentAsString(), "");
    assertNotEquals(orderDAO.readAll().get(0).getCar().getRegistrationId(), "ZZ12345");
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB2ReassignCarToNotFoundOrderFail() throws Exception {
    MvcResult result = mockMvc.perform(get("/changeCarForOrder/100_ZZ12345"))
        .andExpect(status().isOk()).andReturn();
    assertNotEquals(result.getResponse().getContentAsString(), "");
    assertNotEquals(orderDAO.readAll().get(0).getCar().getRegistrationId(), "ZZ12345");
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB3ReassignNonExistentCarToOrderFail() throws Exception {
    MvcResult result =
        mockMvc.perform(get("/changeCarForOrder/1_LK00000")).andExpect(status().isOk()).andReturn();
    assertNotEquals(result.getResponse().getContentAsString(), "");
    assertNotEquals(orderDAO.readAll().get(0).getCar().getRegistrationId(), "LK00000");
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB4ReassignCarToCompletedOrderFail() throws Exception {
    Order order = orderDAO.readAll().get(0);
    Session session = DAO.start();
    order.setIsCompleted(true);
    orderDAO.setSession(session);
    orderDAO.update(order);
    orderDAO.setSession(null);
    DAO.end(session);
    MvcResult result =
        mockMvc.perform(get("/changeCarForOrder/1_ZZ12345")).andExpect(status().isOk()).andReturn();
    assertNotEquals(result.getResponse().getContentAsString(), "");
    assertNotEquals(orderDAO.readAll().get(0).getCar().getRegistrationId(), "ZZ12345");
    session = DAO.start();
    order.setIsCompleted(false);
    orderDAO.setSession(session);
    orderDAO.update(order);
    orderDAO.setSession(null);
    DAO.end(session);
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB5ReassignCarToOrder() throws Exception {
    MvcResult result =
        mockMvc.perform(get("/changeCarForOrder/1_ZZ12345")).andExpect(status().isOk()).andReturn();
    assertEquals("", result.getResponse().getContentAsString());
    assertEquals("ZZ12345", orderDAO.readAll().get(0).getCar().getRegistrationId());
  }

  @SuppressWarnings("unchecked")
  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB6GetCargoAndOrdersPageWithNullReplacementCarsAvailableForCompletedOrder()
      throws Exception {
    Order order = orderDAO.readAll().get(0);
    Session session = DAO.start();
    order.setIsCompleted(true);
    orderDAO.setSession(session);
    orderDAO.update(order);
    orderDAO.setSession(null);
    DAO.end(session);
    MvcResult result = mockMvc.perform(get("/cargoAndOrders")).andExpect(status().isOk())
        .andExpect(forwardedUrl("/pages/cargo_orders.jsp")).andReturn();
    Map<String, Object> model = result.getModelAndView().getModel();
    assertFalse(((List<Boolean>) model.get("hasCarsAvailable")).get(0));
    session = DAO.start();
    order.setIsCompleted(false);
    orderDAO.setSession(session);
    orderDAO.update(order);
    orderDAO.setSession(null);
    DAO.end(session);
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB7AddNewOrderFormTwoCargoNoErrors() throws Exception {
    String[] cities = {"Moscow", "Yaroslavl", "Cheboksary", "Saint-Petersburg"};
    Car car = new Car();
    car.setRegistrationId("MO22832");
    car.setCapacityTons(12f);
    car.setShiftLength(15);
    car.setIsWorking(true);
    car.setLocation(cityDAO.read(cities[0]));
    carDAO.add(car);
    int n = 4;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    List<Cargo> cargo = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList());
    for (int i = 0; i < n * 3; i += 3) {
      names[i] = "list[" + (i / 3) + "].cityName";
      names[i + 1] = "list[" + (i / 3) + "].cargoId";
      names[i + 2] = "list[" + (i / 3) + "].isLoading";
      values[i] = cities[i / 3];
      values[i + 1] = Integer.toString(cargo.get(i / 5).getId());
      values[i + 2] = Boolean.valueOf((i / 3) % 2 == 0).toString();
    }
    MultiValueMap<String, String> params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", nullValue()))
        .andExpect(flash().attribute("counter_value", nullValue()));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB8AddNewOrderCalculationFail() throws Exception {
    String[] cities = {"Kazan", "Cheboksary", "Cheboksary", "Kazan"};
    int n = 4;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    List<Cargo> cargo = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList());
    for (int i = 0; i < n * 3; i += 3) {
      names[i] = "list[" + (i / 3) + "].cityName";
      names[i + 1] = "list[" + (i / 3) + "].cargoId";
      names[i + 2] = "list[" + (i / 3) + "].isLoading";
      values[i] = cities[i / 3];
      values[i + 1] = Integer.toString(cargo.get(i / 5).getId());
      values[i + 2] = Boolean.valueOf((i / 3) % 2 == 0).toString();
    }
    MultiValueMap<String, String> params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", notNullValue()))
        .andExpect(flash().attribute("counter_value", notNullValue()));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB9AddNewOrderFormCycle() throws Exception {
    String[] cities = {"Kazan", "Cheboksary", "Cheboksary", "Kazan"};
    for (int i = 0; i < 2; i++) {
      Car car = new Car();
      car.setCapacityTons(12f);
      car.setShiftLength(3);
      car.setIsWorking(true);
      car.setRegistrationId(cities[i].substring(0, 2).toUpperCase() + "99999");
      car.setLocation(cityDAO.read(cities[i]));
      carDAO.add(car);
      for (UserInfo driver : cityDAO.read(cities[i]).getUserInfos()) {
        driver.setOrder(null);
        userDAO.update(driver.getUser());
      }
    }
    int n = 4;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    List<Cargo> cargo = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList());
    for (int i = 0; i < n * 3; i += 3) {
      names[i] = "list[" + (i / 3) + "].cityName";
      names[i + 1] = "list[" + (i / 3) + "].cargoId";
      names[i + 2] = "list[" + (i / 3) + "].isLoading";
      values[i] = cities[i / 3];
      values[i + 1] = Integer.toString(cargo.get(i / 5).getId());
      values[i + 2] = Boolean.valueOf((i / 3) % 2 == 0).toString();
    }
    MultiValueMap<String, String> params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", nullValue()))
        .andExpect(flash().attribute("counter_value", nullValue()));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testC1AddNewOrderWithDijkstraCalculation() throws Exception {
    int n = 2;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    String[] cities = {"Ryazan", "Penza"};
    Integer id = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList()).get(0).getId();
    for (int i = 0; i < n * 3; i += 3) {
      names[i] = "list[" + (i / 3) + "].cityName";
      names[i + 1] = "list[" + (i / 3) + "].cargoId";
      names[i + 2] = "list[" + (i / 3) + "].isLoading";
      values[i] = cities[i / 3];
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
  public void testC2AddNewOrderFullCycle() throws Exception {
    int n = 6;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    String[] cities = {"Moscow", "Yaroslavl", "Yaroslavl", "Cheboksary", "Cheboksary", "Moscow"};
    for (int i = 1; i < cities.length; i += 2) {
      Car car = new Car();
      car.setCapacityTons(12f);
      car.setShiftLength(13);
      car.setIsWorking(true);
      car.setRegistrationId(cities[i].substring(0, 2).toUpperCase() + "76543");
      car.setLocation(cityDAO.read(cities[i]));
      carDAO.add(car);
      for (UserInfo driver : cityDAO.read(cities[i]).getUserInfos()) {
        driver.setOrder(null);
        userDAO.update(driver.getUser());
      }
    }
    List<Cargo> cargo = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList());
    for (int i = 0; i < n * 3; i += 3) {
      names[i] = "list[" + (i / 3) + "].cityName";
      names[i + 1] = "list[" + (i / 3) + "].cargoId";
      names[i + 2] = "list[" + (i / 3) + "].isLoading";
      values[i] = cities[i / 3];
      values[i + 1] = Integer.toString(cargo.get(i / 6).getId());
      values[i + 2] = Boolean.valueOf((i / 3) % 2 == 0).toString();
    }
    MultiValueMap<String, String> params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", nullValue()))
        .andExpect(flash().attribute("counter_value", nullValue()));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testC3AddNewOrderPartCycle() throws Exception {
    int n = 6;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    String[] cities = {"Moscow", "Yaroslavl", "Cheboksary", "Kazan", "Kazan", "Cheboksary"};
    for (int i = 0; i < cities.length; i += 2) {
      Car car = new Car();
      car.setCapacityTons(12f);
      car.setShiftLength(13);
      car.setIsWorking(true);
      car.setRegistrationId(cities[i].substring(0, 2).toUpperCase() + "44444");
      car.setLocation(cityDAO.read(cities[i]));
      carDAO.add(car);
      for (UserInfo driver : cityDAO.read(cities[i]).getUserInfos()) {
        driver.setOrder(null);
        userDAO.update(driver.getUser());
      }
    }
    List<Cargo> cargo = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList());
    for (int i = 0; i < n * 3; i += 3) {
      names[i] = "list[" + (i / 3) + "].cityName";
      names[i + 1] = "list[" + (i / 3) + "].cargoId";
      names[i + 2] = "list[" + (i / 3) + "].isLoading";
      values[i] = cities[i / 3];
      values[i + 1] = Integer.toString(cargo.get(i / 6).getId());
      values[i + 2] = Boolean.valueOf((i / 3) % 2 == 0).toString();
    }
    MultiValueMap<String, String> params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", nullValue()))
        .andExpect(flash().attribute("counter_value", nullValue()));
    log.debug(orderDAO.readAll().get(orderDAO.readAll().size() - 1).getRoute());
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testC4AddNewOrderTwoCycles() throws Exception {
    int n = 8;
    String[] names = new String[n * 3];
    String[] values = new String[n * 3];
    String[] cities = {"Moscow", "Yaroslavl", "Yaroslavl", "Moscow", "Cheboksary", "Kazan", "Kazan",
        "Cheboksary"};
    for (int i = 1; i < cities.length; i += 2) {
      Car car = new Car();
      car.setCapacityTons(12f);
      car.setShiftLength(13);
      car.setIsWorking(true);
      car.setRegistrationId(cities[i].substring(0, 2).toUpperCase() + "92506");
      car.setLocation(cityDAO.read(cities[i]));
      carDAO.add(car);
      for (UserInfo driver : cityDAO.read(cities[i]).getUserInfos()) {
        driver.setOrder(null);
        userDAO.update(driver.getUser());
      }
    }
    List<Cargo> cargo = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList());
    for (int i = 0; i < n * 3; i += 3) {
      names[i] = "list[" + (i / 3) + "].cityName";
      names[i + 1] = "list[" + (i / 3) + "].cargoId";
      names[i + 2] = "list[" + (i / 3) + "].isLoading";
      values[i] = cities[i / 3];
      values[i + 1] = Integer.toString(cargo.get(i / 6).getId());
      values[i + 2] = Boolean.valueOf((i / 3) % 2 == 0).toString();
    }
    MultiValueMap<String, String> params = generateOrderParameters(names, values);
    mockMvc.perform(post("/addNewOrder").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/cargoAndOrders"))
        .andExpect(flash().attribute("routePoints", nullValue()))
        .andExpect(flash().attribute("counter_value", nullValue()));
    log.debug(orderDAO.read(orderDAO.readAll().size() - 1));
  }
}
