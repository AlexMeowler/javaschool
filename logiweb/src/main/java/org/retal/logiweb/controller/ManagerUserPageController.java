package org.retal.logiweb.controller;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.SessionInfo;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.domain.enums.UserRole;
import org.retal.logiweb.dto.CityDTO;
import org.retal.logiweb.dto.UserDTO;
import org.retal.logiweb.dto.UserInfoDTO;
import org.retal.logiweb.service.logic.impl.CityService;
import org.retal.logiweb.service.logic.impl.UserService;
import org.retal.logiweb.service.validators.UserValidator;
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
public class ManagerUserPageController {

  private final SessionInfo sessionInfo;

  private final UserService userService;

  private final CityService cityService;

  public static final String MANAGER_USERS_PAGE = "/managerUsersPage";

  private static final String CITY_LIST_ATTRIBUTE_NAME = "cityList";

  private static final int USERS_PER_PAGE = 10;

  private static final Logger log = Logger.getLogger(ManagerUserPageController.class);

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public ManagerUserPageController(SessionInfo sessionInfo, UserService userService,
      CityService cityService) {
    this.sessionInfo = sessionInfo;
    this.userService = userService;
    this.cityService = cityService;
  }

  /**
   * Method responsible for redirecting to drivers page.
   */
  @GetMapping(value = MANAGER_USERS_PAGE)
  public RedirectView getManagerUserPage(Model model, RedirectAttributes redir) {
    model.asMap().forEach(redir::addFlashAttribute);
    return new RedirectView(MANAGER_USERS_PAGE + "/1", true);
  }

  /**
   * Paginated view for drivers page.
   */
  @GetMapping(value = MANAGER_USERS_PAGE + "/{page}")
  public String getPartUsers(Model model, @PathVariable Integer page) {
    int maxPage = getMaxPossiblePage();
    if (page > maxPage) {
      return "redirect:" + MANAGER_USERS_PAGE + "/" + maxPage;
    }
    if (page < 1) {
      return "redirect:" + MANAGER_USERS_PAGE + "/1";
    }
    BindingResult userResult =
        (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "user");
    Map<String, String> errors = UserValidator.convertErrorsToHashMap(userResult);
    for (Map.Entry<String, String> e : errors.entrySet()) {
      log.debug(e.getKey() + ":" + e.getValue());
    }
    model.addAllAttributes(errors);
    User user = sessionInfo.getCurrentUser();
    UserInfo userInfo = user.getUserInfo();
    model.addAttribute("current_user_name", userInfo.getName() + " " + userInfo.getSurname());
    List<User> users = getDriversForGivenPage(page);
    model.addAttribute("driverList", users);
    List<City> cities = cityService.getAllCities();
    model.addAttribute(CITY_LIST_ATTRIBUTE_NAME, cities);
    model.addAttribute("page", page);
    model.addAttribute("maxPage", maxPage);
    return "managerUsers";
  }

  /**
   * Method responsible for adding new drivers to database using
   * {@linkplain org.retal.logiweb.service.logic.impl.UserService service layer}.
   * 
   * @see org.retal.logiweb.domain.entity.User
   */
  @PostMapping(value = "/addNewDriver")
  public RedirectView addNewDriver(UserDTO userDTO, UserInfoDTO userInfoDTO, CityDTO cityDTO,
      RedirectAttributes redir, BindingResult bindingResult) {
    log.info("Attempt to add new driver");
    redir.addFlashAttribute("visibledriver", "true");
    User user = mapUserRelatedDTOsToDriverEntity(userDTO, userInfoDTO, cityDTO);
    userService.addNewUser(user, bindingResult, userDTO.getPassword());
    if (bindingResult.hasErrors()) {
      log.warn("Validation failed when adding new driver");
      redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
      redir.addFlashAttribute("user", user);
    }
    return new RedirectView(MANAGER_USERS_PAGE, true);
  }

