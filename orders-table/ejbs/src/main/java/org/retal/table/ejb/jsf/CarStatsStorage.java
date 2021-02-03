package org.retal.table.ejb.jsf;

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.retal.table.ejb.ws.GetCarsStatisticsRequest;
import org.retal.table.ejb.ws.GetCarsStatisticsResponse;
import org.retal.table.ejb.ws.Statistics;
import org.retal.table.ejb.ws.StatisticsService;

@Named("carStatsStorage")
@ApplicationScoped
public class CarStatsStorage {
  
  private static final Logger log = Logger.getLogger(CarStatsStorage.class);

  private int carsTotal = 0;

  private int carsAvailable = 0;

  private int carsAssigned = 0;
  
  private int carsBroken = 0;

  public CarStatsStorage() throws IOException {
    try {
      StatisticsService statisticsService = new StatisticsService();
      Statistics statistics = statisticsService.getStatisticsSoap11();
      GetCarsStatisticsResponse response =
          statistics.getCarsStatistics(new GetCarsStatisticsRequest());
      carsTotal = response.getTotalCars();
      carsAvailable = response.getCarsAvailable();
      carsAssigned = response.getCarsAssigned();
      carsBroken = response.getCarsBroken();
    } catch (Exception e) {
      log.error(e, e);
    }
  }

  public void update(GetCarsStatisticsResponse response) {
    setCarsTotal(response.getTotalCars());
    setCarsAvailable(response.getCarsAvailable());
    setCarsAssigned(response.getCarsAssigned());
    setCarsBroken(response.getCarsBroken());
    log.info("Updated cars storage");
  }

  public int getCarsTotal() {
    return carsTotal;
  }

  public void setCarsTotal(int carsTotal) {
    this.carsTotal = carsTotal;
  }

  public int getCarsAvailable() {
    return carsAvailable;
  }

  public void setCarsAvailable(int carsAvailable) {
    this.carsAvailable = carsAvailable;
  }

  public int getCarsAssigned() {
    return carsAssigned;
  }

  public void setCarsAssigned(int carsAssigned) {
    this.carsAssigned = carsAssigned;
  }
  
  public int getCarsBroken() {
    return carsBroken;
  }

  public void setCarsBroken(int carsBroken) {
    this.carsBroken = carsBroken;
  }
}