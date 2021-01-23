package org.retal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.retal.domain.Car;
import org.retal.domain.Cargo;
import org.retal.domain.Order;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.dto.RoutePointDTO;
import org.retal.dto.RoutePointListWrapper;
import org.retal.service.CarService;
import org.retal.service.CargoAndOrdersService;
import org.retal.service.CityService;
import org.retal.service.UserValidator;
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

  private final CargoAndOrdersService cargoAndOrdersService;

  private final CityService cityService;

  private final CarService carService;

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public CargoAndOrdersPageController(SessionInfo sessionInfo,
      CargoAndOrdersService cargoAndOrdersService, CityService cityService, CarService carService) {
    this.sessionInfo = sessionInfo;
    this.cargoAndOrdersService = cargoAndOrdersService;
    this.cityService = cityService;
    this.carService = carService;
  }

  /**
   * Method responsible for showing the main page for cargo and orders.
   */
  @GetMapping(value = "/cargoAndOrders")
  public String getCargoAndOrdersPage(Model model) {
    BindingResult result =
        (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "routePoints");
    Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
    model.addAllAttributes(errors);
    User user = sessionInfo.getCurrentUser();
    UserInfo userInfo = user.getUserInfo();
    model.addAttribute("current_user_name", userInfo.getName() + " " + userInfo.getSurname());
    model.addAttribute("cargoList", cargoAndOrdersService.getAllCargo());
    model.addAttribute("availableCargoList", getAllCitiesAndAssignableCargo()[1]);
    List<Order> orders = cargoAndOrdersService.getAllOrders();
    model.addAttribute("ordersList", orders);
    model.addAttribute("cityList", cityService.getAllCities());
    List<Boolean> isOrderStarted = new ArrayList<>();
    List<Boolean> hasCarsAvailable = new ArrayList<>();
    orders.forEach(o -> {
      isOrderStarted.add(cargoAndOrdersService.isOrderStarted(o));
      List<Car> cars = getAvailableCarsForOrder(o.getId());
      hasCarsAvailable.add(cars != null && cars.size() != 0);
    });
    model.addAttribute("orderStarted", isOrderStarted);
    model.addAttribute("hasCarsAvailable", hasCarsAvailable);
    return "cargo_orders";
  }

  /**
   * Method for AJAX requests. Required for adding orders (form is generated dynamically using JS).
   * 
   * @return List array of 2 elements. List[0] is all {@linkplain org.retal.domain.City cities} from
   *         database. List[1] is all {@linkplain org.retal.domain.Cargo cargo} available for
   *         creating order.
   */
  @SuppressWarnings("rawtypes")
  @GetMapping(value = "/getCityAndCargoInfo")
  @ResponseBody
  public List[] getAllCitiesAndAssignableCargo() {
    List<Cargo> cargo = cargoAndOrdersService.getAllCargo().stream()
        .filter(c -> c.getPoints().size() == 0).collect(Collectors.toList());
    /*
     * for (int i = 0; i < cargo.size(); i++) { if (cargo.get(i).getPoints().size() != 0) {
     * cargo.remove(i); i--; } }
     */
    return new List[] {cityService.getAllCities(), cargo};
  }

  /**
   * Method for AJAX requests. Required for reassigning car for order.
   * 
   * @param id {@linkplain org.retal.domain.Order Order} primary key
   * @return List of {@linkplain org.retal.domain.Car Cars} available for assigning to this order.
   */
  @GetMapping(value = "/getCarsForOrder/{id}")
  @ResponseBody
  public List<Car> getAvailableCarsForOrder(@PathVariable Integer id) {
    return carService.getAllAvailableCarsForOrderId(id);
  }

  /**
   * Method for AJAX requests. Required for changing assigned car for order
   * 
   * @param data input data of pattern "A_B" where A is {@linkplain org.retal.domain.Order Order} ID
   *        and B is {@linkplain org.retal.domain.Car Car} registration ID
   * @return error message if operation failed and null if operation succeeded.
   * @see org.retal.service.CargoAndOrdersService
   */
  @GetMapping(value = "/changeCarForOrder/{data}")
  @ResponseBody
  public String changeCarForOrder(@PathVariable String data) {
    return cargoAndOrdersService.tryToChangeOrderCar(data);
  }

  /**
   * Method responsible for adding new order from form using
   * {@linkplain org.retal.service.CargoAndOrdersService service layer}.
   * 
   */
  @PostMapping(value = "/addNewOrder")
  public RedirectView addNewOrder(RoutePointListWrapper list, BindingResult bindingResult,
      RedirectAttributes redir) {
    redir.addFlashAttribute("visible", "true");
    if (list.getList() == null) {
      list.setList(new ArrayList<RoutePointDTO>());
    }
    cargoAndOrdersService.createOrderAndRoutePoints(list, bindingResult);
    if (bindingResult.hasErrors()) {
      redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "routePoints", bindingResult);
      redir.addFlashAttribute("counter_value", list.getList().size());
      redir.addFlashAttribute("routePoints", list.getList());
    }
    RedirectView redirectView = new RedirectView("/cargoAndOrders", true);
    return redirectView;
  }
}
