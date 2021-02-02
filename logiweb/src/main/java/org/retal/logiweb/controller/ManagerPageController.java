package org.retal.logiweb.controller;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.SessionInfo;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.domain.enums.UserRole;
import org.retal.logiweb.dto.CarDTO;
import org.retal.logiweb.dto.CityDTO;
import org.retal.logiweb.dto.UserDTO;
import org.retal.logiweb.dto.UserInfoDTO;
import org.retal.logiweb.service.logic.CarService;
import org.retal.logiweb.service.logic.CityService;
import org.retal.logiweb.service.logic.UserService;
import org.retal.logiweb.service.validators.UserValidator;
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

@Controller
public class ManagerPageController {

  private final SessionInfo sessionInfo;

  private final UserService userService;

  private final CarService carService;

  private final CityService cityService;

  public static final String MANAGER_PAGE = "/managerPage";

  private static final String CITY_LIST_ATTRIBUTE_NAME = "cityList";

  private static final Logger log = Logger.getLogger(ManagerPageController.class);

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public ManagerPageController(SessionInfo sessionInfo, UserService userService,
      CarService carService, CityService cityService) {
    this.sessionInfo = sessionInfo;
    this.userService = userService;
    this.carService = carService;
    this.cityService = cityService;
  }

  /**
   * Method responsible for showing manager page.
   */
  @GetMapping(value = MANAGER_PAGE)
  public String getManagerPage(Model model) {
    BindingResult userResult =
        (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "user");
    Map<String, String> errors = UserValidator.convertErrorsToHashMap(userResult);
    for (Map.Entry<String, String> e : errors.entrySet()) {
      log.debug(e.getKey() + ":" + e.getValue());
    }
    model.addAllAttributes(errors);
    BindingResult carResult =
        (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "car");
    errors = UserValidator.convertErrorsToHashMap(carResult);
    for (Map.Entry<String, String> e : errors.entrySet()) {
      log.debug(e.getKey() + ":" + e.getValue());
    }
    model.addAllAttributes(errors);
    User user = sessionInfo.getCurrentUser();
    UserInfo userInfo = user.getUserInfo();
    model.addAttribute("current_user_name", userInfo.getName() + " " + userInfo.getSurname());
    List<User> users = userService.getAllDrivers();
    model.addAttribute("driverList", users);
    List<Car> cars = carService.getAllCars();
    model.addAttribute("carsList", cars);
    List<City> cities = cityService.getAllCities();
    model.addAttribute(CITY_LIST_ATTRIBUTE_NAME, cities);
    return "managerPage";
  }

