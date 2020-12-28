package org.retal.controller;

import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.service.CargoAndOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CargoAndOrdersPageController {
	
	@GetMapping(value="/cargoAndOrders")
	public String getCargoAndOrdersPage(Model model) {
		User user = sessionInfo.getCurrentUser();
		UserInfo userInfo = user.getUserInfo();
		model.addAttribute("current_user_name", userInfo.getName() + " " + userInfo.getSurname());
		model.addAttribute("cargoList", cargoAndOrdersService.getAllCargo());
		return "cargo_orders";
	}
	
	@Autowired
	private SessionInfo sessionInfo;
	
	@Autowired
	private CargoAndOrdersService cargoAndOrdersService;
}
