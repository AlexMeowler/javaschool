package org.retal.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.retal.dao.UserDAO;
import org.retal.dao.UserInfoDAO;
import org.retal.domain.*;
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
	public RedirectView addNewUser(User user, UserInfo userInfo, RedirectAttributes redir)
	{
		RedirectView redirectView = new RedirectView("/adminPage", true);
		redir.addFlashAttribute("visible", "true");
		//userInfo.setId(user.getId());
		userInfo.setUser(user);
		user.setUserInfo(userInfo);
		//userDAO.add(user);
		userInfoDAO.add(userInfo);
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
	
	@Autowired
	private UserInfoDAO userInfoDAO;
	
	private static final Logger log = Logger.getLogger(AdminPageController.class);
}
