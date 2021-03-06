package org.retal.logiweb.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.SessionInfo;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.enums.DriverStatus;
import org.retal.logiweb.service.logic.impl.CargoService;
import org.retal.logiweb.service.logic.impl.DriverService;
import org.retal.logiweb.service.logic.impl.OrderService;
import org.retal.logiweb.service.validators.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class DriverPageController {

  private final SessionInfo sessionInfo;

  private final DriverService driverService;

  private final OrderService orderService;
  
  private final CargoService cargoService;

  public static final String DRIVER_PAGE = "/driverPage";

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public DriverPageController(SessionInfo sessionInfo, DriverService driverService,
      OrderService orderService, CargoService cargoService) {
    this.sessionInfo = sessionInfo;
    this.driverService = driverService;
    this.orderService = orderService;
    this.cargoService = cargoService;
  }

  /**
   * Method responsible for showing driver page with all required information.
   */
  @GetMapping(DRIVER_PAGE)
  public String getDriverPage(Model model) {
    sessionInfo.refreshUser();
    BindingResult result =
        (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "driver");
    Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
    model.addAllAttributes(errors);
    User user = sessionInfo.getCurrentUser();
    addOrderIfExists(user, model);
    model.addAttribute("user", user);
    List<String> statuses = new ArrayList<>();
    for (DriverStatus ds : DriverStatus.values()) {
      statuses.add(ds.toString().replace(" ", "_").toLowerCase());
    }
    model.addAttribute("statuses", statuses);
    return "driverPage";
  }

  /**
   * Method for changing driver status using
   * {@linkplain org.retal.logiweb.service.logic.impl.DriverService service layer}.
   */
  @GetMapping(value = "/changeStatus/{status}")
  public RedirectView changeStatus(@PathVariable String status, RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView(DRIVER_PAGE, true);
    BindingResult bindingResult = new BindException(status, "status");
    driverService.changeStatus(status, bindingResult);
    addErrorsAsFlashAttributes(redir, bindingResult);
    return redirectView;
  }

  /**
   * Method for changing location of driver (aka simulation of moving).
   */
  @GetMapping(value = "/changeLocation")
  public RedirectView changeLocation(RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView(DRIVER_PAGE, true);
    BindingResult bindingResult = new BindException(new Object(), "city");
    driverService.changeLocation(bindingResult);
    addErrorsAsFlashAttributes(redir, bindingResult);
    return redirectView;
  }

  /**
   * Method responsible for updating cargo status (e.g. loaded, unloaded).
   */
  @GetMapping(value = "/updateCargo/{id}")
  public RedirectView updateCargo(@PathVariable Integer id, RedirectAttributes redir) {
    BindingResult bindingResult = new BindException(id, "id");
    driverService.changeStatus(DriverStatus.LOADING_AND_UNLOADING_CARGO.toString(), bindingResult);
    boolean isOrderCompleted =
        cargoService.updateCargoWithOrder(id, bindingResult);
    if (isOrderCompleted) {
      driverService.changeStatus(DriverStatus.ON_SHIFT.toString(), bindingResult);
    }
    RedirectView redirectView = new RedirectView(DRIVER_PAGE, true);
    addErrorsAsFlashAttributes(redir, bindingResult);
    return redirectView;
  }

  /**
   * Method for mapping {@linkplain org.springframework.validation.BindingResult BindingResult} to
   * {@linkplain org.springframework.web.servlet.mvc.support.RedirectAttributes RedirectAttributes}
   * for showing errors for users on page.
   */
  private void addErrorsAsFlashAttributes(RedirectAttributes redir, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "driver", bindingResult);
    }
  }
  
  private void addOrderIfExists(User user, Model model) {
    if (user.getUserInfo().getOrder() != null) {
      Order order = orderService.getOrder(user.getUserInfo().getOrder().getId());
      model.addAttribute("order", order);
      List<String> routeList = new ArrayList<>();
      String nextHop = null;
      int nextHopLength = -1;
      String[] cities = order.getRoute().split(Order.ROUTE_DELIMETER);
      Collections.addAll(routeList, cities);
      String userCity = user.getUserInfo().getCity().getCurrentCity();
      int index = user.getUserInfo().getOrder().getOrderRouteProgression().getRouteCounter() + 1;
      model.addAttribute("routeCounter", index - 1);
      if (index < cities.length) {
        nextHop = cities[index];
        nextHopLength = orderService.lengthBetweenTwoCities(userCity, cities[index]);
        nextHopLength =
            (int) Math.round((double) nextHopLength / OrderService.AVERAGE_CAR_SPEED);
      }
      model.addAttribute("routeList", routeList);
      model.addAttribute("nextHop", nextHop);
      model.addAttribute("nextHopLength", nextHopLength);
    }
  }
}
