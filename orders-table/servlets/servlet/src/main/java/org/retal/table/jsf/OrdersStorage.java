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

  public OrdersStorage() throws IOException {
    try {
      StatisticsService statisticsService = new StatisticsService();
      Statistics statistics = statisticsService.getStatisticsSoap11();
      orders = statistics.getLatestOrders(new GetLatestOrdersRequest()).getOrderList().getOrders();
    } catch (Exception e) {
      log.error(e, e);
    }
  }

  public void update(GetLatestOrdersResponse response) {
    setOrders(response.getOrderList().getOrders());
    channel.send("update");
    log.info("Updated orders storage");
  }

  public List<OrderWS> getOrders() {
    return orders;
  }

  public void setOrders(List<OrderWS> orders) {
    this.orders = orders;
  }
}
