package org.retal.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {

	@GetMapping(value = "/")
	public String getHomeFromBlankPath(Model model) {
		return "redirect:/home";
	}

	@GetMapping("/home")
	public String getHome(Model model) {
		log.info("Redirected to home page");
		return "home";
	}

	private static final Logger log = Logger.getLogger(HomePageController.class);
}