  /**
   * Method responsible for attempt to delete driver from database when button is clicked using
   * {@linkplain org.retal.logiweb.service.logic.impl.UserService service layer}.
   * 
   * @see org.retal.logiweb.domain.entity.User
   */
  @GetMapping(value = "/deleteDriver/{id}")
  public RedirectView deleteDriver(@PathVariable Integer id, RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView(MANAGER_USERS_PAGE, true);
    String url403 = userService.deleteUser(id);
    if (!url403.isEmpty() && !url403.equals(UserService.DELETION_UPDATION_ERROR)) {
      String param = sessionInfo.getCurrentUser().getLogin();
      redirectView.setUrl(url403 + "/" + param);
    }
    if (url403.equals(UserService.DELETION_UPDATION_ERROR)) {
      redir.addFlashAttribute("error_userDeletionFailed",
          "Could not delete driver due to assigned order or being driving car");
    }
    return redirectView;
  }

  /**
   * Method responsible for redirecting to driver editor page.
   * 
   * @see org.retal.logiweb.domain.entity.User
   */
  @GetMapping(value = "/editDriver/{id}")
  public RedirectView editDriver(@PathVariable Integer id, RedirectAttributes redir) {
    log.debug("Edit driver");
    RedirectView redirectView = new RedirectView("/editDriver", true);
    User we = sessionInfo.getCurrentUser();
    User target = userService.getUser(id);
    if (userService.userHasRightsToEditOrDeleteUser(we, target)) {
      redir.addFlashAttribute("user", target);
      redir.addFlashAttribute("we", we);
    } else {
      redirectView.setUrl("/403/" + we.getLogin());
      log.debug(redirectView.getUrl());
    }
    return redirectView;
  }

  /**
   * Method responsible for showing driver editor page.
   * 
   * @see org.retal.logiweb.domain.entity.User
   */
  @GetMapping(value = "/editDriver")
  public String driverEditForm(Model model) {
    BindingResult result =
        (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "user");
    Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
    model.addAllAttributes(errors);
    model.addAttribute("editUser", "/submitEditedDriver");
    List<City> cities = cityService.getAllCities();
    model.addAttribute(CITY_LIST_ATTRIBUTE_NAME, cities);
    return "editUser";
  }

  /**
   * Method responsible for submitting edited driver to
   * {@linkplain org.retal.logiweb.service.logic.impl.UserService service layer} which will update
   * entity if input is valid.
   * 
   * @see org.retal.logiweb.domain.entity.User
   */
  @PostMapping(value = "/submitEditedDriver")
  public RedirectView finishDriverEditing(UserDTO userDTO, BindingResult bindingResult,
      UserInfoDTO userInfoDTO, CityDTO cityDTO, RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView(MANAGER_USERS_PAGE, true);
    User user = mapUserRelatedDTOsToDriverEntity(userDTO, userInfoDTO, cityDTO);
    String redirect = userService.updateUser(user, bindingResult, userDTO.getPassword());
    if (bindingResult.hasErrors()) {
      log.warn("There were validation errors at editing driver");
      redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
      redir.addFlashAttribute("user", user);
      redirectView.setUrl("/editDriver");
    }
    if (!redirect.isEmpty()) {
      redirectView.setUrl(redirect);
    }
    return redirectView;
  }

  /**
   * Method for mapping user related DTOs (user, user info, city) to
   * {@linkplain org.retal.logiweb.domain.entity.User User} entity with user role set to 'driver'.
   * 
   * @param userDTO instance of {@linkplain org.retal.logiweb.dto.UserDTO}
   * @param userInfoDTO instance of {@linkplain org.retal.logiweb.dto.UserInfoDTO}
   * @param cityDTO instance of {@linkplain org.retal.logiweb.dto.CityDTO}
   * @return transient instance of {@linkplain org.retal.logiweb.domain.entity.User User} driver
   *         entity
   */
  private User mapUserRelatedDTOsToDriverEntity(UserDTO userDTO, UserInfoDTO userInfoDTO,
      CityDTO cityDTO) {
    User user = new User(userDTO);
    UserInfo userInfo = new UserInfo(userInfoDTO);
    City city = new City(cityDTO);
    user.setRole(UserRole.DRIVER.toString().toLowerCase());
    userInfo.setCity(city);
    user.setUserInfo(userInfo);
    return user;
  }

  private List<User> getDriversForGivenPage(Integer page) {
    return userService.getPartUsers(USERS_PER_PAGE * (page - 1), USERS_PER_PAGE);
  }

  private Integer getMaxPossiblePage() {
    return (int) Math.ceil(1.0 * userService.getRowsAmount() / USERS_PER_PAGE);
  }
}
