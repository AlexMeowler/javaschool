package org.retal.table.ejb.jsf;

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.retal.table.ejb.ws.GetDriversStatisticsRequest;
import org.retal.table.ejb.ws.GetDriversStatisticsResponse;
import org.retal.table.ejb.ws.Statistics;
import org.retal.table.ejb.ws.StatisticsService;

@Named("driverStatsStorage")
@ApplicationScoped
public class DriverStatsStorage {
  private static final Logger log = Logger.getLogger(DriverStatsStorage.class);

  private int driversTotal = 0;

  private int driversAvailable = 0;

  private int driversUnavailable = 0;

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

  public void update(GetDriversStatisticsResponse response) {
    setDriversTotal(response.getTotalDrivers());
    setDriversAvailable(response.getDriversAvailable());
    setDriversUnavailable(response.getDriversUnavailable());
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
