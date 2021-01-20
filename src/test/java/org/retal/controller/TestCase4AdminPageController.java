package org.retal.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.retal.config.spring.RootConfig;
import org.retal.config.spring.WebConfig;
import org.retal.dao.CarDAO;
import org.retal.dao.CityDAO;
import org.retal.dao.DAO;
import org.retal.dao.OrderDAO;
import org.retal.dao.TestCase1DAO;
import org.retal.dao.UserDAO;
import org.retal.domain.Car;
import org.retal.domain.City;
import org.retal.domain.Order;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.AuthenticatedMatcher;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, RootConfig.class})
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCase4AdminPageController {

  private MockMvc mockMvc;

  private MockHttpSession session;

  private UserDAO userDAO;

  private CarDAO carDAO;

  private CityDAO cityDAO;

  private OrderDAO orderDAO;


  @Configuration
  static class ContextConfiguration {

    @Bean
    public UserDAO getUserDAO() {
      return new UserDAO();
    }

    @Bean
    public OrderDAO getOrderDAO() {
      return new OrderDAO();
    }

    @Bean
    public CarDAO getCarDAO() {
      return new CarDAO();
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
  public void setCityDAO(CityDAO cityDAO) {
    this.cityDAO = cityDAO;
  }

  @Autowired
  public void setMockMvc(WebApplicationContext wac) {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  /**
   * Creates user account before tests for security testing.
   */
  @BeforeClass
  public static void createAdminUser() {
    User user = new User();
    user.setLogin("administrator");
    user.setPassword("password");
    user.setRole("admin");
    UserInfo userInfo = new UserInfo();
    CityDAO cityDAO = new CityDAO();
    City city = new City();
    city.setCurrentCity("Moscow");
    cityDAO.add(city);
    userInfo.setCity(city);
    userInfo.setName("admin");
    userInfo.setSurname("admin");
    user.setUserInfo(userInfo);
    new UserDAO().add(user);
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

  @Test
  @WithMockUser(username = "admin", password = "admin", authorities = "ADMIN")
  public void testA1LoadDataFromFile() throws Exception {
    mockMvc.perform(post("/addCityInfo").with(csrf())).andExpect(status().is3xxRedirection());
    mockMvc.perform(post("/addDriverInfo").with(csrf())).andExpect(status().is3xxRedirection());
    mockMvc.perform(post("/addCarsInfo").with(csrf())).andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser(username = "admin", password = "admin", authorities = "ADMIN")
  public void testA2AddNewUserForm() throws Exception {
    MultiValueMap<String, String> params = generateUserParameters();
    mockMvc.perform(post("/addNewUser").params(params).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", nullValue()));
    assertNotNull(userDAO.findUser(params.getFirst("login")));
  }

  @Test
  @WithMockUser(username = "admin", password = "admin", authorities = "ADMIN")
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
        .andExpect(flash().attribute(BindingResult.MODEL_KEY_PREFIX + "user", notNullValue()))
        .andReturn();
    assertNull(userDAO.findUser(params.getFirst("login")));
    mockMvc.perform(get(AdminPageController.ADMIN_PAGE).flashAttrs(result.getFlashMap()))
        .andExpect(status().isOk());
  }

  @Test
  public void testA4DeleteUserAssignedToOrder() throws Exception {
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
    mockMvc
        .perform(get("/deleteUser/" + id)
            .with(user("administrator").authorities(new SimpleGrantedAuthority("ADMIN"))))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("error_userDeletionFailed", notNullValue()));
    assertNotNull(userDAO.read(id));
    user.getUserInfo().setOrder(null);
    userDAO.update(user);
  }

  @Test
  public void testA5DeleteUser() throws Exception {
    Integer id = userDAO.findUser(generateUserParameters().getFirst("login")).getId();
    mockMvc
        .perform(get("/deleteUser/" + id)
            .with(user("administrator").authorities(new SimpleGrantedAuthority("ADMIN"))))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("error_userDeletionFailed", nullValue()));
    assertNull(userDAO.read(id));
  }

  @Test
  @WithMockUser(username = "admin", password = "admin", authorities = "ADMIN")
  public void testD1GetAdminPage() throws Exception {
    mockMvc.perform(get(AdminPageController.ADMIN_PAGE)).andExpect(status().isOk());
  }
}
