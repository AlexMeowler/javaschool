package org.retal.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.retal.controller.ManagerPageController;
import org.retal.dao.UserDAO;
import org.retal.dao.UserInfoDAO;
import org.retal.domain.User;
import org.retal.domain.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
		return userDAO.readAllWithRole("driver");
	}
	
	public void addNewUser(User user)
	{
		userDAO.add(user);
	}
	
	public void delete(int id)
	{
		userDAO.deleteById(id);
	}
	
	public void delete(User user)
	{
		userDAO.delete(user);
	}
	
	public String deleteWithRoleChecking(User caller, User target)
	{
		String redirect = "";
		if(userHasRightsToDeleteUser(caller, target))
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
	
	private boolean userHasRightsToDeleteUser(User caller, User target)
	{
		String callerRoleString = caller.getRole().toUpperCase();
		String targetRoleString = target.getRole().toUpperCase();
		UserRole callerRole = UserRole.valueOf(callerRoleString);
		UserRole targetRole = UserRole.valueOf(targetRoleString);
		return callerRole.ordinal() > targetRole.ordinal();
	}
	
	@Autowired
	private UserDAO userDAO;
	
	private static final Logger log = Logger.getLogger(UserEditor.class);
}
