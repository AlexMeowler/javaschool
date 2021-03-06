package org.retal.logiweb.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.Cargo;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.SessionInfo;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.dto.RoutePointListWrapper;
import org.retal.logiweb.service.logic.impl.CarService;
import org.retal.logiweb.service.logic.impl.CargoService;
import org.retal.logiweb.service.logic.impl.CityService;
import org.retal.logiweb.service.logic.impl.OrderService;
import org.retal.logiweb.service.validators.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class CargoAndOrdersPageController {

  private final SessionInfo sessionInfo;

  private final OrderService orderService;

  private final CargoService cargoService;

  private final CityService cityService;

  private final CarService carService;

  private static final String ROUTE_POINT_MODEL_ATTRIBUTE = "routePoints";

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public CargoAndOrdersPageController(SessionInfo sessionInfo, OrderService orderService,
      CargoService cargoService, CityService cityService, CarService carService) {
    this.sessionInfo = sessionInfo;
    this.orderService = orderService;
    this.cityService = cityService;
    this.carService = carService;
    this.cargoService = cargoService;
  }

  /**
   * Method responsible for showing the main page for cargo and orders.
   */
  @GetMapping(value = "/cargoAndOrders")
  public String getCargoAndOrdersPage(Model model) {
    BindingResult result = (BindingResult) model.asMap()
        .get(BindingResult.MODEL_KEY_PREFIX + ROUTE_POINT_MODEL_ATTRIBUTE);
    Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
    model.addAllAttributes(errors);
    User user = sessionInfo.getCurrentUser();
    UserInfo userInfo = user.getUserInfo();
    model.addAttribute("current_user_name", userInfo.getName() + " " + userInfo.getSurname());
    model.addAttribute("cargoList", cargoService.getAllCargo());
    model.addAttribute("availableCargoList", getAllCitiesAndAssignableCargo()[1]);
    List<Order> orders = orderService.getAllActiveOrders();
    model.addAttribute("ordersList", orders);
    model.addAttribute("cityList", cityService.getAllCities());
    List<Boolean> isOrderStarted = new ArrayList<>();
    List<Boolean> hasCarsAvailable = new ArrayList<>();
    orders.forEach(o -> {
      isOrderStarted.add(orderService.isOrderStarted(o));
      List<Car> cars = getAvailableCarsForOrder(o.getId());
      hasCarsAvailable.add(cars != null && !cars.isEmpty());
    });
    model.addAttribute("orderStarted", isOrderStarted);
    model.addAttribute("hasCarsAvailable", hasCarsAvailable);
    return "cargo_orders";
  }

  /**
   * Method for AJAX requests. Required for adding orders (form is generated dynamically using JS).
   * 
   * @return List array of 2 elements. List[0] is all
   *         {@linkplain org.retal.logiweb.domain.entity.City cities} from database. List[1] is all
   *         {@linkplain org.retal.logiweb.domain.entity.Cargo cargo} available for creating order.
   */
  @SuppressWarnings("rawtypes")
  @GetMapping(value = "/getCityAndCargoInfo")
  @ResponseBody
  public List[] getAllCitiesAndAssignableCargo() {
    List<Cargo> cargo = cargoService.getAllCargo().stream().filter(c -> c.getPoints().isEmpty())
        .collect(Collectors.toList());
    return new List[] {cityService.getAllCities(), cargo};
  }

  /**
   * Method for AJAX requests. Required for reassigning car for order.
   * 
   * @param id {@linkplain org.retal.logiweb.domain.entity.Order Order} primary key
   * @return List of {@linkplain org.retal.logiweb.domain.entity.Car Cars} available for assigning
   *         to this order.
   */
  @GetMapping(value = "/getCarsForOrder/{id}")
  @ResponseBody
  public List<Car> getAvailableCarsForOrder(@PathVariable Integer id) {
    return carService.getAllAvailableCarsForOrderId(id);
  }

  /**
   * Method for AJAX requests. Required for changing assigned car for order
   * 
   * @param data input data of pattern "A_B" where A is
   *        {@linkplain org.retal.logiweb.domain.entity.Order Order} ID and B is
   *        {@linkplain org.retal.logiweb.domain.entity.Car Car} registration ID
   * @return error message if operation failed and null if operation succeeded.
   * @see org.retal.logiweb.service.logic.impl.OrderService
   */
  @GetMapping(value = "/changeCarForOrder/{data}")
  @ResponseBody
  public String changeCarForOrder(@PathVariable String data) {
    return orderService.changeOrderCar(data);
  }

  /**
   * Method responsible for adding new order from form using
   * {@linkplain org.retal.logiweb.service.logic.impl.OrderService service layer}.
   * 
   */
  @PostMapping(value = "/addNewOrder")
  public RedirectView addNewOrder(RoutePointListWrapper list, BindingResult bindingResult,
      RedirectAttributes redir) {
    redir.addFlashAttribute("visible", "true");
    if (list.getList() == null) {
      list.setList(new ArrayList<>());
    }
    orderService.createOrderFromRoutePoints(list, bindingResult);
    if (bindingResult.hasErrors()) {
      redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + ROUTE_POINT_MODEL_ATTRIBUTE,
          bindingResult);
      redir.addFlashAttribute("counter_value", list.getList().size());
      redir.addFlashAttribute(ROUTE_POINT_MODEL_ATTRIBUTE, list.getList());
    }
    return new RedirectView("/cargoAndOrders", true);
  }
}
