package org.retal.logiweb.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.retal.logiweb.config.web.RootConfig;
import org.retal.logiweb.config.web.WebConfig;
import org.retal.logiweb.dao.CarDAO;
import org.retal.logiweb.dao.CargoDAO;
import org.retal.logiweb.dao.CityDAO;
import org.retal.logiweb.dao.OrderDAO;
import org.retal.logiweb.dao.UserDAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.enums.DriverStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, RootConfig.class})
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DriverPageControllerTest {
  private MockMvc mockMvc;

  private UserDAO userDAO;

  private CarDAO carDAO;

  private OrderDAO orderDAO;

  private CargoDAO cargoDAO;

  private CityDAO cityDAO;

  private static final Logger log = Logger.getLogger(DriverPageControllerTest.class);

  private static int counter = 0;


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
  public static void fillDataBase() {
    CargoAndOrdersPageControllerTest.createManagerUserAndFillDataBase();
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

  /*
   * this is NOT a test case, this is used to create order and assign drivers/car to it for actual
   * tests
   */
  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testA0SetUpOrder() throws Exception {
    for (Car car : carDAO.readAll().stream()
        .filter(c -> c.getLocation().getCurrentCity().equals("Kazan")
            || c.getLocation().getCurrentCity().equals("Cheboksary"))
        .collect(Collectors.toList())) {
      carDAO.delete(car);
    }
    CargoAndOrdersPageControllerTest tests = new CargoAndOrdersPageControllerTest();
    tests.setCarDAO(carDAO);
    tests.setCityDAO(cityDAO);
    tests.setUserDAO(userDAO);
    tests.setCargoDAO(cargoDAO);
    tests.setMockMvc(mockMvc.getDispatcherServlet().getWebApplicationContext());
    tests.testB9AddNewOrderFormCycle();
    Order order = orderDAO.read(1);
    assertNotNull(order);
    assertEquals(carDAO.read("CH00002"), order.getCar());
    // first driver is dMaxKuz
    // second is dIlyNov
  }

  @Test
  @WithMockUser(username = "dSteBar", password = "dSteBar", authorities = "DRIVER")
  public void testA1getDriverPageWithNoOrderAssigned() throws Exception {
    mockMvc.perform(get(DriverPageController.DRIVER_PAGE)).andExpect(status().isOk())
        .andExpect(forwardedUrl("/pages/driverPage.jsp"))
        .andExpect(model().attribute("order", nullValue()));
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testA2getDriverPageWithOrderAssigned() throws Exception {
    mockMvc.perform(get(DriverPageController.DRIVER_PAGE)).andExpect(status().isOk())
        .andExpect(forwardedUrl("/pages/driverPage.jsp"))
        .andExpect(model().attribute("order", notNullValue()));
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testA3ChangeStatusToDriving() throws Exception {
    mockMvc.perform(get("/changeStatus/driving")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE));
    assertEquals(DriverStatus.DRIVING.toString().toLowerCase(),
        userDAO.findUser("dMaxKuz").getUserInfo().getStatus());
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testA4ChangeStatusIllegalArgument() throws Exception {
    mockMvc.perform(get("/changeStatus/abc123")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", notNullValue()));
    assertEquals(DriverStatus.DRIVING.toString().toLowerCase(),
        userDAO.findUser("dMaxKuz").getUserInfo().getStatus());
  }

  @Test
  @WithMockUser(username = "dIlyNov", password = "dIlyNov", authorities = "DRIVER")
  public void testA5ChangeStatusToDrivingWithTakenCar() throws Exception {
    mockMvc.perform(get("/changeStatus/driving")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", notNullValue()));
    assertEquals(DriverStatus.ON_SHIFT.toString().toLowerCase(),
        userDAO.findUser("dIlyNov").getUserInfo().getStatus());
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testA6ChangeStatusToLoadingUnloadingAndBackToDriving() throws Exception {
    mockMvc.perform(get("/changeStatus/loading_and_unloading_cargo"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE));
    assertEquals(DriverStatus.LOADING_AND_UNLOADING_CARGO.toString().toLowerCase(),
        userDAO.findUser("dMaxKuz").getUserInfo().getStatus());
    mockMvc.perform(get("/changeStatus/driving")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE));
    assertEquals(DriverStatus.DRIVING.toString().toLowerCase(),
        userDAO.findUser("dMaxKuz").getUserInfo().getStatus());
  }

  @Test
  @WithMockUser(username = "dSteBar", password = "dSteBar", authorities = "DRIVER")
  public void testA7ChangeStatusToDrivingWithNoOrderASssigned() throws Exception {
    mockMvc.perform(get("/changeStatus/driving")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE));
    assertEquals(DriverStatus.ON_SHIFT.toString().toLowerCase(),
        userDAO.findUser("dSteBar").getUserInfo().getStatus());
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testA8ChangeLocationToNextCityWithoutLoadingCargo() throws Exception {
    mockMvc.perform(get("/changeLocation")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", notNullValue()));
    assertEquals("Cheboksary",
        userDAO.findUser("dMaxKuz").getUserInfo().getCity().getCurrentCity());
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testA9LoadCargo() throws Exception {
    Integer id = orderDAO.read(1).getPoints().stream().filter(p -> p.getIsLoading())
        .filter(p -> p.getCity().getCurrentCity().equals("Cheboksary")).collect(Collectors.toList())
        .get(0).getCargo().getId();
    mockMvc.perform(get("/updateCargo/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", nullValue()));
    assertEquals("loaded", cargoDAO.read(id).getStatus());
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testB1DropCargoInTheSameCity() throws Exception {
    Integer id = orderDAO.read(1).getPoints().stream().filter(p -> p.getIsLoading())
        .filter(p -> p.getCity().getCurrentCity().equals("Cheboksary")).collect(Collectors.toList())
        .get(0).getCargo().getId();
    mockMvc.perform(get("/updateCargo/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", notNullValue()));
    assertEquals("loaded", cargoDAO.read(id).getStatus());
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testB2LoadCargoNonExistent() throws Exception {
    Integer id = 10000;
    assertNull(cargoDAO.read(id));
    mockMvc.perform(get("/updateCargo/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", notNullValue()));
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testB3LoadCargoNotAssigned() throws Exception {
    Integer id = cargoDAO.readAll().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList()).get(0).getId();
    mockMvc.perform(get("/updateCargo/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", notNullValue()));
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testB4ChangeStatusToRestingAndBack() throws Exception {
    User driver = userDAO.findUser("dMaxKuz");
    String status = driver.getUserInfo().getStatus().replace(" ", "_");
    mockMvc.perform(get("/changeStatus/resting")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE));
    assertEquals(DriverStatus.RESTING.toString().toLowerCase(),
        userDAO.findUser("dMaxKuz").getUserInfo().getStatus());
    mockMvc.perform(get("/changeStatus/" + status)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE));
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testB5ChangeLocationToNextCity() throws Exception {
    mockMvc.perform(get("/changeLocation")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", nullValue()));
    assertEquals("Kazan", userDAO.findUser("dMaxKuz").getUserInfo().getCity().getCurrentCity());
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testB6DropCargo() throws Exception {
    Integer id = orderDAO.read(1).getPoints().stream().filter(p -> !p.getIsLoading())
        .filter(p -> p.getCity().getCurrentCity().equals("Kazan")).collect(Collectors.toList())
        .get(0).getCargo().getId();
    mockMvc.perform(get("/updateCargo/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", nullValue()));
    assertEquals("unloaded", cargoDAO.read(id).getStatus());
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testB7LoadNextCargo() throws Exception {
    Integer id = orderDAO.read(1).getPoints().stream().filter(p -> p.getIsLoading())
        .filter(p -> p.getCity().getCurrentCity().equals("Kazan")).collect(Collectors.toList())
        .get(0).getCargo().getId();
    mockMvc.perform(get("/updateCargo/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", nullValue()));
    assertEquals("loaded", cargoDAO.read(id).getStatus());
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testB8ChangeLocationToNextCityShiftLimit() throws Exception {
    mockMvc.perform(get("/changeLocation")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", notNullValue()));
    assertEquals("Kazan", userDAO.findUser("dMaxKuz").getUserInfo().getCity().getCurrentCity());
  }

  @Test
  @WithMockUser(username = "dIlyNov", password = "dIlyNov", authorities = "DRIVER")
  public void testC1ChangeLocationToNextCityAnotherDriverCarWasNotFree() throws Exception {
    mockMvc.perform(get("/changeLocation")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", notNullValue()));
    assertEquals("Kazan", userDAO.findUser("dIlyNov").getUserInfo().getCity().getCurrentCity());
    assertEquals("on shift", userDAO.findUser("dIlyNov").getUserInfo().getStatus());
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testC2EndShiftForFirstDriver() throws Exception {
    mockMvc.perform(get("/changeStatus/resting")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE));
    assertEquals(DriverStatus.RESTING.toString().toLowerCase(),
        userDAO.findUser("dMaxKuz").getUserInfo().getStatus());
    assertNull(orderDAO.read(1).getCar().getDriver());
    assertNull(userDAO.findUser("dMaxKuz").getUserInfo().getOrder());
  }

  @Test
  @WithMockUser(username = "dIlyNov", password = "dIlyNov", authorities = "DRIVER")
  public void testC3ChangeLocationSecondDriver() throws Exception {
    mockMvc.perform(get("/changeLocation")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE));
    assertEquals("Cheboksary",
        userDAO.findUser("dIlyNov").getUserInfo().getCity().getCurrentCity());
  }

  @Test
  @WithMockUser(username = "dIlyNov", password = "dIlyNov", authorities = "DRIVER")
  public void testC4ChangeLocationSecondDriverRouteLimit() throws Exception {
    mockMvc.perform(get("/changeLocation")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", notNullValue()));
    assertEquals("Cheboksary",
        userDAO.findUser("dIlyNov").getUserInfo().getCity().getCurrentCity());
  }

  @Test
  @WithMockUser(username = "dIlyNov", password = "dIlyNov", authorities = "DRIVER")
  public void testC5DropCargoAndFinishOrder() throws Exception {
    Integer id = orderDAO.read(1).getPoints().stream().filter(p -> !p.getIsLoading())
        .filter(p -> p.getCity().getCurrentCity().equals("Cheboksary")).collect(Collectors.toList())
        .get(0).getCargo().getId();
    mockMvc.perform(get("/updateCargo/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "driver", nullValue()));
    assertEquals("unloaded", cargoDAO.read(id).getStatus());
    assertTrue(orderDAO.read(1).getIsCompleted());
  }

  @Test
  @WithMockUser(username = "dMaxKuz", password = "dMaxKuz", authorities = "DRIVER")
  public void testD2ChangeStatusBackToOnShift() throws Exception {
    mockMvc.perform(get("/changeStatus/on_shift")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(DriverPageController.DRIVER_PAGE));
    assertEquals(DriverStatus.ON_SHIFT.toString().toLowerCase(),
        userDAO.findUser("dMaxKuz").getUserInfo().getStatus());
  }



}
