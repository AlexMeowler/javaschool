package org.retal.logiweb.service.logic.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.log4j.Logger;
import org.retal.logiweb.dao.impl.UserDAO;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.SessionInfo;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.domain.enums.DriverStatus;
import org.retal.logiweb.domain.enums.UserRole;
import org.retal.logiweb.dto.UserWrapper;
import org.retal.logiweb.service.jms.NotificationSender;
import org.retal.logiweb.service.logic.interfaces.UserServices;
import org.retal.logiweb.service.validators.UserValidator;
import org.retal.table.jms.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.server.ResponseStatusException;

/**
 * Implementation of {@link UserServices} interface.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class UserService implements UserServices {

  private final UserDAO userDAO;

  private final SessionInfo sessionInfo;

  private final Validator userValidator;

  private final CityService cityService;

  private final NotificationSender sender;

  private static final Logger log = Logger.getLogger(UserService.class);

  public static final String DELETION_UPDATION_ERROR = "ERROR";

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public UserService(UserDAO userDAO, SessionInfo sessionInfo, UserValidator userValidator,
      CityService cityService, NotificationSender sender) {
    this.userDAO = userDAO;
    this.sessionInfo = sessionInfo;
    this.userValidator = userValidator;
    this.cityService = cityService;
    this.sender = sender;
  }

  public List<User> getAllUsers() {
    return userDAO.readAll();
  }

  /**
   * Calls {@link UserDAO#readRows(int, int)} method.
   * @see org.retal.logiweb.dao.interfaces.PartRowsReader#readRows(int, int)
   */
  public List<User> getPartUsers(int from, int amount) {
    return userDAO.readRows(from, amount);
  }

  /**
   * Calls {@link UserDAO#getRowsAmount()} method.
   * @see org.retal.logiweb.dao.interfaces.CountableRows#getRowsAmount()
   */
  public int getRowsAmount() {
    return userDAO.getRowsAmount();
  }

  public User getUser(int id) {
    return userDAO.read(id);
  }

  public List<User> getAllDrivers() {
    return userDAO.readAllWithRole(UserRole.DRIVER.toString());
  }

  @Override
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
      if (user.getRole().equalsIgnoreCase(UserRole.DRIVER.toString())) {
        sender.send(NotificationType.DRIVERS_UPDATE);
      }
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

  @Override
  public String deleteUser(User target) {
    User caller = sessionInfo.getCurrentUser();
    String redirect = "";
    if (userHasRightsToEditOrDeleteUser(caller, target)) {
      UserInfo userInfo = target.getUserInfo();
      if (userInfo.getCar() == null && userInfo.getOrder() == null) {
        userDAO.delete(target);
        if (target.getRole().equalsIgnoreCase(UserRole.DRIVER.toString())) {
          sender.send(NotificationType.DRIVERS_UPDATE);
        }
      } else {
        redirect = DELETION_UPDATION_ERROR;
      }
    } else {
      log.warn("Attempt to delete user without sufficient permissions");
      redirect = "/403";
    }
    return redirect;
  }

  @Override
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
        if (updatedUser.getRole().equalsIgnoreCase(UserRole.DRIVER.toString())) {
          sender.send(NotificationType.DRIVERS_UPDATE);
        }
      }
    } else {
      log.warn("Attempt to edit user without sufficient permissions");
      redirect = "/403/" + caller.getLogin();
    }
    return redirect;
  }

  @Override
  public boolean userHasRightsToEditOrDeleteUser(User caller, User target) {
    String callerRoleString = caller.getRole().toUpperCase();
    String targetRoleString = target.getRole().toUpperCase();
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
    addDriversFromFile("names", "surnames");
  }

  /**
   * Load names and surnames from text files and generates drivers based on them. One driver per
   * city.
   * 
   * @param namesFileName name of text file with names
   * @param surnamesFileName name of text file with surnames
   */
  public void addDriversFromFile(String namesFileName, String surnamesFileName) {
    try {
      BufferedReader namesReader = new BufferedReader(new InputStreamReader(
          UserService.class.getResourceAsStream("/" + namesFileName + ".txt")));
      BufferedReader surnamesReader = new BufferedReader(new InputStreamReader(
          UserService.class.getResourceAsStream("/" + surnamesFileName + ".txt")));
      String name;
      String surname;
      List<City> cities = cityService.getAllCities();
      int i = 0;
      while (i < cities.size() && (name = namesReader.readLine()) != null
          && (surname = surnamesReader.readLine()) != null) {
        String login = "d" + name.substring(0, 3) + surname.substring(0, 3);
        UserInfo userInfo = new UserInfo(name, surname,
            DriverStatus.ON_SHIFT.toString().toLowerCase(), cities.get(i));
        User user = new User(login, login, UserRole.DRIVER.toString().toLowerCase(), userInfo);
        userDAO.add(user);
        i++;
      }
      namesReader.close();
      surnamesReader.close();
    } catch (IOException e) {
      String message = "I/O error has occurred";
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message, e);
    } catch (NullPointerException e) {
      String message = "File " + namesFileName + ".txt or " + surnamesFileName + ".txt not found";
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, message, e);
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
