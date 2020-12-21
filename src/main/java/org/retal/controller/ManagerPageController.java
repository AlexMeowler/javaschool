package org.retal.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.retal.dao.UserDAO;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.domain.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ManagerPageController 
{
	@RequestMapping(value = "/managerPage", method = RequestMethod.GET)
	public String getManagerPage(Model model)
	{
		User user = sessionInfo.getCurrentUser();
		UserInfo userInfo = user.getUserInfo();
		model.addAttribute("name", userInfo.getName() + " " + userInfo.getSurname());
		List<User> users = userDAO.readAllWithRole("driver");
		model.addAttribute("driverList", users);
		return "managerPage";
	}
	
	@RequestMapping(value = "/addNewDriver", method = RequestMethod.POST)
	public RedirectView addNewUser(User user, UserInfo userInfo, RedirectAttributes redir)
	{
		RedirectView redirectView = new RedirectView("/managerPage", true);
		redir.addFlashAttribute("visible", "true");
		userInfo.setUser(user);
		user.setRole("driver");
		user.setUserInfo(userInfo);
		userDAO.add(user);
		return redirectView;
	}
	
	@RequestMapping(value = "/deleteDriver/{id}")
	public RedirectView delete(@PathVariable Integer id, RedirectAttributes redir)
	{
		RedirectView redirectView = new RedirectView("/managerPage", true);
		User user = userDAO.read(id);
		User we = sessionInfo.getCurrentUser();
		if(user.getRole().equals(UserRole.DRIVER.toString().toLowerCase()))
		{
			userDAO.deleteById(id);
		}
		else
		{
			String ourLogin = we.getLogin();
			String ourRole = we.getRole();
			String targetLogin = user.getLogin();
			String targetRole = user.getRole();
			String warnMessage = String.format("Attempt to delete user without appropriate rights. "
					+ "Source: login = '%s', role = '%s'. Target: login = '%s', role = '%s'", ourLogin, ourRole, targetLogin, targetRole);
			log.warn(warnMessage);
			redirectView.setUrl("/403");
		}
		return redirectView;
	}
	
	@Autowired
	private SessionInfo sessionInfo;
	
	@Autowired
	private UserDAO userDAO;
	
	private static final Logger log = Logger.getLogger(ManagerPageController.class);
}
