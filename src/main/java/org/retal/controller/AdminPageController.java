package org.retal.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.retal.domain.*;
import org.retal.dto.UserDTO;
import org.retal.dto.UserInfoDTO;
import org.retal.service.UserService;
import org.retal.service.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class AdminPageController {

	@GetMapping(value = ADMIN_PAGE)
	public String getAdminPage(Model model) {
		BindingResult result = (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "user");
		Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
		for (Map.Entry<String, String> e : errors.entrySet()) {
			log.info(e.getKey() + ":" + e.getValue());
		}
		model.addAllAttributes(errors);
		List<User> users = userEditor.getAllUsers();
		model.addAttribute("userList", users);
		return "adminPage";
	}

	@PostMapping(value = "/addNewUser")
	public RedirectView addNewUser(UserDTO userDTO, BindingResult bindingResult, 
			UserInfoDTO userInfoDTO, RedirectAttributes redir) {
		log.info("Attempt to add new user");
		RedirectView redirectView = new RedirectView(ADMIN_PAGE, true);
		redir.addFlashAttribute("visible", "true");
		User user = new User(userDTO);
		UserInfo userInfo = new UserInfo(userInfoDTO);
		user.setUserInfo(userInfo);
		userEditor.addNewUser(user, bindingResult, userDTO.getPassword());
		if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
			redir.addFlashAttribute("user", user);
		}
		return redirectView;
	}

	@GetMapping(value = "/deleteUser/{id}")
	public RedirectView delete(@PathVariable Integer id, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView(ADMIN_PAGE, true);
		userEditor.deleteUser(id);
		return redirectView;
	}

	@GetMapping(value = "/editUser/{id}")
	public RedirectView edit(@PathVariable Integer id, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView("/editUser", true);
		redir.addFlashAttribute("user", userEditor.getUser(id));
		redir.addFlashAttribute("we", sessionInfo.getCurrentUser());
		return redirectView;
	}

	@GetMapping(value = "/editUser")
	public String editForm(Model model) {
		BindingResult result = (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "user");
		Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
		model.addAllAttributes(errors);
		model.addAttribute("editUser", "/submitEditedUser");
		return "editUser";
	}

	@PostMapping(value = "/submitEditedUser")
	public RedirectView finishEditing(UserDTO userDTO, BindingResult bindingResult, 
			UserInfoDTO userInfoDTO, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView(ADMIN_PAGE, true);
		User user = new User(userDTO);
		UserInfo userInfo = new UserInfo(userInfoDTO);
		user.setUserInfo(userInfo);
		userEditor.updateUser(user, bindingResult, userDTO.getPassword());
		if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
			redir.addFlashAttribute("user", user);
			redirectView.setUrl("/editUser");
		}
		return redirectView;
	}

	@Autowired
	private UserService userEditor;

	@Autowired
	private SessionInfo sessionInfo;
	
	private static final String ADMIN_PAGE = "/adminPage";

	private static final Logger log = Logger.getLogger(AdminPageController.class);
}
