package org.retal.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class AuthAndLogoutController 
{
	/*@RequestMapping(value = "/spring_auth", method = RequestMethod.POST)
	public void logInAuthPost(@RequestParam(value="j_login") String login, 
	        					@RequestParam(value="j_password") String password,
	        					Model model)
	{
		log.info("auth attempt");
	}*/
	
	@RequestMapping(value = "/spring_auth", method = RequestMethod.GET)
	public RedirectView logInAuthGet(@RequestParam(value = "error", required = false) String error,
	        				@RequestParam(value = "logout", required = false) String logout, RedirectAttributes redir)
	{
		RedirectView redirectView = new RedirectView("/home", true);
		if(error != null)
		{
			log.info("auth fail");
			redir.addFlashAttribute("message", "Invalid login or password");
		}
		if(logout != null)
		{
			log.info("logout success");
			redir.addFlashAttribute("message", "Logged out succesfully");
		}
		return redirectView;
	}
	
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public void logOut(Model model)
	{
		log.info("logout attempt");
	}
	
	private static final Logger log = Logger.getLogger(AuthAndLogoutController.class);
}
