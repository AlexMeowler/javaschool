package org.retal.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.retal.domain.Car;
import org.retal.domain.Cargo;
import org.retal.domain.Order;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class CargoAndOrdersPageController {
	
	@GetMapping(value="/cargoAndOrders")
	public String getCargoAndOrdersPage(Model model) {
		BindingResult result = (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "routePoints");
		Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
		for (Map.Entry<String, String> e : errors.entrySet()) {
			log.info(e.getKey() + ":" + e.getValue());
		}
		model.addAllAttributes(errors);
		User user = sessionInfo.getCurrentUser();
		UserInfo userInfo = user.getUserInfo();
		model.addAttribute("current_user_name", userInfo.getName() + " " + userInfo.getSurname());
		model.addAttribute("cargoList", cargoAndOrdersService.getAllCargo());
		model.addAttribute("ordersList", cargoAndOrdersService.getAllOrders());
		model.addAttribute("cityList", cityService.getAllCities());
		return "cargo_orders";
	}
	
	@SuppressWarnings("rawtypes")
	@GetMapping(value="/getCityAndCargoInfo")
	@ResponseBody
	public List[] getAllCities() {
		List<Cargo> cargo = cargoAndOrdersService.getAllCargo();
		for(int i = 0; i < cargo.size(); i++) {
			if(cargo.get(i).getPoints().size() != 0) {
				cargo.remove(i);
				i--;
			}
		}
		return new List[] {cityService.getAllCities(), cargo};
	}
	
	@GetMapping(value="/getCarsForOrder/{id}")
	@ResponseBody
	public List<Car> getAvailableCarsForOrder(@PathVariable Integer id) {
		return carService.getAllAvailableCarsForOrderId(id);
	}
	
	@GetMapping(value="/changeCarForOrder/{data}")
	@ResponseBody
	public String changeCarForOrder(@PathVariable String data) {
		Car car = null;
		Order order  = null;
		String errorMessage = "";
		try {
			log.debug(data);
			String[] input = data.split("_");
			Integer id = Integer.parseInt(input[0]);
			log.debug(id);
			order = cargoAndOrdersService.getOrder(id);
			car = carService.getCar(input[1]);
		} catch(Exception e) {
			errorMessage = "Invalid argument, please don't try to change page code.";
		}
		log.debug(car == null);
		log.debug(order == null);
		if(car == null || order == null || order.getIsCompleted()) {
			errorMessage = "Invalid argument, please don't try to change page code.";
		}
		if(errorMessage.isEmpty()) {
			order.setCar(car);
			cargoAndOrdersService.updateOrder(order);
			return null;
		} else {
			return errorMessage;
		}
	}
	
	@PostMapping(value = "/addNewOrder")
	public RedirectView addNewOrder(RoutePointListWrapper list, BindingResult bindingResult, 
								RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView("/cargoAndOrders", true);
		redir.addFlashAttribute("visible", "true");
		cargoAndOrdersService.createOrderWithRoutePoints(list, bindingResult);
		if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "routePoints", bindingResult);
			redir.addFlashAttribute("counter_value", list.getList().size());
			redir.addFlashAttribute("routePoints", list.getList());
		}
		return redirectView;
	}
	
	@Autowired
	private SessionInfo sessionInfo;
	
	@Autowired
	private CargoAndOrdersService cargoAndOrdersService;
	
	@Autowired
	private CityService cityService;
	
	@Autowired
	private CarService carService;
	
	private static final Logger log = Logger.getLogger(CargoAndOrdersPageController.class);
}
