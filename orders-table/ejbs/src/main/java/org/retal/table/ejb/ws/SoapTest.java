package org.retal.table.ejb.ws;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.log4j.Logger;

@Singleton
@Startup
public class SoapTest {

  private static final Logger log = Logger.getLogger(SoapTest.class);

  @PostConstruct
  public void init() {
    log.info("Starting " + this.getClass().getName());
    log.info("Contacting SOAP service...");
    Statistics statistics = new StatisticsService().getStatisticsSoap11();
    GetLatestOrdersResponse response = statistics.getLatestOrders(new GetLatestOrdersRequest());
    log.info("Retrieved " + (response != null ? response.getOrderList().getOrders().size() : "null")
        + " orders from Logiweb");
  }
}
