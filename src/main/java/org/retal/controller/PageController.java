package org.retal.controller;

import java.util.List;

import org.retal.domain.User;

import org.retal.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController 
{
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getHomeFromBlankPath(Model model)
	{
		return "redirect:/home";
	}
	
	@RequestMapping("/home")
	public String getHome(Model model)
	{
		return "home";
	}
	
	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public String logInHome(@RequestParam(value="j_login") String login, @RequestParam(value="j_password") String password)
	{
		return "home";
	}
	
	@RequestMapping(value = "/spring_auth", method = RequestMethod.POST)
	public void logInAuthPost(@RequestParam(value="j_login") String login, 
	        					@RequestParam(value="j_password") String password,
	        					Model model)
	{
		
	}
	
	@RequestMapping(value = "/spring_auth", method = RequestMethod.GET)
	public String logInAuthGet(@RequestParam(value = "error", required = false) String error,
	        				@RequestParam(value = "logout", required = false) String logout, Model model)
	{
		if(error != null)
		{
			model.addAttribute("message", "Invalid login or password");
		}
		if(logout != null)
		{
			model.addAttribute("message", "Logged out succesfully");
		}
		return "home";
	}
	
	@RequestMapping(value = "/logged", method = RequestMethod.GET)
	public String getLogged(Model model)
	{
		List<User> users = userDAO.readAll();
		model.addAttribute("userList", users);
		return "logged";
	}
	
	@RequestMapping(value = "/logged", method = RequestMethod.POST)
	public String getLogged2(@RequestParam(value="j_login") String login, @RequestParam(value="j_password") String password, Model model)
	{
		List<User> users = userDAO.readAll();
		model.addAttribute("userList", users);
		return "logged";
	}
	
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public void logOut(Model model)
	{
		
	}
	
	@Autowired
	private UserDAO userDAO;
}
