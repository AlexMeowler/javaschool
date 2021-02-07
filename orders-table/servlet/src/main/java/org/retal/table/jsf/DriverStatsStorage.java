package org.retal.table.jsf;

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;
import org.retal.table.ws.GetDriversStatisticsRequest;
import org.retal.table.ws.GetDriversStatisticsResponse;
import org.retal.table.ws.Statistics;
import org.retal.table.ws.StatisticsService;

@Named("driverStatsStorage")
@ApplicationScoped
public class DriverStatsStorage {
  private static final Logger log = Logger.getLogger(DriverStatsStorage.class);

  private int driversTotal = 0;

  private int driversAvailable = 0;

  private int driversUnavailable = 0;
  
  @Inject
  @Push(channel = "drivers")
  private PushContext channel;

  /**
   * Creates instance of this class and attempts to connect to web service. If connection failed,
   * then default values are applied to fields.
   */
  public DriverStatsStorage() throws IOException {
    try {
      StatisticsService statisticsService = new StatisticsService();
      Statistics statistics = statisticsService.getStatisticsSoap11();
      GetDriversStatisticsResponse response =
          statistics.getDriversStatistics(new GetDriversStatisticsRequest());
      driversTotal = response.getTotalDrivers();
      driversAvailable = response.getDriversAvailable();
      driversUnavailable = response.getDriversUnavailable();
    } catch (Exception e) {
      log.error(e, e);
    }
  }

  /**
   * Updates fields with values from given web service response.
   * @param response to extract data from
   */
  public void update(GetDriversStatisticsResponse response) {
    setDriversTotal(response.getTotalDrivers());
    setDriversAvailable(response.getDriversAvailable());
    setDriversUnavailable(response.getDriversUnavailable());
    sendMessage("update");
  }
  
  public void sendMessage(Object message) {
    channel.send(message);
    log.info("Updated drivers storage");
  }

  public int getDriversTotal() {
    return driversTotal;
  }

  public void setDriversTotal(int driversTotal) {
    this.driversTotal = driversTotal;
  }

  public int getDriversAvailable() {
    return driversAvailable;
  }

  public void setDriversAvailable(int driversAvailable) {
    this.driversAvailable = driversAvailable;
  }

  public int getDriversUnavailable() {
    return driversUnavailable;
  }

  public void setDriversUnavailable(int driversUnavailable) {
    this.driversUnavailable = driversUnavailable;
  }
}
