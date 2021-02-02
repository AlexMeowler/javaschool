package org.retal.logiweb.service.web;

import java.util.List;
import org.retal.logiweb.domain.entity.Order;
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

  private static final int LIST_SIZE = 2;
  
  private static final String NAMESPACE_URI = "http://retal.org/logiweb/ws";

  @Autowired
  public OrderEndpoint(CargoAndOrdersService cargoAndOrdersService) {
    this.cargoAndOrdersService = cargoAndOrdersService;
  }

  /**
   * Javadoc TODO
   * @param request
   * @return
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getLatestOrdersRequest")
  @ResponsePayload
  public GetLatestOrdersResponse getLatestOrders(@RequestPayload GetLatestOrdersRequest request) {
    List<Order> orders = cargoAndOrdersService.getAllOrders();
    OrderList list = new OrderList();
    //TODO minimal
    orders.subList(orders.size() - LIST_SIZE, orders.size()).stream()
        .map(this::mapOrderEntityToWebServiceResponse).forEach(o -> list.getOrders().add(o));
    GetLatestOrdersResponse response = new GetLatestOrdersResponse();
    response.setOrderList(list);
    return response;
  }

  private OrderWS mapOrderEntityToWebServiceResponse(Order order) {
    OrderWS mapped = new OrderWS();
    mapped.setId(order.getId());
    mapped.setIsCompleted(order.getIsCompleted());
    mapped.setRoute(order.getRoute());
    return mapped;
  }
}
