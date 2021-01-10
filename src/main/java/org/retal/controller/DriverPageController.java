package org.retal.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.retal.dao.OrderDAO;
import org.retal.domain.Order;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.enums.DriverStatus;
import org.retal.service.CargoAndOrdersService;
import org.retal.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class DriverPageController {
	
	@GetMapping("/driverPage")
	public String getDriverPage(Model model) {
		User user = sessionInfo.getCurrentUser();
		Order order = orderDAO.read(user.getUserInfo().getOrder().getId());
		model.addAttribute("order", order);
		model.addAttribute("user", user);
		List<String> statuses = new ArrayList<>();
		for(DriverStatus ds : DriverStatus.values()) {
			statuses.add(ds.toString().replace(" ", "_").toLowerCase());
		}
		model.addAttribute("statuses", statuses);
		List<String> routeList = new ArrayList<>();
		String nextHop = null;
		int nextHopLength = -1;
		String[] cities = order.getRoute().split(";");
		String userCity = user.getUserInfo().getCurrentCity().getCurrentCity();
		for(int i = 0; i < cities.length; i++) {
			routeList.add(cities[i]);
			if(nextHop == null && i > 0 && cities[i - 1].equalsIgnoreCase(userCity)) {
				nextHop = cities[i];
				nextHopLength = cargoAndOrdersService.lengthBetweenTwoCities(userCity, cities[i]);
			}
		}
		log.debug(nextHop + "; " + nextHopLength);
		nextHopLength = (int)Math.round((double)nextHopLength / CargoAndOrdersService.AVERAGE_CAR_SPEED);
		model.addAttribute("routeList", routeList);
		model.addAttribute("nextHop", nextHop);
		model.addAttribute("nextHopLength", nextHopLength);
		return "driverPage";
	}
	
	@GetMapping(value = "/changeStatus/{status}")
	public RedirectView changeStatus(@PathVariable String status) {
		RedirectView redirectView = new RedirectView("/driverPage", true);
		driverService.changeStatus(status);
		return redirectView;
	}
	
	@GetMapping(value = "/changeLocation/{city}")
	public RedirectView changeLocation(@PathVariable String city) {
		RedirectView redirectView = new RedirectView("/driverPage", true);
		driverService.changeLocation(city);
		return redirectView;
	}
	
	@Autowired
	private SessionInfo sessionInfo;	
	
	@Autowired
	private DriverService driverService;	
	
	@Autowired
	private CargoAndOrdersService cargoAndOrdersService;
	
	@Autowired
	private OrderDAO orderDAO;
	
	private static final Logger log = Logger.getLogger(DriverPageController.class);
}
