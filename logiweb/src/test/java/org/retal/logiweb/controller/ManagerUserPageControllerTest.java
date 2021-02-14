package org.retal.logiweb.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.retal.logiweb.controller.AdminPageControllerTest.RunnableWithExceptionThrows;
import org.retal.logiweb.dao.DAOTest;
import org.retal.logiweb.dao.impl.CarDAO;
import org.retal.logiweb.dao.impl.CityDAO;
import org.retal.logiweb.dao.impl.CityDistanceDAO;
import org.retal.logiweb.dao.impl.OrderDAO;
import org.retal.logiweb.dao.impl.UserDAO;
import org.retal.logiweb.dao.interfaces.DAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.service.logic.impl.CarService;
import org.retal.logiweb.service.logic.impl.CityService;
import org.retal.logiweb.service.logic.impl.UserService;
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
public class ManagerUserPageControllerTest {

  private MockMvc mockMvc;

  private UserDAO userDAO;

  private CarDAO carDAO;

  private OrderDAO orderDAO;

  private static final Logger log = Logger.getLogger(ManagerUserPageControllerTest.class);

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
        .andExpect(redirectedUrl(ManagerUserPageController.MANAGER_USERS_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()))
        .andReturn();
    assertNull(userDAO.findUser(params.getFirst("login")));
    mockMvc.perform(
        get(ManagerUserPageController.MANAGER_USERS_PAGE + "/1").flashAttrs(result.getFlashMap()))
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
        .andExpect(redirectedUrl(ManagerUserPageController.MANAGER_USERS_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()))
        .andReturn();
    assertNull(userDAO.findUser(params.getFirst("login")));
    mockMvc.perform(
        get(ManagerUserPageController.MANAGER_USERS_PAGE + "/1").flashAttrs(result.getFlashMap()))
        .andExpect(status().isOk());
    params = generateUserParameters();
    params.set("login", "<script>alert(\"hi\")</script>");
    params.set("name", "WHERE id = 1");
    params.set("surname", "DROP TABLE USERS");
    params.set("currentCity", "Abc");
    params.set("password", "");
    params.set("status", "flexing");
    result = mockMvc.perform(post("/addNewDriver").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ManagerUserPageController.MANAGER_USERS_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()))
        .andReturn();
    assertNull(userDAO.findUser(params.getFirst("login")));
    mockMvc.perform(
        get(ManagerUserPageController.MANAGER_USERS_PAGE + "/1").flashAttrs(result.getFlashMap()))
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
    order.setRoute(DAOTest.CITY_NAMES[0] + Order.ROUTE_DELIMETER + DAOTest.CITY_NAMES[1]);
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
        .andExpect(redirectedUrl(ManagerUserPageController.MANAGER_USERS_PAGE))
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
  public void testB4GetHomePage() throws Exception {
    mockMvc.perform(get(ManagerUserPageController.MANAGER_USERS_PAGE))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(ManagerUserPageController.MANAGER_USERS_PAGE + "/1"));
  }

  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB5GetHomePageBigNumber() throws Exception {
    String page = "1000";
    MvcResult result =
        mockMvc.perform(get(ManagerUserPageController.MANAGER_USERS_PAGE + "/" + page))
            .andExpect(status().is3xxRedirection()).andReturn();
    assertNotEquals(
        result.getResponse().getRedirectedUrl()
            .substring(ManagerUserPageController.MANAGER_USERS_PAGE.length()).replace("/", ""),
        page);
  }
  
  @Test
  @WithMockUser(username = "manager", password = "manager", authorities = "MANAGER")
  public void testB5GetHomePageNegativeNumber() throws Exception {
    String page = "-10";
    MvcResult result =
        mockMvc.perform(get(ManagerUserPageController.MANAGER_USERS_PAGE + "/" + page))
            .andExpect(status().is3xxRedirection()).andReturn();
    assertNotEquals(
        result.getResponse().getRedirectedUrl()
            .substring(ManagerUserPageController.MANAGER_USERS_PAGE.length()).replace("/", ""),
        page);
  }
}
