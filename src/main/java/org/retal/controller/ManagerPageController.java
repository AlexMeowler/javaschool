package org.retal.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.retal.domain.Car;
import org.retal.domain.City;
import org.retal.domain.SessionInfo;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.domain.UserRole;
import org.retal.dto.CarDTO;
import org.retal.dto.CityDTO;
import org.retal.dto.UserDTO;
import org.retal.dto.UserInfoDTO;
import org.retal.service.CarService;
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

@Controller
public class ManagerPageController {
	
	@GetMapping(value = MANAGER_PAGE)
	public String getManagerPage(Model model) {
		BindingResult userResult = (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "user");
		Map<String, String> errors = UserValidator.convertErrorsToHashMap(userResult);
		model.addAllAttributes(errors);
		BindingResult carResult = (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "car");
		errors = UserValidator.convertErrorsToHashMap(carResult);
		model.addAllAttributes(errors);
		User user = sessionInfo.getCurrentUser();
		UserInfo userInfo = user.getUserInfo();
		model.addAttribute("current_user_name", userInfo.getName() + " " + userInfo.getSurname());
		List<User> users = userService.getAllDrivers();
		model.addAttribute("driverList", users);
		List<Car> cars = carService.getAllCars();
		model.addAttribute("carsList", cars);
		List<City> cities = cityService.getAllCities();
		model.addAttribute("cityList", cities);
		return "managerPage";
	}

	@PostMapping(value = "/addNewDriver")
	public RedirectView addNewDriver(UserDTO userDTO, UserInfoDTO userInfoDTO, 
			CityDTO cityDTO, RedirectAttributes redir,BindingResult bindingResult) {
		log.info("Attempt to add new driver");
		RedirectView redirectView = new RedirectView(MANAGER_PAGE, true);
		redir.addFlashAttribute("visibledriver", "true");
		User user = new User(userDTO);
		UserInfo userInfo = new UserInfo(userInfoDTO);
		City city = new City(cityDTO);
		user.setRole(UserRole.DRIVER.toString().toLowerCase());
		userInfo.setCurrentCity(city);
		user.setUserInfo(userInfo);
		userService.addNewUser(user, bindingResult, userDTO.getPassword());
		if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
			redir.addFlashAttribute("user", user);
		}
		return redirectView;
	}

	@GetMapping(value = "/deleteDriver/{id}")
	public RedirectView deleteDriver(@PathVariable Integer id, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView(MANAGER_PAGE, true);
		User user = userService.getUser(id);
		String url403 = userService.deleteUser(user);
		if (!url403.isEmpty()) {
			String param = sessionInfo.getCurrentUser().getLogin();
			redirectView.setUrl(url403 + "/" + param);
		}
		return redirectView;
	}

	@GetMapping(value = "/editDriver/{id}")
	public RedirectView editDriver(@PathVariable Integer id, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView("/editDriver", true);
		redir.addFlashAttribute("user", userService.getUser(id));
		redir.addFlashAttribute("we", sessionInfo.getCurrentUser());
		return redirectView;
	}

	@GetMapping(value = "/editDriver")
	public String driverEditForm(Model model) {
		BindingResult result = (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "user");
		Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
		model.addAllAttributes(errors);
		model.addAttribute("editUser", "/submitEditedDriver");
		List<City> cities = cityService.getAllCities();
		model.addAttribute("cityList", cities);
		return "editUser";
	}

	@PostMapping(value = "/submitEditedDriver")
	public RedirectView finishDriverEditing(UserDTO userDTO, BindingResult bindingResult, 
			UserInfoDTO userInfoDTO, CityDTO cityDTO, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView(MANAGER_PAGE, true);
		User user = new User(userDTO);
		UserInfo userInfo = new UserInfo(userInfoDTO);
		City city = new City(cityDTO);
		user.setRole(UserRole.DRIVER.toString().toLowerCase());
		userInfo.setCurrentCity(city);
		user.setUserInfo(userInfo);
		userService.updateUser(user, bindingResult, userDTO.getPassword());
		if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "user", bindingResult);
			redir.addFlashAttribute("user", user);
			redirectView.setUrl("/editDriver");
		}
		return redirectView;
	}
	
	@PostMapping(value = "/addNewCar")
	public RedirectView addNewCar(CarDTO carDTO, BindingResult bindingResult, CityDTO cityDTO, 
			RedirectAttributes redir, @RequestParam(name = "capacity") String capacity,
			@RequestParam(name = "shift") String shiftLength) {
		RedirectView redirectView = new RedirectView(MANAGER_PAGE, true);
		redir.addFlashAttribute("visiblecar", "true");
		Car car = new Car(carDTO);
		City city = new City(cityDTO);
		car.setLocation(city);
		carService.addNewCar(car, bindingResult, capacity, shiftLength);
		if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "car", bindingResult);
			redir.addFlashAttribute("car", car);
		}
		return redirectView;
	}
	
	@GetMapping(value = "/deleteCar/{id}")
	public RedirectView deleteCar(@PathVariable String id, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView(MANAGER_PAGE, true);
		Car car = carService.getCar(id);
		carService.deleteCar(car);
		return redirectView;
	}
	
	@GetMapping(value = "/editCar/{id}")
	public RedirectView editCar(@PathVariable String id, RedirectAttributes redir) {
		RedirectView redirectView = new RedirectView("/editCar", true);
		redir.addFlashAttribute("car", carService.getCar(id));
		
		return redirectView;
	}

	@GetMapping(value = "/editCar")
	public String carEditForm(Model model) {
		BindingResult result = (BindingResult) model.asMap().get(BindingResult.MODEL_KEY_PREFIX + "car");
		Map<String, String> errors = UserValidator.convertErrorsToHashMap(result);
		model.addAllAttributes(errors);
		List<City> cities = cityService.getAllCities();
		model.addAttribute("cityList", cities);
		return "editCar";
	}
	
	@PostMapping(value = "/submitEditedCar")
	public RedirectView finishCarEditing(CarDTO carDTO, BindingResult bindingResult,
				RedirectAttributes redir, @RequestParam(name = "capacity") String capacity,
				@RequestParam(name = "shift") String shiftLength, CityDTO cityDTO) {
		RedirectView redirectView = new RedirectView(MANAGER_PAGE, true);
		Car car = new Car(carDTO);
		City city = new City(cityDTO);
		car.setLocation(city);
		carService.updateCar(car, bindingResult, capacity, shiftLength);
		if (bindingResult.hasErrors()) {
			redir.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX + "car", bindingResult);
			redir.addFlashAttribute("car", car);
			redirectView.setUrl("/editCar");
		}
		return redirectView;
	}

	@Autowired
	private SessionInfo sessionInfo;

	@Autowired
	private UserService userService;

	@Autowired
	private CarService carService;
	
	@Autowired 
	private CityService cityService;
	
	private static final String MANAGER_PAGE = "/managerPage";

	private static final Logger log = Logger.getLogger(ManagerPageController.class);
}
