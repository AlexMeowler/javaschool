package org.retal.controller;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.retal.domain.Cargo;
import org.retal.domain.City;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.domain.enums.CargoStatus;
import org.retal.dto.CargoDTO;
import org.retal.dto.CityDTO;
import org.retal.dto.UserDTO;
import org.retal.dto.UserInfoDTO;
import org.retal.service.CarService;
import org.retal.service.CargoAndOrdersService;
import org.retal.service.CityService;
import org.retal.service.UserService;
import org.retal.service.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for administrator page and all possible actions on that page.
 * 
 * @author Alexander Retivov
 *
 */
@Controller
public class AdminPageController {

  private final UserService userService;

  private final SessionInfo sessionInfo;

  private final CityService cityService;

  private final CarService carService;

  private final CargoAndOrdersService cargoAndOrdersService;

  public static final String ADMIN_PAGE = "/adminPage";

  private static final Logger log = Logger.getLogger(AdminPageController.class);

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public AdminPageController(UserService userService, SessionInfo sessionInfo,
      CityService cityService, CarService carService, CargoAndOrdersService cargoAndOrdersService) {
    this.userService = userService;
    this.sessionInfo = sessionInfo;
    this.cityService = cityService;
    this.carService = carService;
    this.cargoAndOrdersService = cargoAndOrdersService;
  }

  /**
   * Method responsible for showing the main administrator page.
   */
  @GetMapping(value = ADMIN_PAGE)
  public String getAdminPage(Model model) {
    BindingResult result =
        (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "user");
    Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
    for (Map.Entry<String, String> e : errors.entrySet()) {
      log.debug(e.getKey() + ":" + e.getValue());
    }
    model.addAllAttributes(errors);
    result = (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "cargo");
    errors = UserValidator.convertErrorsToHashMap(result);
    for (Map.Entry<String, String> e : errors.entrySet()) {
      log.debug(e.getKey() + ":" + e.getValue());
    }
    model.addAllAttributes(errors);
    List<User> users = userService.getAllUsers();
    model.addAttribute("userList", users);
    List<City> cities = cityService.getAllCities();
    model.addAttribute("cityList", cities);
    model.addAttribute("cargoList", cargoAndOrdersService.getAllCargo());
    return "adminPage";
  }

  /**
   * Method responsible for adding cities and distances between them to database from file. It was
   * used only once, but will remain here in case of additional cities/distances are required.
   * 
   * @see org.retal.domain.City
   * @see org.retal.domain.CityDistance
   */
  @PostMapping(value = "/addCityInfo")
  public RedirectView addCityInfo() {
    RedirectView redirectView = new RedirectView(ADMIN_PAGE, true);
    cityService.addCitiesFromFile();
    cityService.addDistancesFromFile();
    return redirectView;
  }

  /**
   * Method responsible for adding drivers to database from file. It was used only once, but will
   * remain here in case of additional drivers are required.
   */
  @PostMapping(value = "/addDriverInfo")
  public RedirectView addDriverInfo() {
    RedirectView redirectView = new RedirectView(ADMIN_PAGE, true);
    userService.addDriversFromFile();
    return redirectView;
  }

  /**
   * Method responsible for generating one car for each city. It was used only once, but will remain
   * here in case of additional cars are required.
   * 
   * @see org.retal.domain.Car
   */
  @PostMapping(value = "/addCarsInfo")
  public RedirectView addCarsInfo() {
    RedirectView redirectView = new RedirectView(ADMIN_PAGE, true);
    carService.generateCarForEachCity();
    return redirectView;
  }

  /**
   * Method responsible for adding new user to database from form on page using
   * {@linkplain org.retal.service.UserService service layer}.
   * 
   * @see org.retal.domain.User
   */
  @PostMapping(value = "/addNewUser")
  public RedirectView addNewUser(UserDTO userDTO, BindingResult bindingResult,
      UserInfoDTO userInfoDTO, CityDTO cityDTO, RedirectAttributes redir) {
    redir.addFlashAttribute("visible", "true");
    User user = mapUserRelatedDTOsToEntity(userDTO, userInfoDTO, cityDTO);
    
    userService.addNewUser(user, bindingResult, userDTO.getPassword());
    RedirectView redirectView = new RedirectView(ADMIN_PAGE, true);
    if (bindingResult.hasErrors()) {
      log.warn("Validation failed when adding new user");
      redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
      redir.addFlashAttribute("user", user);
    }
    return redirectView;
  }

