package org.retal.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.retal.domain.Cargo;
import org.retal.domain.City;
import org.retal.domain.RoutePoint;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.dto.CityDTO;
import org.retal.dto.RoutePointDTO;
import org.retal.dto.RoutePointListWrapper;
import org.retal.dto.UserDTO;
import org.retal.dto.UserInfoDTO;
import org.retal.service.CargoAndOrdersService;
import org.retal.service.CityService;
import org.retal.service.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
	
	@PostMapping(value = "/addNewOrder")
	public RedirectView addNewOrder(RoutePointListWrapper list, BindingResult bindingResult, 
								RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView("/cargoAndOrders", true);
		redir.addFlashAttribute("visible", "true");
		cargoAndOrdersService.createOrderWithRoutePoints(list, bindingResult);
		if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "routePoints", bindingResult);
			redir.addFlashAttribute("counter_value", list.getList().size());
			redir.addFlashAttribute("routePoints", list.getList());//FIXME for each
		}
		return redirectView;
	}
	
	@Autowired
	private SessionInfo sessionInfo;
	
	@Autowired
	private CargoAndOrdersService cargoAndOrdersService;
	
	@Autowired
	private CityService cityService;
	
	private static final Logger log = Logger.getLogger(CargoAndOrdersPageController.class);
}
