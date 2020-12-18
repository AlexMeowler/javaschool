package org.retal.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomePageController 
{
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getHomeFromBlankPath(Model model)
	{
		return "redirect:/home";
	}
	
	@RequestMapping("/home")
	public String getHome(Model model)
	{
		log.info("Redirected to home page");
		return "home";
	}
	
	/*@RequestMapping(value = "/home", method = RequestMethod.POST)
	public String logInHome(@RequestParam(value="j_login") String login, @RequestParam(value="j_password") String password)
	{
		return "home";
	}*/
	
	private static final Logger log = Logger.getLogger(HomePageController.class);
}
