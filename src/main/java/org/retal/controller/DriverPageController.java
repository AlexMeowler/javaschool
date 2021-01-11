package org.retal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.retal.dao.OrderDAO;
import org.retal.domain.Order;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.enums.DriverStatus;
import org.retal.service.CargoAndOrdersService;
import org.retal.service.DriverService;
import org.retal.service.UserValidator;
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
	
	@GetMapping("/driverPage")
	public String getDriverPage(Model model) {
		sessionInfo.refreshUser();
		BindingResult result = (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "driver");
		Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
		model.addAllAttributes(errors);
		User user = sessionInfo.getCurrentUser();
		if(user.getUserInfo().getOrder() != null) {
			Order order = orderDAO.read(user.getUserInfo().getOrder().getId());
			model.addAttribute("order", order);
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
		}
		
		model.addAttribute("user", user);
		List<String> statuses = new ArrayList<>();
		for(DriverStatus ds : DriverStatus.values()) {
			statuses.add(ds.toString().replace(" ", "_").toLowerCase());
		}
		model.addAttribute("statuses", statuses);
		return "driverPage";
	}
	
	@GetMapping(value = "/changeStatus/{status}")
	public RedirectView changeStatus(@PathVariable String status, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView("/driverPage", true);
		BindingResult bindingResult = new BindException(status, "status");
		driverService.changeStatus(status, bindingResult);
		if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "driver", bindingResult);
		}
		return redirectView;
	}
	
	@GetMapping(value = "/changeLocation/{city}")
	public RedirectView changeLocation(@PathVariable String city, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView("/driverPage", true);
		BindingResult bindingResult = new BindException(city, "city");
		driverService.changeLocation(city, bindingResult);
		if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "driver", bindingResult);
		}
		return redirectView;
	}
	
	@GetMapping(value = "/updateCargo/{id}")
	public RedirectView updateCargo(@PathVariable Integer id, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView("/driverPage", true);
		BindingResult bindingResult = new BindException(id, "id");
		driverService.changeStatus(DriverStatus.LOADING_AND_UNLOADING_CARGO.toString(), bindingResult);
		boolean isOrderCompleted = cargoAndOrdersService.updateCargoAndCheckOrderCompletion(id, bindingResult);
		if(isOrderCompleted) {
			driverService.changeStatus(DriverStatus.ON_SHIFT.toString(), bindingResult);
		}
		if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "driver", bindingResult);
		}
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