  /**
   * Method responsible for adding new drivers to database using
   * {@linkplain org.retal.logiweb.service.logic.UserService service layer}.
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
    return new RedirectView(MANAGER_PAGE, true);
  }

  /**
   * Method responsible for attempt to delete driver from database when button is clicked using
   * {@linkplain org.retal.logiweb.service.logic.UserService service layer}.
   * 
   * @see org.retal.logiweb.domain.entity.User
   */
  @GetMapping(value = "/deleteDriver/{id}")
  public RedirectView deleteDriver(@PathVariable Integer id, RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView(MANAGER_PAGE, true);
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
    RedirectView redirectView = new RedirectView("/editDriver", true);
    User we = sessionInfo.getCurrentUser();
    User target = userService.getUser(id);
    if (userService.userHasRightsToEditOrDeleteUser(we, target)) {
      redir.addFlashAttribute("user", target);
      redir.addFlashAttribute("we", we);
    } else {
      redirectView.setUrl("/403/" + we.getLogin());
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
   * {@linkplain org.retal.logiweb.service.logic.UserService service layer} which will update entity if
   * input is valid.
   * 
   * @see org.retal.logiweb.domain.entity.User
   */
  @PostMapping(value = "/submitEditedDriver")
  public RedirectView finishDriverEditing(UserDTO userDTO, BindingResult bindingResult,
      UserInfoDTO userInfoDTO, CityDTO cityDTO, RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView(MANAGER_PAGE, true);
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
   * Method responsible for adding new cars to database using
   * {@linkplain org.retal.logiweb.service.logic.CarService service layer}.
   * 
   * @see org.retal.logiweb.domain.entity.Car
   */
  @PostMapping(value = "/addNewCar")
  public RedirectView addNewCar(CarDTO carDTO, BindingResult bindingResult, CityDTO cityDTO,
      RedirectAttributes redir, @RequestParam(name = "capacity") String capacity,
      @RequestParam(name = "shift") String shiftLength) {
    redir.addFlashAttribute("visiblecar", "true");
    Car car = mapCarRelatedDTOsToCarEntity(carDTO, cityDTO);
    carService.addNewCar(car, bindingResult, capacity, shiftLength);
    if (bindingResult.hasErrors()) {
      log.warn("There were validation errors at adding new car");
      redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "car", bindingResult);
      redir.addFlashAttribute("car", car);
    }
    return new RedirectView(MANAGER_PAGE, true);
  }

  /**
   * Method responsible for attempt to delete car from database when button is clicked using
   * {@linkplain org.retal.logiweb.service.logic.CarService service layer}.
   * 
   * @see org.retal.logiweb.domain.entity.Car
   */
  @GetMapping(value = "/deleteCar/{id}")
  public RedirectView deleteCar(@PathVariable String id, RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView(MANAGER_PAGE, true);
    Car car = carService.getCar(id);
    if (!carService.deleteCar(car)) {
      redir.addFlashAttribute("error_carDeletionFailed",
          "Could not delete car due to assigned order or being driven by someone");
    }
    return redirectView;
  }

  /**
   * Method responsible for redirecting to car editor page.
   * 
   * @see org.retal.logiweb.domain.entity.Car
   */
  @GetMapping(value = "/editCar/{id}")
  public RedirectView editCar(@PathVariable String id, RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView("/editCar", true);
    redir.addFlashAttribute("car", carService.getCar(id));
    return redirectView;
  }

  /**
   * Method responsible for showing car editor page.
   * 
   * @see org.retal.logiweb.domain.entity.Car
   */
  @GetMapping(value = "/editCar")
  public String carEditForm(Model model) {
    BindingResult result =
        (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "car");
    Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
    model.addAllAttributes(errors);
    List<City> cities = cityService.getAllCities();
    model.addAttribute(CITY_LIST_ATTRIBUTE_NAME, cities);
    return "editCar";
  }

  /**
   * Method responsible for submitting edited car to
   * {@linkplain org.retal.logiweb.service.logic.CarService service layer} which will update car if input
   * is valid.
   * 
   * @see org.retal.logiweb.domain.entity.Car
   */
  @PostMapping(value = "/submitEditedCar")
  public RedirectView finishCarEditing(CarDTO carDTO, BindingResult bindingResult,
      RedirectAttributes redir, @RequestParam(name = "capacity") String capacity,
      @RequestParam(name = "shift") String shiftLength, CityDTO cityDTO) {
    RedirectView redirectView = new RedirectView(MANAGER_PAGE, true);
    Car car = mapCarRelatedDTOsToCarEntity(carDTO, cityDTO);
    carService.updateCar(car, bindingResult, capacity, shiftLength);
    if (bindingResult.hasErrors()) {
      log.warn("There were validation errors at editing car");
      redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "car", bindingResult);
      redir.addFlashAttribute("car", car);
      redirectView.setUrl("/editCar");
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
   * @return transient instance of {@linkplain org.retal.logiweb.domain.entity.User User} driver entity
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

  /**
   * Method for mapping car related DTOs (car, city) to {@linkplain org.retal.logiweb.domain.entity.Car
   * Car} entity with user role set to 'driver'.
   * 
   * @param carDTO instance of {@linkplain org.retal.logiweb.dto.CarDTO}
   * @param cityDTO instance of {@linkplain org.retal.logiweb.dto.CityDTO}
   * @return transient instance of {@linkplain org.retal.logiweb.domain.entity.Car Car} entity
   */
  private Car mapCarRelatedDTOsToCarEntity(CarDTO carDTO, CityDTO cityDTO) {
    Car car = new Car(carDTO);
    City city = new City(cityDTO);
    car.setLocation(city);
    return car;
  }
}
