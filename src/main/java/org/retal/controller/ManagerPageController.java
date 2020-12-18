package org.retal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ManagerPageController 
{
	@RequestMapping(value = "/managerPage", method = RequestMethod.GET)
	public String getManagerPage(Model model)
	{
		return "managerPage";
	}
}
