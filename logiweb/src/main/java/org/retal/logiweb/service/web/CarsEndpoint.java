package org.retal.logiweb.service.web;

import java.util.List;
import org.apache.log4j.Logger;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.ws.GetCarsStatisticsRequest;
import org.retal.logiweb.domain.ws.GetCarsStatisticsResponse;
import org.retal.logiweb.service.logic.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class CarsEndpoint {

  private final CarService carService;

  private static final String NAMESPACE_URI = "http://retal.org/logiweb/ws";

  private static final Logger log = Logger.getLogger(CarsEndpoint.class);

  @Autowired
  public CarsEndpoint(CarService carService) {
    this.carService = carService;
  }

  /**
   * Provides statistics about cars: total, broken, assigned to order etc.
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getCarsStatisticsRequest")
  @ResponsePayload
  public GetCarsStatisticsResponse getDriversInfo(
      @RequestPayload GetCarsStatisticsRequest request) {
    log.info("Received SOAP request");
    GetCarsStatisticsResponse response = new GetCarsStatisticsResponse();
    List<Car> cars = carService.getAllCars();
    response.setTotalCars(cars.size());
    response.setCarsBroken((int) cars.stream().filter(c -> !c.getIsWorking()).count());
    response.setCarsAssigned((int) cars.stream().filter(c -> c.getIsWorking())
        .filter(c -> c.getOrder() != null).count());
    response.setCarsAvailable((int) cars.stream().filter(c -> c.getIsWorking())
        .filter(c -> c.getOrder() == null).count());
    log.info("Sending SOAP response");
    return response;
  }
}
