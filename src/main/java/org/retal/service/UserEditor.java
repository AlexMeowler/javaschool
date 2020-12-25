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
	
	public void addNewUser(User user, BindingResult bindingResult, String password)
	{
		userValidator.validate(new UserWrapper(user, password), bindingResult);
		if(password.length() < 6)
		{
			String property = "realPassword";
			String message = "Password must have at least 6 characters";
			log.info(property + " : " + message);
			bindingResult.reject(property, message);
		}
		if(userDAO.find(user.getLogin()) != null)
		{
			bindingResult.reject("unique", "Login must be unique");
		}
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
	
	public String updateUser(User updatedUser, BindingResult bindingResult, String password)
	{
		userValidator.validate(new UserWrapper(updatedUser, password), bindingResult);
		User correlationDB = userDAO.find(updatedUser.getLogin());
		if((correlationDB != null) && (correlationDB.getId() != updatedUser.getId()))
		{
			bindingResult.reject("unique", "New login must be unique");
		}
		User caller = sessionInfo.getCurrentUser();
		String redirect = "";
		User target = userDAO.read(updatedUser.getId());
		if(!bindingResult.hasErrors() && userHasRightsToEditOrDeleteUser(caller, target))
		{
			if(password.isEmpty())
			{
				User copy = userDAO.read(updatedUser.getId());
				copy.setLogin(updatedUser.getLogin());
				copy.setRole(updatedUser.getRole());
				copy.setUserInfo(updatedUser.getUserInfo());
				updatedUser = copy;
			}
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
	
	public class UserWrapper
	{
		public UserWrapper(User user, String password)
		{
			this.user = user;
			this.password = password;
		}
		
		public User getUser()
		{
			return user;
		}
		
		public String getPassword()
		{
			return password;
		}
		private User user;
		private String password;
	}
}
