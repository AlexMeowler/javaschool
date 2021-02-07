package org.retal.table.jms;

import org.apache.log4j.Logger;
import org.retal.table.ws.CargoList;
import org.retal.table.ws.CargoWS;
import org.retal.table.ws.DriverList;
import org.retal.table.ws.GetCarsStatisticsRequest;
import org.retal.table.ws.GetCarsStatisticsResponse;
import org.retal.table.ws.GetDriversStatisticsRequest;
import org.retal.table.ws.GetDriversStatisticsResponse;
import org.retal.table.ws.GetLatestOrdersRequest;
import org.retal.table.ws.GetLatestOrdersResponse;
import org.retal.table.ws.OrderList;
import org.retal.table.ws.OrderWS;
import org.retal.table.ws.Statistics;

public class StatisticsMock implements Statistics {

  public static final int DRIVERS_AVAILABLE = 10;
  public static final int DRIVERS_UNAVAILABLE = 5;
  public static final int DRIVERS_TOTAL = 15;

  public static final int CARS_ASSIGNED = 10;
  public static final int CARS_BROKEN = 3;
  public static final int CARS_AVAILABLE = 20;
  public static final int CARS_TOTAL = 33;

  /**
   * Fields "car", "id", "isCompleted", "route".
   */
  public static final Object[] ORDER_PARAMS = {"AB12345", 1, false, "ROUTE"};

  /**
   * Fields "from", "to", "id", "name".
   */
  public static final Object[] CARGO_PARAMS = {"A", "B", 1, "NAME"};

  public static final String DRIVER_NAME = "Driver1";

  private static final Logger log = Logger.getLogger(StatisticsMock.class);

  @Override
  public GetDriversStatisticsResponse getDriversStatistics(
      GetDriversStatisticsRequest getDriversStatisticsRequest) {
    log.debug("Drivers statistics request");
    GetDriversStatisticsResponse response = new GetDriversStatisticsResponse();
    response.setDriversAvailable(DRIVERS_AVAILABLE);
    response.setDriversUnavailable(DRIVERS_UNAVAILABLE);
    response.setTotalDrivers(DRIVERS_TOTAL);
    return response;
  }

  @Override
  public GetCarsStatisticsResponse getCarsStatistics(
      GetCarsStatisticsRequest getCarsStatisticsRequest) {
    log.debug("Cars statistics request");
    GetCarsStatisticsResponse response = new GetCarsStatisticsResponse();
    response.setCarsAssigned(CARS_ASSIGNED);
    response.setCarsAvailable(CARS_AVAILABLE);
    response.setCarsBroken(CARS_BROKEN);
    response.setTotalCars(CARS_TOTAL);
    return response;
  }

  @Override
  public GetLatestOrdersResponse getLatestOrders(GetLatestOrdersRequest getLatestOrdersRequest) {
    log.debug("Orders statistics request");
    OrderWS order = new OrderWS();
    order.setCargoList(generateCargoList());
    order.setDriverList(generateDriverList());
    order.setCar((String) ORDER_PARAMS[0]);
    order.setId((Integer) ORDER_PARAMS[1]);
    order.setIsCompleted((Boolean) ORDER_PARAMS[2]);
    order.setRoute((String) ORDER_PARAMS[3]);
    OrderList orders = new OrderList();
    orders.getOrders().add(order);
    GetLatestOrdersResponse response = new GetLatestOrdersResponse();
    response.setOrderList(orders);
    return response;
  }

  private CargoList generateCargoList() {
    CargoWS cargoWS = new CargoWS();
    cargoWS.setFrom((String) CARGO_PARAMS[0]);
    cargoWS.setTo((String) CARGO_PARAMS[1]);
    cargoWS.setId((Integer) CARGO_PARAMS[2]);
    cargoWS.setName((String) CARGO_PARAMS[3]);
    CargoList cargo = new CargoList();
    cargo.getCargo().add(cargoWS);
    return cargo;
  }

  private DriverList generateDriverList() {
    DriverList list = new DriverList();
    list.getDrivers().add(DRIVER_NAME);
    return list;
  }

}
