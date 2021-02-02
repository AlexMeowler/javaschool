package org.retal.table.ejb.jsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.retal.table.ejb.ws.GetLatestOrdersRequest;
import org.retal.table.ejb.ws.GetLatestOrdersResponse;
import org.retal.table.ejb.ws.OrderWS;
import org.retal.table.ejb.ws.Statistics;
import org.retal.table.ejb.ws.StatisticsService;

@Named("ordersStorage")
@Singleton
@Startup
public class OrdersStorage {

  private static final Logger log = Logger.getLogger(OrdersStorage.class);

  private List<OrderWS> orders;

  public OrdersStorage() throws IOException {
    try {
      StatisticsService statisticsService = new StatisticsService();
      Statistics statistics = statisticsService.getStatisticsSoap11();
      orders = statistics.getLatestOrders(new GetLatestOrdersRequest()).getOrderList().getOrders();
    } catch (Exception e) {
      log.error(e, e);
      this.orders = new ArrayList<>();
    }
  }

  public void update(GetLatestOrdersResponse response) {
    setOrders(response.getOrderList().getOrders());
    log.info("Updated orders storage");
  }

  public List<OrderWS> getOrders() {
    return orders;
  }

  public void setOrders(List<OrderWS> orders) {
    this.orders = orders;
  }
}
