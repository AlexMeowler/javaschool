package org.retal.controller;

import java.util.List;

import org.retal.domain.User;

import org.retal.dao.UserDAO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PageController 
{
	@RequestMapping("/")
	public String getHome(Model model)
	{
		return "home";
	}
	
	@RequestMapping(value = "/logged", method = RequestMethod.GET)
	public String getLogged(Model model)
	{
		List<User> users = userDAO.readAll();
		model.addAttribute("userList", users);
		return "logged";
	}
	
	private UserDAO userDAO = new UserDAO();
}
