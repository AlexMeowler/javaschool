package org.retal.controller;

import java.util.List;

import org.apache.log4j.Logger;
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
		return new List[] {cityService.getAllCities(), cargoAndOrdersService.getAllCargo()};
	}
	
	@PostMapping(value = "/addNewOrder")
	public RedirectView addNewUser(RoutePointListWrapper list, BindingResult bindingResult, 
								RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView("/cargoAndOrders", true);
		redir.addFlashAttribute("visible", "true");
		List<RoutePoint> points = cargoAndOrdersService.mapRoutePointDTOsToEntities(list.getList());
		log.info("Mapped " + points.size() + " DTOs to entities");
		cargoAndOrdersService.createOrderWithRoutePoints(points);
		//userService.addNewUser(user, bindingResult, userDTO.getPassword());
		/*if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
			redir.addFlashAttribute("user", user);
		}*/
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
