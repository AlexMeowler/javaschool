package org.retal.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.retal.dao.CarDAO;
import org.retal.dao.UserDAO;
import org.retal.domain.Car;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.domain.UserRole;
import org.retal.service.UserEditor;
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
		List<User> users = userEditor.getAllDrivers();
		model.addAttribute("driverList", users);
		List<Car> cars = carDAO.readAll();
		model.addAttribute("carsList", cars);
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
		userEditor.addNewUser(user);
		return redirectView;
	}
	
	@RequestMapping(value = "/deleteDriver/{id}")
	public RedirectView delete(@PathVariable Integer id, RedirectAttributes redir)
	{
		RedirectView redirectView = new RedirectView("/managerPage", true);
		User user = userEditor.getUser(id);
		User we = sessionInfo.getCurrentUser();
		String url403 = userEditor.deleteWithRoleChecking(we, user);
		if(!url403.isEmpty())
		{
			redirectView.setUrl(url403);
		}
		return redirectView;
	}
	
	@Autowired
	private SessionInfo sessionInfo;
	
	@Autowired
	private UserEditor userEditor;
	
	@Autowired 
	private CarDAO carDAO;
	
	private static final Logger log = Logger.getLogger(ManagerPageController.class);
}
