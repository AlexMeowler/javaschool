package org.retal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DriverPageController 
{
	@RequestMapping("/driverPage")
	public String getDriverPage(Model model)
	{
		return "driverPage";
	}
}
