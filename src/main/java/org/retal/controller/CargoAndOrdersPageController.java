package org.retal.controller;

import java.util.List;

import org.retal.domain.City;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.service.CargoAndOrdersService;
import org.retal.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CargoAndOrdersPageController {
	
	@GetMapping(value="/cargoAndOrders")
	public String getCargoAndOrdersPage(Model model) {
		User user = sessionInfo.getCurrentUser();
		UserInfo userInfo = user.getUserInfo();
		model.addAttribute("current_user_name", userInfo.getName() + " " + userInfo.getSurname());
		model.addAttribute("cargoList", cargoAndOrdersService.getAllCargo());
		model.addAttribute("cityList", cityService.getAllCities());
		return "cargo_orders";
	}
	
	@GetMapping(value="/getCityAndCargoInfo")
	@ResponseBody
	public List[] getAllCities() {
		return new List[] {cityService.getAllCities(), cargoAndOrdersService.getAllCargo()};
	}
	
	@Autowired
	private SessionInfo sessionInfo;
	
	@Autowired
	private CargoAndOrdersService cargoAndOrdersService;
	
	@Autowired
	private CityService cityService;
}
