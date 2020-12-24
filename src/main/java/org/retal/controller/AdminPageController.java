package org.retal.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.retal.domain.*;
import org.retal.service.UserEditor;
import org.retal.service.UserEditorValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
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
		List<User> users = userEditor.getAllUsers();
		model.addAttribute("userList", users);
		return "adminPage";
	}
	
	@RequestMapping(value = "/addNewUser", method = RequestMethod.POST)
	public RedirectView addNewUser(@ModelAttribute("user") User user, BindingResult bindingResult, UserInfo userInfo, 
			RedirectAttributes redir, @RequestParam(name = "password") String password)
	{
		log.info("Attempt to add new user");
		RedirectView redirectView = new RedirectView("/adminPage", true);
		redir.addFlashAttribute("visible", "true");
		user.setUserInfo(userInfo);
		user.setRealPassword(password);
		userEditor.addNewUser(user, bindingResult);
		redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
		redir.addFlashAttribute("user", user);
		return redirectView;
	}
	
	@RequestMapping(value = "/deleteUser/{id}")
	public RedirectView delete(@PathVariable Integer id, RedirectAttributes redir)
	{
		RedirectView redirectView = new RedirectView("/adminPage", true);
		userEditor.delete(id);
		return redirectView;
	}
	
	@Autowired
	private UserEditor userEditor;
	
	private static final Logger log = Logger.getLogger(AdminPageController.class);
}
