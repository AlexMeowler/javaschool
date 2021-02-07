package org.retal.table.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.retal.table.jsf.CarStatsStorage;
import org.retal.table.jsf.DriverStatsStorage;
import org.retal.table.jsf.OrdersStorage;
import org.retal.table.ws.StatisticsService;

@RunWith(MockitoJUnitRunner.class)
public class ReceiverTest {

  @Spy
  private CarStatsStorage cars;

  @Spy
  private DriverStatsStorage drivers;

  @Spy
  private OrdersStorage orders;

  @Mock
  private StatisticsService statistics;

  @InjectMocks
  private NotificationReceiver receiver;

  private AutoCloseable closeable;


  private static int counter = 1;

  private static final Logger log = Logger.getLogger(ReceiverTest.class);

  /**
   * Setting up mocks and giving information on test start.
   */
  @Before
  public void setup() {
    closeable = MockitoAnnotations.openMocks(this);
    // receiver = new NotificationReceiver();
    when(statistics.getStatisticsSoap11()).thenReturn(new StatisticsMock());
    doNothing().when(cars).sendMessage(any(Object.class));
    doNothing().when(drivers).sendMessage(any(Object.class));
    doNothing().when(orders).sendMessage(any(Object.class));
    receiver.setStatisticsService(statistics);
    log.info("-----Starting test " + counter + "-----");
  }

  /**
   * Releasing mocks, ending test.
   */
  @After
  public void end() throws Exception {
    closeable.close();
    log.info("-----Ending test " + counter + "-----");
    counter++;
  }

  @Test
  public void testOrdersUpdate() throws JMSException {
    ObjectMessage message =
        new ObjectMessageMock(new NotificationMessage(NotificationType.ORDERS_UPDATE));
    receiver.onMessage(message);
    assertNotEquals(orders.getOrders().size(), 0);
    orders.getOrders().forEach(o -> {
      assertEquals(StatisticsMock.ORDER_PARAMS[0], o.getCar());
      assertEquals(StatisticsMock.ORDER_PARAMS[1], o.getId());
      assertEquals(StatisticsMock.ORDER_PARAMS[2], o.isIsCompleted());
      assertEquals(StatisticsMock.ORDER_PARAMS[3], o.getRoute());
      o.getCargoList().getCargo().forEach(c -> {
        assertEquals(StatisticsMock.CARGO_PARAMS[0], c.getFrom());
        assertEquals(StatisticsMock.CARGO_PARAMS[1], c.getTo());
        assertEquals(StatisticsMock.CARGO_PARAMS[2], c.getId());
        assertEquals(StatisticsMock.CARGO_PARAMS[3], c.getName());
      });
      o.getDriverList().getDrivers().forEach(d -> assertEquals(StatisticsMock.DRIVER_NAME, d));
    });
  }

  @Test
  public void testCarsUpdate() throws JMSException {
    ObjectMessage message =
        new ObjectMessageMock(new NotificationMessage(NotificationType.CARS_UPDATE));
    receiver.onMessage(message);
    assertEquals(StatisticsMock.CARS_ASSIGNED, cars.getCarsAssigned());
    assertEquals(StatisticsMock.CARS_AVAILABLE, cars.getCarsAvailable());
    assertEquals(StatisticsMock.CARS_BROKEN, cars.getCarsBroken());
    assertEquals(StatisticsMock.CARS_TOTAL, cars.getCarsTotal());
  }

  @Test
  public void testDriversUpdate() throws JMSException {
    ObjectMessage message =
        new ObjectMessageMock(new NotificationMessage(NotificationType.DRIVERS_UPDATE));
    receiver.onMessage(message);
    assertEquals(StatisticsMock.DRIVERS_AVAILABLE, drivers.getDriversAvailable());
    assertEquals(StatisticsMock.DRIVERS_UNAVAILABLE, drivers.getDriversUnavailable());
    assertEquals(StatisticsMock.DRIVERS_TOTAL, drivers.getDriversTotal());
  }

  @Test
  public void testUnsupportedMessageType() throws JMSException {
    ObjectMessage message = new ObjectMessageMock(new Serializable() {
      private static final long serialVersionUID = 1L;
    });
    receiver.onMessage(message);
  }
}
