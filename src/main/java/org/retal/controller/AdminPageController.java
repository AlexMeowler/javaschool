package org.retal.controller;

import java.util.List;

import org.retal.dao.UserDAO;
import org.retal.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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
	
	@RequestMapping(value = "/addNewUser", method = RequestMethod.POST)
	public RedirectView addNewUser(User user, RedirectAttributes redir)
	{
		RedirectView redirectView = new RedirectView("/adminPage", true);
		redir.addFlashAttribute("visible", "true");
		userDAO.add(user);
		return redirectView;
	}
	
	@RequestMapping(value = "/deleteUser/{id}")
	public RedirectView delete(@PathVariable Integer id, RedirectAttributes redir)
	{
		RedirectView redirectView = new RedirectView("/adminPage", true);
		userDAO.deleteById(id);
		return redirectView;
	}
	
	@Autowired
	private UserDAO userDAO;
}
