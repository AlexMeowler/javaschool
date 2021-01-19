package org.retal.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.retal.config.spring.RootConfig;
import org.retal.config.spring.WebConfig;
import org.retal.domain.Car;
import org.retal.domain.City;
import org.retal.domain.HibernateSessionFactory;
import org.retal.domain.MethodUndefinedException;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, RootConfig.class})
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DAOTest {

  private UserDAO userDAO;

  private CityDAO cityDAO;

  private CarDAO carDAO;
  
  private OrderRouteProgressionDAO orderRouteProgressionDAO;

  private static final String[] cityNames =
      {"Mowcow", "Omsk", "Samara", "Yaroslavl", "Cheboksary", "Chelyabinsk"};
  

  @Configuration
  static class ContextConfiguration {

    @Bean
    public UserDAO getUserDAO() {
      return new UserDAO();
    }

    @Bean
    public CityDAO getCityDAO() {
      return new CityDAO();
    }

    @Bean
    public CarDAO getCarDAO() {
      return new CarDAO();
    }
    
    @Bean
    public OrderRouteProgressionDAO getOrderRouteProgressionDAO() {
      return new OrderRouteProgressionDAO();
    }
  }
  
  @Autowired
  public void setUserDAO(UserDAO userDAO) {
    this.userDAO = userDAO;
  }
  
  @Autowired
  public void setCityDAO(CityDAO cityDAO) {
    this.cityDAO = cityDAO;
  }
  
  @Autowired
  public void setCarDAO(CarDAO carDAO) {
    this.carDAO = carDAO;
  }
  
  @Autowired
  public void setOrderRouteProgressionDAO(OrderRouteProgressionDAO orderRouteProgressionDAO) {
    this.orderRouteProgressionDAO = orderRouteProgressionDAO;
  }

  @Test
  public void test1CityWritingAndReading() {
    for (String name : cityNames) {
      City city = new City();
      city.setCurrentCity(name);
      cityDAO.add(city);
    }
    City readCity = cityDAO.read(cityNames[0]);
    assertEquals(cityNames[0], readCity.getCurrentCity());
  }

  @Test(expected = MethodUndefinedException.class)
  public void test2CityDeletion() {
    City city = cityDAO.read(cityNames[1]);
    cityDAO.delete(city);
  }

  @Test(expected = MethodUndefinedException.class)
  public void test3CityUpdate() {
    City city = cityDAO.read(cityNames[1]);
    cityDAO.update(city);
  }

  @Test
  public void test4CityReadAll() {
    List<City> cities = cityDAO.readAll();
    for (String name : cityNames) {
      assertTrue(cities.stream().filter(c -> c.getCurrentCity().equals(name)).count() == 1);
    }
  }

  @Test
  public void test5UserWriteAndRead() {
    User user = new User();
    UserInfo userInfo = new UserInfo();
    City city = cityDAO.read(cityNames[0]);
    assertNotNull(city);
    userInfo.setCity(city);
    user.setId(1);
    user.setLogin("admin");
    user.setUserInfo(userInfo);
    user.setRole("admin");
    userDAO.add(user);
    User readUser = userDAO.read(Integer.valueOf(1));
    assertEquals("admin", readUser.getLogin());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test6CityWrongPKRead() {
    cityDAO.read(25.8);
  }

  @Test
  public void test7CityNotFound() {
    assertEquals(null, cityDAO.read("ABC"));
  }

  @Test
  public void test8UserNotFound() {
    assertNull(userDAO.read(Integer.valueOf(100)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test9UserWrongPK() {
    userDAO.read("1");
  }

  @Test
  public void testA1UserUpdate() {
    User user = userDAO.read(Integer.valueOf(1));
    user.getUserInfo().setName("Alex");
    userDAO.update(user);
    user = userDAO.read(Integer.valueOf(1));
    assertEquals("Alex", user.getUserInfo().getName());
  }

  @Test
  public void testA2UserUpdate() {
    List<User> users = userDAO.readAll();
    assertTrue(users.size() == 1);
  }

  @Test
  public void testA3UserFindByName() {
    User user = userDAO.findUser("admin");
    assertNotNull(user);
  }

  @Test
  public void testA4UserGetAllWithRole() {
    List<User> admins = userDAO.readAllWithRole("admin");
    assertTrue(admins.size() == 1);
  }

  @Test
  public void testA5UserDelete() {
    User user = userDAO.read(Integer.valueOf(1));
    userDAO.delete(user);
    user = userDAO.read(Integer.valueOf(1));
    assertNull(user);
  }

  @Test
  public void testA6CarAddAndRead() {
    Car car = new Car();
    String id = "AB12121";
    car.setRegistrationId(id);
    car.setLocation(cityDAO.read(cityNames[0]));
    carDAO.add(car);
    car = null;
    car = carDAO.read(id);
    assertNotNull(car);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testA7CarReadError() {
    carDAO.read("AB12121", "12");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testA8CarReadError() {
    carDAO.read("AB12121", Integer.valueOf(4));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testA9CarReadError() {
    carDAO.read(Integer.valueOf(4));
  }
  
  @Test(expected = MethodUndefinedException.class)
  public void testB1TransactionNotBegunAndReadAllDefaultMethod() {
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();
    transaction.commit();
    DAO.end(session);
    orderRouteProgressionDAO.readAll();
  }
  
  @Test
  public void testB2FindUserMaliciousInput() {
    assertNull(userDAO.findUser("DROP TABLE users"));
    assertNull(userDAO.findUser("<script>alert(\"haha\")</script>"));
  }
}
