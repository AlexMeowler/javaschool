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

import java.util.List;
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
import org.retal.logiweb.config.web.RootConfig;
import org.retal.logiweb.config.web.WebConfig;
import org.retal.logiweb.dao.CarDAO;
import org.retal.logiweb.dao.CargoDAO;
import org.retal.logiweb.dao.CityDAO;
import org.retal.logiweb.dao.DAO;
import org.retal.logiweb.dao.DAOTest;
import org.retal.logiweb.dao.OrderDAO;
import org.retal.logiweb.dao.UserDAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
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
public class AdminPageControllerTest {

  private MockMvc mockMvc;

  private UserDAO userDAO;

  private CarDAO carDAO;

  private OrderDAO orderDAO;

  private CargoDAO cargoDAO;

  private static final Logger log = Logger.getLogger(AdminPageControllerTest.class);

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
  public void setMockMvc(WebApplicationContext wac) {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  /**
   * Creates user admin and driver account before tests for security testing.
   */
  @BeforeClass
  public static void createAdminAndDriverUsers() {
    User user = generateUser("administrator", "password", "admin", "Moscow", "admin", "admin");
    UserDAO userDAO = new UserDAO();
    userDAO.add(user);
    user = generateUser("driver123", "password", "driver", "Moscow", "driver", "driver");
    userDAO.add(user);
  }

  private static User generateUser(String login, String password, String role, String cityName,
      String name, String surname) {
    User user = new User();
    user.setLogin(login);
    user.setPassword(password);
    user.setRole(role);
    UserInfo userInfo = new UserInfo();
    City city = new City();
    city.setCurrentCity(cityName);
    new CityDAO().add(city);
    userInfo.setCity(city);
    userInfo.setName(name);
    userInfo.setSurname(surname);
    user.setUserInfo(userInfo);
    return user;
  }

  /**
   * Cleaning DB after all tests are performed.
   */
  @SuppressWarnings("unchecked")
  @AfterClass
  public static void cleanup() {
    Session session = DAO.start();
    session.createSQLQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
    List<String> tables = session
        .createNativeQuery(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES  " + "where TABLE_SCHEMA='PUBLIC'")
        .getResultList();
    for (String table : tables) {
      session.createSQLQuery("TRUNCATE TABLE " + table).executeUpdate();
    }
    List<String> sequences =
        session.createNativeQuery("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE "
            + "SEQUENCE_SCHEMA='PUBLIC'").getResultList();
    for (String seq : sequences) {
      session.createSQLQuery("ALTER SEQUENCE " + seq + " RESTART WITH 1").executeUpdate();
    }
    session.createSQLQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    DAO.end(session);
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
    params.add("role", "admin");
    params.add("name", "testname");
    params.add("surname", "testsurname");
    params.add("status", "resting");
    params.add("currentCity", "Moscow");
    return params;
  }

  private MultiValueMap<String, String> generateCargoParameters() {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("name", "cargotestname");
    params.add("mass", "380");
    params.add("description", "testdescription");
    return params;
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testA1LoadDataFromFiles() throws Exception {
    mockMvc.perform(post("/addCityInfo").with(csrf())).andExpect(status().is3xxRedirection());
    mockMvc.perform(post("/addDriverInfo").with(csrf())).andExpect(status().is3xxRedirection());
    mockMvc.perform(post("/addCarsInfo").with(csrf())).andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testA2AddNewUserForm() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    mockMvc.perform(post("/addNewUser").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", nullValue()));
    assertNotNull(userDAO.findUser(params.getFirst("login")));
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testA3AddNewUserFormValidationFailAndRedirectToAdminPageWithFlashAttributes()
      throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    params.set("login", "a");
    params.set("name", "bb123");
    params.set("surname", "dd123");
    params.set("currentCity", "Abc");
    params.set("password", "a1");
    params.set("status", "flexing");
    MvcResult result = mockMvc.perform(post("/addNewUser").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(AdminPageController.ADMIN_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()))
        .andReturn();
    assertNull(userDAO.findUser(params.getFirst("login")));
    mockMvc.perform(get(AdminPageController.ADMIN_PAGE).flashAttrs(result.getFlashMap()))
        .andExpect(status().isOk());
    params = generateUserParameters();
    params.set("login", "a");
    params.set("name", "");
    params.set("surname", "DROP TABLE USERS");
    params.set("currentCity", "Abc");
    params.set("password", "");
    params.set("status", "flexing");
    result = mockMvc.perform(post("/addNewUser").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(AdminPageController.ADMIN_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()))
        .andReturn();
    assertNull(userDAO.findUser(params.getFirst("login")));
    mockMvc.perform(get(AdminPageController.ADMIN_PAGE).flashAttrs(result.getFlashMap()))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testA4DeleteUserAssignedToOrderOrCar() throws Exception {
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
      mockMvc.perform(get("/deleteUser/" + id)).andExpect(status().is3xxRedirection())
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
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testA5AddExistingUser() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    mockMvc.perform(post("/addNewUser").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()));
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testA6EditUser() throws Exception {
    Integer id = userDAO.findUser(generateUserParameters().getFirst("login")).getId();
    MvcResult result = mockMvc.perform(get("/editUser/" + id))
        .andExpect(status().is3xxRedirection()).andExpect(flash().attribute("user", notNullValue()))
        .andExpect(flash().attribute("we", notNullValue())).andReturn();
    mockMvc.perform(get("/editUser").flashAttrs(result.getFlashMap())).andExpect(status().isOk())
        .andExpect(forwardedUrl("/pages/editUser.jsp"));
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testA7SubmitEditedUserWithEmptyassword() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    String name = "changedName";
    params.set("name", name);
    params.set("login", "changedLogin");
    params.set("password", "");
    params.set("id",
        Integer.toString(userDAO.findUser(generateUserParameters().getFirst("login")).getId()));
    mockMvc.perform(post("/submitEditedUser").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(AdminPageController.ADMIN_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", nullValue()));
    User user = userDAO.findUser(params.getFirst("login"));
    assertEquals(user.getUserInfo().getName(), name);
    user.setLogin(generateUserParameters().getFirst("login"));
    userDAO.update(user);
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testA8SubmitEditedUserAssignedToOrderOrCar() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    Order order = orderDAO.readAll().get(0);
    User user = userDAO.findUser(params.getFirst("login"));
    user.getUserInfo().setOrder(order);
    userDAO.update(user);
    String name = "changedAgainName";
    params.set("name", name);
    params.set("id", Integer.toString(userDAO.findUser(params.getFirst("login")).getId()));
    RunnableWithExceptionThrows r = () -> {
      mockMvc.perform(post("/submitEditedUser").params(params).with(csrf()))
          .andExpect(redirectedUrl("/editUser")).andExpect(status().is3xxRedirection())
          .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()))
          .andExpect(redirectedUrl("/editUser"));
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
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testA9SubmitEditedUserValidationFail() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    String name = "ab2";
    params.set("name", name);
    params.set("login", userDAO.readAll().get(0).getLogin());
    params.set("id", Integer.toString(userDAO.findUser(params.getFirst("login")).getId()));
    mockMvc.perform(post("/submitEditedUser").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/editUser"))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()));
    assertNotEquals(userDAO.findUser(params.getFirst("login")).getUserInfo().getName(), name);
  }

  @Test
  @WithMockUser(username = "driver123", password = "password", authorities = "DRIVER")
  public void testB0ForgeAddress() throws Exception {
    Integer id = userDAO.findUser("administrator").getId();
    mockMvc.perform(get("/deleteUser/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/403/driver123"));
  }

  @Test
  @WithMockUser(username = "driver123", password = "password", authorities = "DRIVER")
  public void testB1ForgeEditUserAddress() throws Exception {
    Integer id = userDAO.findUser("administrator").getId();
    mockMvc.perform(get("/editUser/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/403/driver123"));
  }

  @Test
  @WithMockUser(username = "driver123", password = "password", authorities = "DRIVER")
  public void testB2SubmitEditedUserPermissionsFail() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    String name = "ab2";
    params.set("name", name);
    params.set("id", Integer.toString(userDAO.findUser("administrator").getId()));
    mockMvc.perform(post("/submitEditedUser").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/403/driver123"))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()));
    assertNotEquals(userDAO.findUser(params.getFirst("login")).getUserInfo().getName(), name);
  }
  
  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testB3SubmitEditedUserWithChangedPassword() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    String name = "changedName";
    params.set("name", name);
    params.set("login", "changedLogin");
    params.set("password", "123123123");
    params.set("id",
        Integer.toString(userDAO.findUser(generateUserParameters().getFirst("login")).getId()));
    mockMvc.perform(post("/submitEditedUser").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(AdminPageController.ADMIN_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", nullValue()));
    User user = userDAO.findUser(params.getFirst("login"));
    assertEquals(user.getUserInfo().getName(), name);
    user.setLogin(generateUserParameters().getFirst("login"));
    user.setPassword(generateUserParameters().getFirst("password"));
    userDAO.update(user);
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testB4DeleteUser() throws Exception {
    Integer id = userDAO.findUser(generateUserParameters().getFirst("login")).getId();
    mockMvc.perform(get("/deleteUser/" + id)).andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("error_userDeletionFailed", nullValue()));
    assertNull(userDAO.read(id));
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testB5AddNewCargoForm() throws Exception {
    MultiValueMap<String, String> params = generateCargoParameters();
    mockMvc.perform(post("/addNewCargo").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "cargo", nullValue()));
    assertEquals(1, cargoDAO.readAll().size());
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testB6AddNewCargoFormValidationFailAndRedirectToAdminPageWithFlashAttributes()
      throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    params.set("name", "");
    params.set("mass", "-13");
    params.set("description", "DROP ALL OBJECTS");
    MvcResult result = mockMvc.perform(post("/addNewCargo").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(AdminPageController.ADMIN_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "cargo", notNullValue()))
        .andReturn();
    assertEquals(1, cargoDAO.readAll().size());
    mockMvc.perform(get(AdminPageController.ADMIN_PAGE).flashAttrs(result.getFlashMap()))
        .andExpect(status().isOk());
    params = generateUserParameters();
    params.set("name", "");
    params.set("mass", "22.8");
    params.set("description", "DROP ALL OBJECTS");
    result = mockMvc.perform(post("/addNewCargo").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(AdminPageController.ADMIN_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "cargo", notNullValue()))
        .andReturn();
    assertEquals(1, cargoDAO.readAll().size());
    params.set("name", "<script>alert(\"abc\")</script>");
    result = mockMvc.perform(post("/addNewCargo").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(AdminPageController.ADMIN_PAGE))
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "cargo", notNullValue()))
        .andReturn();
    assertEquals(1, cargoDAO.readAll().size());
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testB7DeleteNotExistingUser() throws Exception {
    mockMvc.perform(get("/deleteUser/1000")).andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testB8GetAdminPage() throws Exception {
    mockMvc.perform(get(AdminPageController.ADMIN_PAGE)).andExpect(status().isOk());
  }

  @FunctionalInterface
  public interface RunnableWithExceptionThrows {
    public void run() throws Exception;
  }
}
