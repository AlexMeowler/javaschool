package org.retal.logiweb.controller;

import java.util.List;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.SessionInfo;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.service.logic.impl.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CompletedOrdersPageController {

  private final SessionInfo sessionInfo;

  private final OrderService orderService;

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public CompletedOrdersPageController(SessionInfo sessionInfo, OrderService orderService) {
    this.sessionInfo = sessionInfo;
    this.orderService = orderService;
  }

  /**
   * Method responsible for showing the main page for cargo and orders.
   */
  @GetMapping(value = "/completedOrders")
  public String getCargoAndOrdersPage(Model model) {
    User user = sessionInfo.getCurrentUser();
    UserInfo userInfo = user.getUserInfo();
    model.addAttribute("current_user_name", userInfo.getName() + " " + userInfo.getSurname());
    List<Order> orders = orderService.getAllCompletedOrders();
    model.addAttribute("ordersList", orders);
    return "completedOrders";
  }
}
