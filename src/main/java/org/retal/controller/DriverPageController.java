package org.retal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DriverPageController {
	@GetMapping("/driverPage")
	public String getDriverPage(Model model) {
		return "driverPage";
	}
}
