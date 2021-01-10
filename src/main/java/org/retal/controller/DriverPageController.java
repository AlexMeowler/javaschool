package org.retal.controller;

import java.util.ArrayList;
import java.util.List;

import org.retal.dao.OrderDAO;
import org.retal.domain.Order;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.domain.enums.DriverStatus;
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
		for(String s : order.getRoute().split(";")) {
			routeList.add(s);
		}
		model.addAttribute("routeList", routeList);
		return "driverPage";
	}
	
	@GetMapping(value = "/changeStatus/{status}")
	public RedirectView changeStatus(@PathVariable String status, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView("/driverPage", true);
		driverService.changeStatus(status);
		return redirectView;
	}
	
	@Autowired
	private SessionInfo sessionInfo;	
	
	@Autowired
	private DriverService driverService;	
	
	@Autowired
	private OrderDAO orderDAO;	
}
