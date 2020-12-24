package org.retal.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.retal.dao.CarDAO;
import org.retal.dao.UserDAO;
import org.retal.domain.Car;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.domain.UserRole;
import org.retal.service.UserEditor;
import org.retal.service.UserEditorValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ManagerPageController 
{
	@RequestMapping(value = "/managerPage", method = RequestMethod.GET)
	public String getManagerPage(Model model)
	{
		if (model.containsAttribute("user")) 
		{
	        User user = (User)model.getAttribute("user");
	        model.addAttribute("login", user.getLogin());
	        model.addAttribute("role", user.getRole());
	        model.addAttribute("name", user.getUserInfo().getName());
	        model.addAttribute("surname", user.getUserInfo().getSurname());
	    }
		BindingResult result = (BindingResult)model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "user");
		Map<String, String> errors = UserEditorValidator.convertErrorsToHashMap(result);
		model.addAllAttributes(errors);
		User user = sessionInfo.getCurrentUser();
		UserInfo userInfo = user.getUserInfo();
		model.addAttribute("current_user_name", userInfo.getName() + " " + userInfo.getSurname());
		List<User> users = userEditor.getAllDrivers();
		model.addAttribute("driverList", users);
		List<Car> cars = carDAO.readAll();
		model.addAttribute("carsList", cars);
		return "managerPage";
	}
	
	@RequestMapping(value = "/addNewDriver", method = RequestMethod.POST)
	public RedirectView addNewUser(User user, UserInfo userInfo, RedirectAttributes redir,
			BindingResult bindingResult, @RequestParam(name = "password") String password)
	{
		log.info("Attempt to add new driver");
		RedirectView redirectView = new RedirectView("/managerPage", true);
		redir.addFlashAttribute("visible", "true");
		userInfo.setUser(user);
		user.setRole("driver");
		user.setUserInfo(userInfo);
		user.setRealPassword(password);
		userEditor.addNewUser(user, bindingResult);
		redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
		redir.addFlashAttribute("user", user);
		return redirectView;
	}
	
	@RequestMapping(value = "/deleteDriver/{id}")
	public RedirectView delete(@PathVariable Integer id, RedirectAttributes redir)
	{
		RedirectView redirectView = new RedirectView("/managerPage", true);
		User user = userEditor.getUser(id);
		String url403 = userEditor.delete(user);
		if(!url403.isEmpty())
		{
			String param = sessionInfo.getCurrentUser().getLogin();
			redirectView.setUrl(url403 + "/" + param);
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
