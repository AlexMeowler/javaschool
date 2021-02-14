package org.retal.logiweb.service;

import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.payload;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import javax.xml.transform.Source;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.retal.logiweb.config.spring.app.hibernate.HibernateSessionFactory;
import org.retal.logiweb.controller.AdminPageControllerTest;
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
import org.retal.logiweb.domain.entity.CityDistance;
import org.retal.logiweb.domain.entity.CompletedOrderInfo;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.RoutePoint;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.domain.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"/config/spring-context.xml", "/config/spring-ws-servlet.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WebServicesTest {

  private MockWebServiceClient mockClient;

  /**
   * Fills DB with some orders, drivers and cars for providing info about.
   */
  @BeforeClass
  public static void startup() {
    CityDAO cityDAO = new CityDAO();
    cityDAO.add(new City("Moscow"));
    cityDAO.add(new City("Yaroslavl"));
    CityDistance cityDistance = new CityDistance();
    cityDistance.setCityA("Moscow");
    cityDistance.setCityB("Yaroslavl");
    cityDistance.setDistance(249);
    new CityDistanceDAO().add(cityDistance);
    CarDAO carDAO = new CarDAO();
    carDAO.add(new Car("AB27283", 20, 10f, true, cityDAO.read("Moscow")));
    Cargo cargo = new Cargo();
    cargo.setMass(300);
    cargo.setName("X");
    cargo.setStatus("prepared");
    cargo.setDescription("Y");
    CargoDAO cargoDAO = new CargoDAO();
    cargoDAO.add(cargo);
    Set<RoutePoint> points = new HashSet<>();
    points.add(new RoutePoint(cityDAO.read("Moscow"), true, cargoDAO.readAll().get(0), null));
    points.add(new RoutePoint(cityDAO.read("Yaroslavl"), false, cargoDAO.readAll().get(0), null));
    Session session = HibernateSessionFactory.getSessionFactory().openSession();
    OrderDAO orderDAO = new OrderDAO();
    Transaction transaction = session.beginTransaction();
    orderDAO.setSession(session);
    Order order = new Order(false, carDAO.read("AB27283"), null,
        "Moscow" + Order.ROUTE_DELIMETER + "Yaroslavl", 0.3f, 3);
    orderDAO.add(order);
    transaction.commit();
    transaction = session.beginTransaction();
    RoutePointDAO routePointDAO = new RoutePointDAO();
    routePointDAO.setSession(session);
    for (RoutePoint rp : points) {
      rp.setOrder(order);
      routePointDAO.add(rp);
    }
    transaction.commit();
    orderDAO.setSession(null);
    routePointDAO.setSession(null);
    session.close();
    UserDAO userDAO = new UserDAO();
    userDAO.add(new User("driver", "driver", UserRole.DRIVER.toString().toLowerCase(),
        new UserInfo("drivername", "driversurname", "on shift", cityDAO.read("Moscow"))));
    User user = userDAO.findUser("driver");
    user.getUserInfo().setOrder(order);
    userDAO.update(user);
  }

  @AfterClass
  public static void cleanup() {
    AdminPageControllerTest.cleanup();
  }

  /**
   * Autowired setter for mocking webservice client.
   */
  @Autowired
  public void setMockClient(ApplicationContext applicationContext) {
    mockClient = MockWebServiceClient.createClient(applicationContext);
    GenericApplicationContext ctx = (GenericApplicationContext) applicationContext;
    final XmlBeanDefinitionReader definitionReader = new XmlBeanDefinitionReader(ctx);
    definitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
    definitionReader.setNamespaceAware(true);
  }

  private String getBody(String path) throws Exception {
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(path)));
    String line;
    String body = "";
    while ((line = reader.readLine()) != null) {
      body += line;
    }
    reader.close();
    return body;
  }

  @Test
  public void testA1OrderEndpoint() throws Exception {
    Source requestPayload = new StringSource(getBody("/web/getOrdersReq.txt"));
    Source responsePayload = new StringSource(getBody("/web/getOrdersResp.txt"));
    mockClient.sendRequest(withPayload(requestPayload)).andExpect(payload(responsePayload));
  }
  
  @Test
  public void testA2CompletedOrderEndpoint() throws Exception {
    OrderDAO orderDAO = new OrderDAO();
    Order order = orderDAO.readAll().get(0);
    Session session = DAO.start();
    order.setCar(null);
    order.setIsCompleted(true);
    orderDAO.setSession(session);
    orderDAO.update(order);
    DAO.end(session);
    orderDAO.setSession(null);
    UserDAO userDAO = new UserDAO();
    User user = userDAO.readAll().get(0);
    user.getUserInfo().setOrder(null);
    userDAO.update(user);
    CompletedOrderInfoDAO completedOrderInfoDAO = new CompletedOrderInfoDAO();
    completedOrderInfoDAO.add(new CompletedOrderInfo("AB12345", "Abc Abc (10)", order));
    Source requestPayload = new StringSource(getBody("/web/getOrdersReq.txt"));
    Source responsePayload = new StringSource(getBody("/web/getCompletedOrderResp.txt"));
    mockClient.sendRequest(withPayload(requestPayload)).andExpect(payload(responsePayload));
  }
  
  @Test
  public void testA3DriverEndpoint() throws Exception {
    Source requestPayload = new StringSource(getBody("/web/getDriversStatisticsReq.txt"));
    Source responsePayload = new StringSource(getBody("/web/getDriversStatisticsResp.txt"));
    mockClient.sendRequest(withPayload(requestPayload)).andExpect(payload(responsePayload));
  }
  
  @Test
  public void testA4CarsEndpoint() throws Exception {
    Source requestPayload = new StringSource(getBody("/web/getCarsStatisticsReq.txt"));
    Source responsePayload = new StringSource(getBody("/web/getCarsStatisticsResp.txt"));
    mockClient.sendRequest(withPayload(requestPayload)).andExpect(payload(responsePayload));
  }
}
