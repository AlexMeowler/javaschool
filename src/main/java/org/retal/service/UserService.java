package org.retal.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.retal.dao.UserDAO;
import org.retal.domain.City;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.domain.enums.DriverStatus;
import org.retal.domain.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

@Service
public class UserService {
	
	public List<User> getAllUsers() {
		return userDAO.readAll();
	}

	public User getUser(int id) {
		return userDAO.read(id);
	}

	public List<User> getAllDrivers() {
		return userDAO.readAllWithRole(UserRole.DRIVER.toString());
	}

	public void addNewUser(User user, BindingResult bindingResult, String password) {
		userValidator.validate(new UserWrapper(user, password), bindingResult);
		if (password.length() < 6) {
			String property = "realPassword";
			String message = "Password must have at least 6 characters";
			log.info(property + " : " + message);
			bindingResult.reject(property, message);
		}
		if (userDAO.findUser(user.getLogin()) != null) {
			bindingResult.reject("unique", "Login must be unique");
		}
		if (!bindingResult.hasErrors()) {
			userDAO.add(user);
		}
	}

	public void deleteUser(int id) {
		User target = userDAO.read(id);
		deleteUser(target);
	}

	public String deleteUser(User target) {
		User caller = sessionInfo.getCurrentUser();
		String redirect = "";
		if (userHasRightsToEditOrDeleteUser(caller, target)) {
			userDAO.delete(target);
		} else {
			log.warn("Attempt to delete user without sufficient permissions");
			redirect = "/403";
		}
		return redirect;
	}

	public String updateUser(User updatedUser, BindingResult bindingResult, String password) {
		userValidator.validate(new UserWrapper(updatedUser, password), bindingResult);
		User correlationDB = userDAO.findUser(updatedUser.getLogin());
		if ((correlationDB != null) && (correlationDB.getId() != updatedUser.getId())) {
			bindingResult.reject("unique", "New login must be unique");
		}
		User caller = sessionInfo.getCurrentUser();
		String redirect = "";
		User target = userDAO.read(updatedUser.getId());
		if (!bindingResult.hasErrors() && userHasRightsToEditOrDeleteUser(caller, target)) {
			if (password.isEmpty()) {
				User copy = userDAO.read(updatedUser.getId());
				copy.setLogin(updatedUser.getLogin());
				copy.setRole(updatedUser.getRole());
				copy.setUserInfo(updatedUser.getUserInfo());
				updatedUser = copy;
			}
			userDAO.update(updatedUser);
		} else {
			log.warn("Attempt to edit user without sufficient permissions");
			redirect = "/403";
		}
		return redirect;
	}

	private boolean userHasRightsToEditOrDeleteUser(User caller, User target) {
		String callerRoleString = caller.getRole().toUpperCase();
		String targetRoleString = target != null ? target.getRole().toUpperCase() : UserRole.DRIVER.toString().toUpperCase();
		UserRole callerRole = UserRole.valueOf(callerRoleString);
		UserRole targetRole = UserRole.valueOf(targetRoleString);
		boolean hasHigherRank = callerRole.ordinal() > targetRole.ordinal();
		boolean isAdmin = caller.getRole().equalsIgnoreCase(UserRole.ADMIN.toString());
		return hasHigherRank || isAdmin;
	}
	
	public void addDriversFromFile() {
		try {
			BufferedReader namesReader = new BufferedReader(new InputStreamReader(UserService.class.getResourceAsStream("/names.txt")));
			BufferedReader surnamesReader = new BufferedReader(new InputStreamReader(UserService.class.getResourceAsStream("/surnames.txt")));
			String name, surname;
			List<City> cities = cityService.getAllCities();
			int i = 0;
			while(i < cities.size() && (name = namesReader.readLine()) != null && (surname = surnamesReader.readLine()) != null) {
				User user = new User();
				UserInfo userInfo = new UserInfo();
				String login = "d" + name.substring(0, 3) + surname.substring(0, 3);
				user.setLogin(login);
				user.setPassword(login);
				user.setRole(UserRole.DRIVER.toString().toLowerCase());
				user.setUserInfo(userInfo);
				userInfo.setName(name);
				userInfo.setSurname(surname);
				userInfo.setStatus(DriverStatus.ON_SHIFT.toString().toLowerCase());
				userInfo.setCurrentCity(cities.get(i));
				userDAO.add(user);
				i++;
			}
			namesReader.close();
			surnamesReader.close();
		} catch (IOException e) {
			log.error("File names.txt or surnames.txt not found");
		}
	}

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private SessionInfo sessionInfo;

	@Autowired
	@Qualifier("userValidator")
	private Validator userValidator;
	
	@Autowired
	private CityService cityService;

	private static final Logger log = Logger.getLogger(UserService.class);

	public class UserWrapper {
		public UserWrapper(User user, String password) {
			this.user = user;
			this.password = password;
		}

		public User getUser() {
			return user;
		}

		public String getPassword() {
			return password;
		}

		private User user;
		private String password;
	}
}
