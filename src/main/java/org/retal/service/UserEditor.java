package org.retal.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.retal.controller.ManagerPageController;
import org.retal.dao.UserDAO;
import org.retal.dao.UserInfoDAO;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

@Service
public class UserEditor 
{
	public List<User> getAllUsers()
	{
		return userDAO.readAll();
	}
	
	public User getUser(int id)
	{
		return userDAO.read(id);
	}
	
	public List<User> getAllDrivers()
	{
		return userDAO.readAllWithRole(UserRole.DRIVER.toString());
	}
	
	public void addNewUser(User user, BindingResult bindingResult)
	{
		userValidator.validate(user, bindingResult);
		if(userDAO.find(user.getLogin()) != null)
		{
			bindingResult.reject("unique", "Login must be unique");
		}
		user.setRealPassword(null);
		if(!bindingResult.hasErrors())
		{
			userDAO.add(user);
		}
	}
	
	public void delete(int id)
	{
		User target = userDAO.read(id);
		delete(target);
	}
	
	public String delete(User target)
	{
		User caller = sessionInfo.getCurrentUser();
		String redirect = "";
		if(userHasRightsToEditOrDeleteUser(caller, target))
		{
			userDAO.delete(target);
		}
		else
		{
			log.warn("Attempt to delete user without sufficient permissions");
			redirect = "/403";
		}
		return redirect;
	}
	
	public String updateUser(User updatedUser, BindingResult bindingResult)
	{
		userValidator.validate(updatedUser, bindingResult);
		User correlationDB = userDAO.find(updatedUser.getLogin());
		if((correlationDB != null) && (correlationDB.getId() != updatedUser.getId()))
		{
			bindingResult.reject("unique", "Login must be unique");
		}
		User caller = sessionInfo.getCurrentUser();
		String redirect = "";
		User target = userDAO.read(updatedUser.getId());
		if(!bindingResult.hasErrors() && userHasRightsToEditOrDeleteUser(caller, target))
		{
			userDAO.update(updatedUser);
		}
		else
		{
			log.warn("Attempt to edit user without sufficient permissions");
			redirect = "/403";
		}
		return redirect;
	}
	
	private boolean userHasRightsToEditOrDeleteUser(User caller, User target)
	{
		String callerRoleString = caller.getRole().toUpperCase();
		String targetRoleString = target.getRole().toUpperCase();
		UserRole callerRole = UserRole.valueOf(callerRoleString);
		UserRole targetRole = UserRole.valueOf(targetRoleString);
		boolean hasHigherRank = callerRole.ordinal() > targetRole.ordinal(); 
		boolean isAdmin = caller.getRole().equals(UserRole.ADMIN.toString().toLowerCase());
		return hasHigherRank || isAdmin;
	}
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private SessionInfo sessionInfo;
	
	@Autowired
	@Qualifier("userEditorValidator")
	private Validator userValidator;
	
	private static final Logger log = Logger.getLogger(UserEditor.class);
}
