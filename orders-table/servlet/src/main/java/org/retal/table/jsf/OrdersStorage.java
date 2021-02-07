package org.retal.table.jsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;
import org.retal.table.ws.GetLatestOrdersRequest;
import org.retal.table.ws.GetLatestOrdersResponse;
import org.retal.table.ws.OrderWS;
import org.retal.table.ws.Statistics;
import org.retal.table.ws.StatisticsService;

@Named("ordersStorage")
@ApplicationScoped
public class OrdersStorage {

  private static final Logger log = Logger.getLogger(OrdersStorage.class);

  private List<OrderWS> orders = new ArrayList<>();

  @Inject
  @Push(channel = "orders")
  private PushContext channel;

  /**
   * Creates instance of this class and attempts to connect to web service. If connection failed,
   * then default values are applied to fields.
   */
  public OrdersStorage() throws IOException {
    try {
      StatisticsService statisticsService = new StatisticsService();
      Statistics statistics = statisticsService.getStatisticsSoap11();
      orders = statistics.getLatestOrders(new GetLatestOrdersRequest()).getOrderList().getOrders();
    } catch (Exception e) {
      log.error(e, e);
    }
  }

  /**
   * Updates fields with values from given web service response.
   * @param response to extract data from
   */
  public void update(GetLatestOrdersResponse response) {
    setOrders(response.getOrderList().getOrders());
    sendMessage("update");
  }

  public List<OrderWS> getOrders() {
    return orders;
  }

  public void setOrders(List<OrderWS> orders) {
    this.orders = orders;
  }

  public void sendMessage(Object message) {
    channel.send(message);
    log.info("Updated orders storage");
  }
}
