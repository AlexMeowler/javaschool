package org.retal.table.jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.apache.log4j.Logger;
import org.retal.table.jsf.CarStatsStorage;
import org.retal.table.jsf.DriverStatsStorage;
import org.retal.table.jsf.OrdersStorage;
import org.retal.table.ws.GetCarsStatisticsRequest;
import org.retal.table.ws.GetDriversStatisticsRequest;
import org.retal.table.ws.GetLatestOrdersRequest;
import org.retal.table.ws.Statistics;
import org.retal.table.ws.StatisticsService;

@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "notificationsQueue")})
public class NotificationReceiver implements MessageListener, Receiver {

  @Inject
  private OrdersStorage ordersStorage;

  @Inject
  private DriverStatsStorage driverStatsStorage;

  @Inject
  private CarStatsStorage carStatsStorage;

  private StatisticsService statisticsService;

  /**
   * Creates instance of this class with an attempt to instantinate web service
   * {@link StatisticsService}. If instantination failed due to any reason, exception will be logged
   * and property will be set to null.
   */
  public NotificationReceiver() {
    try {
      statisticsService = new StatisticsService();
    } catch (Exception e) {
      statisticsService = null;
      log.error("Could not instantinate StatisticsService, setting property to null...");
      log.error(e, e);
    }
  }

  private static final Logger log = Logger.getLogger(NotificationReceiver.class);

  @Override
  public boolean messageBodySupported(Class<?> messageClass) {
    return messageClass.isAssignableFrom(NotificationMessage.class);
  }

  @Override
  public void onMessage(Message message) {
    try {
      ObjectMessage objectMessage = (ObjectMessage) message;
      if (messageBodySupported(objectMessage.getObject().getClass())) {
        NotificationMessage msg = (NotificationMessage) objectMessage.getObject();
        log.info("Received notification of type " + msg.getType().toString());
        Statistics statistics = statisticsService.getStatisticsSoap11();
        switch (msg.getType()) {
          case CARS_UPDATE:
            carStatsStorage.update(statistics.getCarsStatistics(new GetCarsStatisticsRequest()));
            break;
          case DRIVERS_UPDATE:
            driverStatsStorage
                .update(statistics.getDriversStatistics(new GetDriversStatisticsRequest()));
            break;
          case ORDERS_UPDATE:
            ordersStorage.update(statistics.getLatestOrders(new GetLatestOrdersRequest()));
            break;
          default:
            throw new JMSException(
                "NotificationType " + msg.getType().toString() + " support is not implemented");
        }
      } else {
        log.warn("Incompatible message type for receiver " + this.getClass().getName()
            + ". Message ignored");
      }
    } catch (JMSException e) {
      log.error(e, e);
    }
  }

  public void setStatisticsService(StatisticsService statisticsService) {
    this.statisticsService = statisticsService;
  }

}
