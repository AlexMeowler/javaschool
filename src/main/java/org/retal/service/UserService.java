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
import org.retal.dto.UserWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service, containing business-logic methods regarding {@linkplain org.retal.domain.User User} and
 * {@linkplain org.retal.domain.UserInfo UserInfo} entities.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class UserService {

  private UserDAO userDAO;

  private SessionInfo sessionInfo;

  private Validator userValidator;

  private CityService cityService;

  private static final Logger log = Logger.getLogger(UserService.class);

  public static final String DELETION_UPDATION_ERROR = "ERROR";

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public UserService(UserDAO userDAO, SessionInfo sessionInfo, UserValidator userValidator,
      CityService cityService) {
    this.userDAO = userDAO;
    this.sessionInfo = sessionInfo;
    this.userValidator = userValidator;
    this.cityService = cityService;
  }

  public List<User> getAllUsers() {
    return userDAO.readAll();
  }

  public User getUser(int id) {
    return userDAO.read(id);
  }

  public List<User> getAllDrivers() {
    return userDAO.readAllWithRole(UserRole.DRIVER.toString());
  }

  /**
   * Validates and adds new user to database.
   * 
   * @param user {@linkplain org.retal.domain.User User} to be added
   * @param bindingResult object to store validation result
   * @param password not hashed password to be validated
   */
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

  /**
   * Deletes user from database by ID.
   * 
   * @param id ID of user to be deleted
   */
  public String deleteUser(int id) {
    User target = userDAO.read(id);
    return deleteUser(target);
  }

  /**
   * Deletes user from database.
   * 
   * @param target user to be deleted
   * @return redirect address (for controllers) if method invoker has no rights to delete
   *         <b>target</b> user
   */
  public String deleteUser(User target) {
    User caller = sessionInfo.getCurrentUser();
    String redirect = "";
    if (userHasRightsToEditOrDeleteUser(caller, target)) {
      UserInfo userInfo = target.getUserInfo();
      if (userInfo.getCar() == null && userInfo.getOrder() == null) {
        userDAO.delete(target);
      } else {
        redirect = DELETION_UPDATION_ERROR;
      }
    } else {
      log.warn("Attempt to delete user without sufficient permissions");
      redirect = "/403";
    }
    return redirect;
  }

  /**
   * Updates user in database.
   * 
   * @param updatedUser user to be updates
   * @param bindingResult object to store validation result
   * @param password not hashed password to be validated
   * @return redirect address (for controllers) if method invoker has no rights to edit
   *         <b>updatedUser</b> user
   */
  public String updateUser(User updatedUser, BindingResult bindingResult, String password) {
    userValidator.validate(new UserWrapper(updatedUser, password), bindingResult);
    User correlationDB = userDAO.findUser(updatedUser.getLogin());
    if ((correlationDB != null) && (correlationDB.getId() != updatedUser.getId())) {
      bindingResult.reject("unique", "New login must be unique");
    }
    User caller = sessionInfo.getCurrentUser();
    String redirect = "";
    User target = userDAO.read(updatedUser.getId());
    UserInfo userInfo = target.getUserInfo();
    if (userInfo.getOrder() != null || userInfo.getCar() != null) {
      bindingResult.reject("userUnavailable",
          "User could not be updated due to assigned order or being driving car");
    }
    if (userHasRightsToEditOrDeleteUser(caller, target)) {
      if (!bindingResult.hasErrors()) {
        if (password.isEmpty()) {
          User copy = userDAO.read(updatedUser.getId());
          copy.setLogin(updatedUser.getLogin());
          copy.setRole(updatedUser.getRole());
          copy.setUserInfo(updatedUser.getUserInfo());
          updatedUser = copy;
        }
        userDAO.update(updatedUser);
      }
    } else {
      log.warn("Attempt to edit user without sufficient permissions");
      redirect = "/403/" + caller.getLogin();
    }
    return redirect;
  }

  /**
   * Checks if user A has rights to edit or delete user B.
   * 
   * @param caller user A
   * @param target user B
   * @return true is user A has right to edit or delete user B, false otherwise
   */
  public boolean userHasRightsToEditOrDeleteUser(User caller, User target) {
    String callerRoleString = caller.getRole().toUpperCase();
    String targetRoleString = target.getRole().toUpperCase();
    // target.getRole() != null ? target.getRole().toUpperCase() :
    // UserRole.DRIVER.toString().toUpperCase();
    UserRole callerRole = UserRole.valueOf(callerRoleString);
    UserRole targetRole = UserRole.valueOf(targetRoleString);
    boolean hasHigherRank = callerRole.ordinal() > targetRole.ordinal();
    boolean isAdmin = caller.getRole().equalsIgnoreCase(UserRole.ADMIN.toString());
    return hasHigherRank || isAdmin;
  }

  /**
   * Load names and surnames from text files and generates drivers based on them. One driver per
   * city.
   */
  public void addDriversFromFile() {
    try {
      BufferedReader namesReader = new BufferedReader(
          new InputStreamReader(UserService.class.getResourceAsStream("/names.txt")));
      BufferedReader surnamesReader = new BufferedReader(
          new InputStreamReader(UserService.class.getResourceAsStream("/surnames.txt")));
      String name;
      String surname;
      List<City> cities = cityService.getAllCities();
      int i = 0;
      while (i < cities.size() && (name = namesReader.readLine()) != null
          && (surname = surnamesReader.readLine()) != null) {
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
        userInfo.setCity(cities.get(i));
        userDAO.add(user);
        i++;
      }
      namesReader.close();
      surnamesReader.close();
    } catch (IOException e) {
      String message = "File names.txt or surnames.txt not found";
      log.error(message);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
  }

  /**
   * Sets the amount of hours at work during current month at 0 for all users. This method is only
   * called when month change occurred.
   */
  public void setUsersWorkedHoursToZero() {
    for (User u : userDAO.readAll()) {
      u.getUserInfo().setHoursWorked(0);
      userDAO.update(u);
    }
  }


}
