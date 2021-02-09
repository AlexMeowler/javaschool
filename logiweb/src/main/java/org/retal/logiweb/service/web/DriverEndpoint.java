package org.retal.logiweb.service.web;

import java.util.List;
import org.apache.log4j.Logger;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.enums.DriverStatus;
import org.retal.logiweb.domain.ws.GetDriversStatisticsRequest;
import org.retal.logiweb.domain.ws.GetDriversStatisticsResponse;
import org.retal.logiweb.service.logic.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class DriverEndpoint {

  private final UserService userService;

  private static final String NAMESPACE_URI = "http://retal.org/logiweb/ws";

  private static final Logger log = Logger.getLogger(DriverEndpoint.class);

  @Autowired
  public DriverEndpoint(UserService userService) {
    this.userService = userService;
  }

  /**
   * Provides statistics about drivers: total, available, unavailable.
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getDriversStatisticsRequest")
  @ResponsePayload
  public GetDriversStatisticsResponse getDriversInfo(
      @RequestPayload GetDriversStatisticsRequest request) {
    log.info("Received SOAP request");
    GetDriversStatisticsResponse response = new GetDriversStatisticsResponse();
    List<User> drivers = userService.getAllDrivers();
    response.setTotalDrivers(drivers.size());
    response.setDriversAvailable((int) drivers.stream().map(u -> u.getUserInfo())
        .filter(ui -> ui.getStatus().equalsIgnoreCase(DriverStatus.ON_SHIFT.toString()))
        .filter(ui -> ui.getOrder() == null).count());
    response.setDriversUnavailable((int) drivers.stream().map(u -> u.getUserInfo())
        .filter(ui -> !ui.getStatus().equalsIgnoreCase(DriverStatus.ON_SHIFT.toString())
            || ui.getOrder() != null)
        .count());
    log.info("Sending SOAP response");
    return response;
  }
}
