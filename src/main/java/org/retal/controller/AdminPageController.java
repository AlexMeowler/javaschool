package org.retal.controller;

import java.util.List;

import org.retal.dao.UserDAO;
import org.retal.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AdminPageController 
{
	@RequestMapping(value = "/adminPage", method = RequestMethod.GET)
	public String getAdminPage(Model model)
	{
		List<User> users = userDAO.readAll();
		model.addAttribute("userList", users);
		return "adminPage";
	}
	
	/*@RequestMapping(value = "/adminPage", method = RequestMethod.POST)
	public String getLogged2(@RequestParam(value="j_login") String login, @RequestParam(value="j_password") String password, Model model)
	{
		List<User> users = userDAO.readAll();
		model.addAttribute("userList", users);
		return "adminPage";
	}*/
	
	@Autowired
	private UserDAO userDAO;
}
