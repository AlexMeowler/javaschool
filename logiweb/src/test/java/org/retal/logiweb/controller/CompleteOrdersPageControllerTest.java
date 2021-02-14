package org.retal.logiweb.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Random;
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
import org.retal.logiweb.dao.DAOTest;
import org.retal.logiweb.dao.impl.CarDAO;
import org.retal.logiweb.dao.impl.CargoDAO;
import org.retal.logiweb.dao.impl.CityDAO;
import org.retal.logiweb.dao.impl.CityDistanceDAO;
import org.retal.logiweb.dao.impl.CompletedOrderInfoDAO;
import org.retal.logiweb.dao.impl.OrderDAO;
import org.retal.logiweb.dao.impl.RoutePointDAO;
import org.retal.logiweb.dao.impl.UserDAO;
import org.retal.logiweb.dao.interfaces.DAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.Cargo;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.CompletedOrderInfo;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.RoutePoint;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.service.logic.impl.CityService;
import org.retal.logiweb.service.logic.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, RootConfig.class})
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CompleteOrdersPageControllerTest {

  private MockMvc mockMvc;

  private OrderDAO orderDAO;

  private CargoDAO cargoDAO;

  private RoutePointDAO routePointDAO;

  private CityDAO cityDAO;
  
  private CompletedOrderInfoDAO completedOrderInfoDAO;

  private static final Logger log = Logger.getLogger(CompleteOrdersPageControllerTest.class);

  private static int counter = 1;


  @Autowired
  public void setUserDAO(UserDAO userDAO) {
  }

  @Autowired
  public void setOrderDAO(OrderDAO orderDAO) {
    this.orderDAO = orderDAO;
  }

  @Autowired
  public void setCarDAO(CarDAO carDAO) {
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
  
  @Autowired
  public void setCompletedOrderInfoDAO(CompletedOrderInfoDAO completedOrderInfoDAO) {
    this.completedOrderInfoDAO = completedOrderInfoDAO;
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
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA1GetCompletedOrderPage() throws Exception {
    Session session = DAO.start();
    orderDAO.setSession(session);
    Order order = new Order();
    order.setCar(null);
    order.setIsCompleted(true);
    order.setRoute(DAOTest.CITY_NAMES[0] + Order.ROUTE_DELIMETER + DAOTest.CITY_NAMES[1]);
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
    final String carId = "ZZ00000";
    final String drivers = "test";
    CompletedOrderInfo info = new CompletedOrderInfo(carId, drivers, order);
    completedOrderInfoDAO.add(info);
    MvcResult result =
        mockMvc.perform(get("/completedOrders")).andExpect(status().isOk()).andReturn();
    Map<String, Object> model = result.getModelAndView().getModel();
    @SuppressWarnings("unchecked")
    Order orderModel = ((List<Order>) model.get("ordersList")).get(0);
    assertEquals(carId, orderModel.getCompletedOrderInfo().getCarId());
    assertEquals(drivers, orderModel.getCompletedOrderInfo().getDrivers());
  }
}