  /**
   * Method responsible for attempt to delete user from database when button is clicked using
   * {@linkplain org.retal.service.UserService service layer}.
   * 
   * @see org.retal.domain.User
   */
  @GetMapping(value = "/deleteUser/{id}")
  public RedirectView delete(@PathVariable Integer id, RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView(ADMIN_PAGE, true);
    String answer = userService.deleteUser(id);
    if (answer.equals(UserService.DELETION_UPDATION_ERROR)) {
      redir.addFlashAttribute("error_userDeletionFailed",
          "Could not delete user due to assigned order or being driving car");
    }
    return redirectView;
  }

  /**
   * Method responsible for redirecting to user editor page.
   * 
   * @see org.retal.domain.User
   */
  @GetMapping(value = "/editUser/{id}")
  public RedirectView edit(@PathVariable Integer id, RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView("/editUser", true);
    redir.addFlashAttribute("user", userService.getUser(id));
    redir.addFlashAttribute("we", sessionInfo.getCurrentUser());
    return redirectView;
  }

  /**
   * Method responsible for showing user editor page.
   * 
   * @see org.retal.domain.User
   */
  @GetMapping(value = "/editUser")
  public String editForm(Model model) {
    BindingResult result =
        (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "user");
    Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
    model.addAllAttributes(errors);
    model.addAttribute("editUser", "/submitEditedUser");
    List<City> cities = cityService.getAllCities();
    model.addAttribute("cityList", cities);
    return "editUser";
  }

  /**
   * Method responsible for submitting edited user to {@linkplain org.retal.service.UserService
   * service layer} which will update user if input is valid.
   * 
   * @see org.retal.domain.User
   */
  @PostMapping(value = "/submitEditedUser")
  public RedirectView finishEditing(UserDTO userDTO, BindingResult bindingResult,
      UserInfoDTO userInfoDTO, CityDTO cityDTO, RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView(ADMIN_PAGE, true);
    User user = mapUserRelatedDTOsToEntity(userDTO, userInfoDTO, cityDTO);
    userService.updateUser(user, bindingResult, userDTO.getPassword());
    if (bindingResult.hasErrors()) {
      log.warn("Validation fail when editing user");
      redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
      redir.addFlashAttribute("user", user);
      redirectView.setUrl("/editUser");
    }
    return redirectView;
  }

  /**
   * Method responsible for adding cargo entities using
   * {@linkplain org.retal.service.CargoAndOrdersService service layer}.
   * 
   * @see org.retal.domain.Cargo
   */
  @PostMapping(value = "/addNewCargo")
  public RedirectView addNewCargo(CargoDTO cargoDTO, BindingResult bindingResult,
      RedirectAttributes redir, @RequestParam(name = "mass") String weight) {
    redir.addFlashAttribute("visiblecargo", "true");
    Cargo cargo = new Cargo(cargoDTO);
    cargo.setStatus(CargoStatus.PREPARED.toString().toLowerCase());
    cargoAndOrdersService.addNewCargo(cargo, bindingResult, weight);
    RedirectView redirectView = new RedirectView(ADMIN_PAGE, true);
    if (bindingResult.hasErrors()) {
      log.warn("Validation fail when adding new cargo");
      redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "cargo", bindingResult);
      redir.addFlashAttribute("cargo", cargo);
    }
    return redirectView;
  }

  /**
   * Method for mapping user related DTOs (user, user info, city) to
   * {@linkplain org.retal.domain.User User} entity.
   * 
   * @param userDTO instance of {@linkplain org.retal.dto.UserDTO}
   * @param userInfoDTO instance of {@linkplain org.retal.dto.UserInfoDTO}
   * @param cityDTO instance of {@linkplain org.retal.dto.CityDTO}
   * @return transient instance of {@linkplain org.retal.domain.User User} entity
   */
  private User mapUserRelatedDTOsToEntity(UserDTO userDTO, UserInfoDTO userInfoDTO,
      CityDTO cityDTO) {
    User user = new User(userDTO);
    UserInfo userInfo = new UserInfo(userInfoDTO);
    City currentCity = new City(cityDTO);
    userInfo.setCity(currentCity);
    user.setUserInfo(userInfo);
    return user;
  }
}
