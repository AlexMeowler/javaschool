package org.retal.logiweb.service.web;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.retal.logiweb.domain.entity.Cargo;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.RoutePoint;
import org.retal.logiweb.domain.ws.CargoList;
import org.retal.logiweb.domain.ws.CargoWS;
import org.retal.logiweb.domain.ws.DriverList;
import org.retal.logiweb.domain.ws.GetLatestOrdersRequest;
import org.retal.logiweb.domain.ws.GetLatestOrdersResponse;
import org.retal.logiweb.domain.ws.OrderList;
import org.retal.logiweb.domain.ws.OrderWS;
import org.retal.logiweb.service.logic.CargoAndOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class OrderEndpoint {

  private final CargoAndOrdersService cargoAndOrdersService;

  private static final int LIST_SIZE = 10;

  private static final String NAMESPACE_URI = "http://retal.org/logiweb/ws";

  private static final Logger log = Logger.getLogger(OrderEndpoint.class);

  @Autowired
  public OrderEndpoint(CargoAndOrdersService cargoAndOrdersService) {
    this.cargoAndOrdersService = cargoAndOrdersService;
  }

  /**
   * Javadoc TODO
   * 
   * @param request
   * @return
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getLatestOrdersRequest")
  @ResponsePayload
  public GetLatestOrdersResponse getLatestOrders(@RequestPayload GetLatestOrdersRequest request) {
    log.info("Received SOAP request");
    List<Order> orders = cargoAndOrdersService.getAllOrders();
    OrderList list = new OrderList();
    int startIndex = Math.max(0, orders.size() - LIST_SIZE);
    orders.subList(startIndex, orders.size()).stream().map(this::toWebServiceResponse)
        .forEach(o -> list.getOrders().add(o));
    GetLatestOrdersResponse response = new GetLatestOrdersResponse();
    response.setOrderList(list);
    log.info("Sending SOAP response");
    return response;
  }

  private OrderWS toWebServiceResponse(Order order) {
    OrderWS mapped = new OrderWS();
    mapped.setId(order.getId());
    mapped.setIsCompleted(order.getIsCompleted());
    mapped.setCar(order.getCar() != null ? order.getCar().getRegistrationId() : "-");
    mapped.setRoute(order.getRoute());
    mapped.setDriverList(toMappedDriverList(order));
    mapped.setCargoList(toMappedCargoList(order));
    return mapped;
  }

  private DriverList toMappedDriverList(Order order) {
    DriverList list = new DriverList();
    order.getDriverInfo().stream().forEach(ui -> list.getDrivers()
        .add(String.format("%s %s (%d)", ui.getName(), ui.getSurname(), ui.getUser().getId())));
    return list;
  }

  private CargoList toMappedCargoList(Order order) {
    CargoList list = new CargoList();
    order.getCargo().stream().map(this::toMappedCargo).forEach(list.getCargo()::add);
    return list;
  }

  private CargoWS toMappedCargo(Cargo cargo) {
    CargoWS cargoWS = new CargoWS();
    cargoWS.setId(cargo.getId());
    cargoWS.setName(cargo.getName());
    List<RoutePoint> points =
        cargo.getPoints().stream().sorted((a, b) -> b.getIsLoading().compareTo(a.getIsLoading()))
            .collect(Collectors.toList());
    cargoWS.setFrom(points.get(0).getCity().getCurrentCity());
    cargoWS.setTo(points.get(1).getCity().getCurrentCity());
    return cargoWS;
  }
}
