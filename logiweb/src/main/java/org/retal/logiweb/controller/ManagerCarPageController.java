package org.retal.logiweb.controller;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.retal.logiweb.domain.entity.Car;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.SessionInfo;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.entity.UserInfo;
import org.retal.logiweb.dto.CarDTO;
import org.retal.logiweb.dto.CityDTO;
import org.retal.logiweb.service.logic.CarService;
import org.retal.logiweb.service.logic.CityService;
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
public class ManagerCarPageController {

  private final SessionInfo sessionInfo;

  private final CarService carService;

  private final CityService cityService;

  public static final String MANAGER_CARS_PAGE = "/managerCarsPage";

  private static final String CITY_LIST_ATTRIBUTE_NAME = "cityList";

  private static final int CARS_PER_PAGE = 10;

  private static final Logger log = Logger.getLogger(ManagerCarPageController.class);

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public ManagerCarPageController(SessionInfo sessionInfo, CarService carService,
      CityService cityService) {
    this.sessionInfo = sessionInfo;
    this.carService = carService;
    this.cityService = cityService;
  }

  /**
   * Method responsible for showing manager page.
   */
  @GetMapping(value = MANAGER_CARS_PAGE)
  public RedirectView getManagerCarsPage(Model model, RedirectAttributes redir) {
    model.asMap().forEach(redir::addFlashAttribute);
    return new RedirectView(MANAGER_CARS_PAGE + "/1", true);
  }

  @GetMapping(value = MANAGER_CARS_PAGE + "/{page}")
  public String getPartUsers(Model model, @PathVariable Integer page) {
    int maxPage = getMaxPossiblePage();
    if (page > maxPage) {
      return "redirect:" + MANAGER_CARS_PAGE + "/" + maxPage;
    }
    if (page < 1) {
      return "redirect:" + MANAGER_CARS_PAGE + "/1";
    }
    BindingResult carResult =
        (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "car");
    Map<String, String> errors = UserValidator.convertErrorsToHashMap(carResult);
    for (Map.Entry<String, String> e : errors.entrySet()) {
      log.debug(e.getKey() + ":" + e.getValue());
    }
    model.addAllAttributes(errors);
    User user = sessionInfo.getCurrentUser();
    UserInfo userInfo = user.getUserInfo();
    model.addAttribute("current_user_name", userInfo.getName() + " " + userInfo.getSurname());
    List<Car> cars = getCarsForGivenPage(page);
    model.addAttribute("carsList", cars);
    List<City> cities = cityService.getAllCities();
    model.addAttribute(CITY_LIST_ATTRIBUTE_NAME, cities);
    return "managerCars";
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
    return new RedirectView(MANAGER_CARS_PAGE, true);
  }

  /**
   * Method responsible for attempt to delete car from database when button is clicked using
   * {@linkplain org.retal.logiweb.service.logic.CarService service layer}.
   * 
   * @see org.retal.logiweb.domain.entity.Car
   */
  @GetMapping(value = "/deleteCar/{id}")
  public RedirectView deleteCar(@PathVariable String id, RedirectAttributes redir) {
    RedirectView redirectView = new RedirectView(MANAGER_CARS_PAGE, true);
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
    Car car = carService.getCar(id);
    if (car == null) {
      throw new NullPointerException("Car not found");
    }
    redir.addFlashAttribute("car", car);
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
   * {@linkplain org.retal.logiweb.service.logic.CarService service layer} which will update car if
   * input is valid.
   * 
   * @see org.retal.logiweb.domain.entity.Car
   */
  @PostMapping(value = "/submitEditedCar")
  public RedirectView finishCarEditing(CarDTO carDTO, BindingResult bindingResult,
      RedirectAttributes redir, @RequestParam(name = "capacity") String capacity,
      @RequestParam(name = "shift") String shiftLength, CityDTO cityDTO) {
    RedirectView redirectView = new RedirectView(MANAGER_CARS_PAGE, true);
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
   * Method for mapping car related DTOs (car, city) to
   * {@linkplain org.retal.logiweb.domain.entity.Car Car} entity with user role set to 'driver'.
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

  private List<Car> getCarsForGivenPage(Integer page) {
    List<Car> cars = carService.getAllCars();
    return cars.subList(CARS_PER_PAGE * (page - 1), Math.min(cars.size(), CARS_PER_PAGE * page));
  }

  private Integer getMaxPossiblePage() {
    return (int) Math.ceil(1.0 * carService.getAllCars().size() / CARS_PER_PAGE);
  }
}
