package org.retal.logiweb.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.retal.logiweb.config.spring.app.hibernate.HibernateSessionFactory;
import org.retal.logiweb.config.spring.web.RootConfig;
import org.retal.logiweb.config.spring.web.WebConfig;
import org.retal.logiweb.controller.AdminPageControllerTest;
import org.retal.logiweb.dao.impl.CarDAO;
import org.retal.logiweb.dao.impl.CargoDAO;
import org.retal.logiweb.dao.impl.CityDAO;
import org.retal.logiweb.dao.impl.CityDistanceDAO;
import org.retal.logiweb.dao.impl.CompletedOrderInfoDAO;
import org.retal.logiweb.dao.impl.OrderDAO;
import org.retal.logiweb.dao.impl.OrderRouteProgressionDAO;
import org.retal.logiweb.dao.impl.RoutePointDAO;
import org.retal.logiweb.dao.impl.UserDAO;
import org.retal.logiweb.dao.interfaces.DAO;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.Cargo;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.CityDistance;
import org.retal.logiweb.domain.entity.CompletedOrderInfo;
import org.retal.logiweb.domain.entity.MethodUndefinedException;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.OrderRouteProgression;
import org.retal.logiweb.domain.entity.RoutePoint;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
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

  private CompletedOrderInfoDAO completedOrderInfoDAO;

  private CityDistanceDAO cityDistanceDAO;

  private CargoDAO cargoDAO;

  private OrderDAO orderDAO;

  private RoutePointDAO routePointDAO;

  public static final String[] CITY_NAMES =
      {"Moscow", "Yaroslavl", "Omsk", "Samara", "Cheboksary", "Chelyabinsk"};

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

  @Autowired
  public void setCompletedOrderInfoDAO(CompletedOrderInfoDAO completedOrderInfoDAO) {
    this.completedOrderInfoDAO = completedOrderInfoDAO;
  }

  @Autowired
  public void setCityDistanceDAO(CityDistanceDAO cityDistanceDAO) {
    this.cityDistanceDAO = cityDistanceDAO;
  }

  @Autowired
  public void setCargoDAO(CargoDAO cargoDAO) {
    this.cargoDAO = cargoDAO;
  }

  @Autowired
  public void setOrderDAO(OrderDAO orderDAO) {
    this.orderDAO = orderDAO;
  }

  @Autowired
  public void setRoutePointDAO(RoutePointDAO routePointDAO) {
    this.routePointDAO = routePointDAO;
  }

  /**
   * Cleaning DB after all tests are performed.
   */
  @AfterClass
  public static void cleanup() {
    AdminPageControllerTest.cleanup();
  }

  @Test
  public void test1CityWritingAndReading() {
    for (String name : CITY_NAMES) {
      City city = new City();
      city.setCurrentCity(name);
      cityDAO.add(city);
    }
    City readCity = cityDAO.read(CITY_NAMES[0]);
    assertEquals(CITY_NAMES[0], readCity.getCurrentCity());
  }

  @Test(expected = MethodUndefinedException.class)
  public void test2CityDeletion() {
    City city = cityDAO.read(CITY_NAMES[1]);
    cityDAO.delete(city);
  }

  @Test(expected = MethodUndefinedException.class)
  public void test3CityUpdate() {
    City city = cityDAO.read(CITY_NAMES[1]);
    cityDAO.update(city);
  }

  @Test
  public void test4CityReadAll() {
    List<City> cities = cityDAO.readAll();
    for (String name : CITY_NAMES) {
      assertEquals(1, cities.stream().filter(c -> c.getCurrentCity().equals(name)).count());
    }
  }

  @Test
  public void test5UserWriteAndRead() {
    User user = new User();
    UserInfo userInfo = new UserInfo();
    City city = cityDAO.read(CITY_NAMES[0]);
    assertNotNull(city);
    userInfo.setCity(city);
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
    assertEquals(1, users.size());
  }

  @Test
  public void testA3UserFindByName() {
    User user = userDAO.findUser("admin");
    assertNotNull(user);
  }

  @Test
  public void testA4UserGetAllWithRole() {
    List<User> admins = userDAO.readAllWithRole("admin");
    assertEquals(1, admins.size());
  }

  @Test
  public void testA40UserRowsCount() {
    assertEquals(0, userDAO.getRowsAmount());
  }

  @Test
  public void testA41UserPartRead() {
    assertEquals(0, userDAO.readRows(0, 1).size());
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
    String[] ids = {"AB12121", "CD19532"};
    Car car;
    for (String id : ids) {
      car = new Car();
      car.setRegistrationId(id);
      car.setLocation(cityDAO.read(CITY_NAMES[0]));
      car.setCapacityTons(2f);
      car.setShiftLength(13);
      carDAO.add(car);
    }
    car = null;
    car = carDAO.read(ids[0]);
    assertNotNull(car);
    assertEquals(carDAO.readAll().size(), ids.length);
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

  @Test
  public void testB00CarGetRows() {
    assertEquals(carDAO.readAll().size(), carDAO.getRowsAmount());
  }

  @Test
  public void testB0CarReadPart() {
    assertEquals(carDAO.readAll().get(1), carDAO.readRows(1, 1).get(0));
  }

  @Test(expected = MethodUndefinedException.class)
  public void testB1TransactionNotBegunAndReadAllDefaultMethod() {
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();
    transaction.commit();
    DAO.end(session);
    orderRouteProgressionDAO.readAll(); // testing default readAll() method of DAO interface
  }

  @Test
  public void testB2CheckMaliciousInputAndNoUserFound() {
    String[] input = {"DROP TABLE users", "<script>alert(\"haha\")</script>"};
    assertNull(userDAO.findUser(input[0]));
    assertNull(userDAO.findUser(input[1]));
    assertNull(userDAO.readAllWithRole(input[0]));
    assertNull(userDAO.readAllWithRole(input[1]));
    assertNull(userDAO.findUser("xd"));
  }

  @Test
  public void testB3UpdateAndDeleteCar() {
    Car car = carDAO.readAll().get(0);
    car.setLocation(cityDAO.read(CITY_NAMES[1]));
    carDAO.update(car);
    assertEquals(car.getLocation(), carDAO.read(car.getRegistrationId()).getLocation());
    carDAO.delete(car);
    assertNull(carDAO.read(car.getRegistrationId()));
  }

  @Test
  public void testB4addAndReadCityDistances() {
    CityDistance cd = new CityDistance();
    cd.setCityA(CITY_NAMES[0]);
    cd.setCityB(CITY_NAMES[1]);
    cd.setDistance(100);
    cityDistanceDAO.add(cd);
    int distance = cityDistanceDAO.read(CITY_NAMES[0], CITY_NAMES[1]).getDistance();
    assertEquals(100, distance);
    assertEquals(1, cityDistanceDAO.readAll().size());
  }

  @Test(expected = MethodUndefinedException.class)
  public void testB5UpdateError() {
    CityDistance cd = cityDistanceDAO.read(CITY_NAMES[0], CITY_NAMES[1]);
    cd.setDistance(200);
    cityDistanceDAO.update(cd);
  }

  @Test(expected = MethodUndefinedException.class)
  public void testB6DeleteError() {
    CityDistance cd = cityDistanceDAO.read(CITY_NAMES[0], CITY_NAMES[1]);
    cityDistanceDAO.delete(cd);
  }

  @Test
  public void testB7CargoAddAndReadAndReadAll() {
    int n = 2;
    for (int i = 0; i < n; i++) {
      Cargo cargo = new Cargo();
      cargo.setDescription("d" + i);
      cargo.setName("n" + i);
      cargo.setStatus("prepared");
      cargo.setMass(100 * (i + 1));
      cargoDAO.add(cargo);
    }
    int x = 0;
    List<Cargo> list = cargoDAO.readAll();
    Cargo readCargo = cargoDAO.read(x + 1);
    assertEquals(list.get(x), readCargo);
    assertEquals(list.size(), n);
  }

  @Test
  public void testB8CargoUpdate() {
    int id = 1;
    String status = "loaded";
    Cargo cargo = cargoDAO.read(id);
    cargo.setStatus(status);
    cargoDAO.update(cargo);
    assertEquals(cargoDAO.read(id).getStatus(), status);
    cargo.setStatus("prepared");
    cargoDAO.update(cargo);
  }

  @Test(expected = MethodUndefinedException.class)
  public void testB9CargoDelete() {
    cargoDAO.delete(cargoDAO.read(1));
  }

  @Test
  public void testC1OrderAddAndRead() {
    Session session = DAO.start();
    orderDAO.setSession(session);
    Order order = new Order();
    order.setCar(carDAO.readAll().get(0));
    order.setIsCompleted(false);
    order.setRoute(CITY_NAMES[0] + Order.ROUTE_DELIMETER + CITY_NAMES[1]);
    order.setRequiredCapacity(cargoDAO.read(1).getMass().floatValue());
    order.setRequiredShiftLength(3);
    orderDAO.add(order);
    DAO.end(session);
    orderDAO.setSession(null);
    assertEquals(order.getRoute(), orderDAO.read(1).getRoute());
  }

  @Test(expected = NullPointerException.class)
  public void testC2OrderSessionNotSetError() {
    Order order = new Order();
    orderDAO.add(order);
  }

  @Test(expected = MethodUndefinedException.class)
  public void testC3OrderDeleteError() {
    orderDAO.delete(orderDAO.read(1));
  }

  // route point, orderprogression
  @Test
  public void testC4RoutePointsForOrder() {
    Session session = DAO.start();
    routePointDAO.setSession(session);
    Order order = orderDAO.read(1);
    String[] route = order.getRoute().split(Order.ROUTE_DELIMETER);
    for (int i = 0; i < route.length; i++) {
      RoutePoint rp = new RoutePoint();
      rp.setCity(cityDAO.read(route[i]));
      rp.setCargo(cargoDAO.read(i + 1));
      rp.setIsLoading(i % 2 == 0);
      rp.setOrder(order);
      routePointDAO.add(rp);
    }
    routePointDAO.setSession(null);
    DAO.end(session);
    assertNotEquals(orderDAO.read(1).getPoints().size(), 0);
  }

  @Test(expected = NullPointerException.class)
  public void testC5RoutePointsSessionNull() {
    RoutePoint rp = new RoutePoint();
    routePointDAO.add(rp);
  }

  @Test(expected = MethodUndefinedException.class)
  public void testC6RoutePointsRead() {
    routePointDAO.read(1);
  }

  @Test(expected = MethodUndefinedException.class)
  public void testC7RoutePointsDelete() {
    RoutePoint rp = new RoutePoint();
    rp.setId(1);
    routePointDAO.delete(rp);
  }

  @Test(expected = MethodUndefinedException.class)
  public void testC8RoutePointsUpdate() {
    RoutePoint rp = new RoutePoint();
    rp.setId(1);
    routePointDAO.update(rp);
  }

  @Test
  public void testC9OrderNotFound() {
    assertNull(orderDAO.read(10));
  }

  @Test
  public void testD1OrderReadAll() {
    List<Order> orders = orderDAO.readAll();
    assertEquals(1, orders.size());
    assertNotEquals(orders.get(0).getPoints().size(), 0);
    assertNotEquals(orders.get(0).getCargo().size(), 0);
  }

  @Test
  public void testD2OrderUpdate() {
    Order order = orderDAO.read(1);
    order.setIsCompleted(true);
    Session session = DAO.start();
    orderDAO.setSession(session);
    orderDAO.update(order);
    orderDAO.setSession(null);
    DAO.end(session);
    assertTrue(orderDAO.read(1).getIsCompleted());
  }

  @Test(expected = NullPointerException.class)
  public void testD3OrderUpdateSessionNull() {
    Order order = orderDAO.read(1);
    order.setIsCompleted(false);
    orderDAO.update(order);
  }

  @Test
  public void testD4addAndReadRouteProgressionForOrder() {
    OrderRouteProgression orp = new OrderRouteProgression();
    orp.setOrder(orderDAO.readAll().get(0));
    orp.setRouteCounter(0);
    orderRouteProgressionDAO.add(orp);
    assertEquals(orderRouteProgressionDAO.read(1), orp);
  }

  @Test
  public void testD5UpdateAndDeleteRouteProgression() {
    OrderRouteProgression orp = orderRouteProgressionDAO.read(1);
    orp.incrementCounter();
    orderRouteProgressionDAO.update(orp);
    assertEquals(orderRouteProgressionDAO.read(1).getRouteCounter(), Integer.valueOf(1));
    orderRouteProgressionDAO.delete(orp);
    assertNull(orderRouteProgressionDAO.read(1));
  }

  @Test
  public void testD6CompletedOrderAddAndRead() {
    Order order = orderDAO.readAll().get(0);
    completedOrderInfoDAO
        .add(new CompletedOrderInfo(order.getCar().getRegistrationId(), "test", order));
    assertEquals(orderDAO.readAll().get(0).getCompletedOrderInfo(),
        completedOrderInfoDAO.read(order.getId()));
  }

  @Test(expected = MethodUndefinedException.class)
  public void testD7DeleteCompletedOrderInfo() {
    completedOrderInfoDAO.delete(completedOrderInfoDAO.read(1));
  }

  @Test(expected = MethodUndefinedException.class)
  public void testD8UpdateCompletedOrderInfo() {
    CompletedOrderInfo info = completedOrderInfoDAO.read(1);
    info.setCarId("ZZ00000");
    completedOrderInfoDAO.update(info);
  }
}
