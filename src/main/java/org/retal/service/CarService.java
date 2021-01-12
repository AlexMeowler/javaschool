package org.retal.service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.retal.dao.CarDAO;
import org.retal.dao.OrderDAO;
import org.retal.domain.Car;
import org.retal.domain.City;
import org.retal.domain.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

@Service
public class CarService {

	public List<Car> getAllCars() {
		return carDAO.readAll();
	}

	public Car getCar(String primaryKey) {
		return carDAO.read(primaryKey);
	}

	public void addNewCar(Car car, BindingResult bindingResult, String capacity, String shiftlength) {
		doInitialDataValidation(car, bindingResult, capacity, shiftlength);
		Car correlationDB = carDAO.findCar(car.getRegistrationId());
		if (correlationDB != null) {
			bindingResult.reject("uniqueCarId", "Car ID must be unique");
		}
		if(!bindingResult.hasErrors()) {
			carDAO.add(car);
		}
	}

	public void deleteCar(Car car) {
		carDAO.delete(car);
	}
	
	public void updateCar(Car car, BindingResult bindingResult, String capacity, 
							String shiftlength) {
		log.info("Attempt to update car ID = " + car.getRegistrationId());
		doInitialDataValidation(car, bindingResult, capacity, shiftlength);
		if(!bindingResult.hasErrors()) {
			carDAO.update(car);
		}
	}
	
	private void doInitialDataValidation(Car car, BindingResult bindingResult, String capacity, String shiftlength) {
		try {
			Integer shiftLength = Integer.parseInt(shiftlength);
			car.setShiftLength(shiftLength);
		} catch (NumberFormatException e) {
			bindingResult.reject("shiftLength", "Shift length must be positive integer");
		}
		try {
			Float capacityTons = Float.parseFloat(capacity);
			car.setCapacityTons(capacityTons);
		} catch (NumberFormatException e) {
			bindingResult.reject("capacityTons", "Capacity must be positive decimal");
		}
		carValidator.validate(car, bindingResult);
	}

	public void generateCarForEachCity() {
		List<City> cities = cityService.getAllCities();
		Random rand = new Random();
		for(City c : cities) {
			Car car = new Car();
			car.setIsWorking(true);
			car.setLocation(c);
			car.setCapacityTons((float)(1 + rand.nextInt(41) * 1.0 / 10));
			String registrationLetters = c.getCurrentCity().substring(0, 2).toUpperCase();
			String registrationNumber = "" + (10000 + rand.nextInt(90000));
			car.setRegistrationId(registrationLetters + registrationNumber);
			car.setShiftLength(12 + rand.nextInt(13));
			carDAO.add(car);
		}
	}
	
	public List<Car> getAllAvailableCarsForOrderId(Integer id) {
		Order order = orderDAO.read(id);
		List<Car> availableCars = carDAO.readAll().stream()
				.filter(c -> c.getIsWorking())
				.filter(c -> c.getOrder() == null)
				.filter(c -> c.getCapacityTons() >= order.getRequiredCapacity())
				.filter(c -> c.getLocation().equals(order.getCar().getLocation()))
				.filter(c -> c.getShiftLength() >= order.getRequiredShiftLength())
				.collect(Collectors.toList());
		log.debug(availableCars.size() + " cars are fit for order ID=" + order.getId());
		return !order.getIsCompleted() ? availableCars : null;
	}

	@Autowired
	private CarDAO carDAO;
	
	@Autowired
	private OrderDAO orderDAO;
	
	@Autowired
	private CityService cityService;
	
	@Autowired
	@Qualifier("carValidator")
	private Validator carValidator;
	
	private static final Logger log = Logger.getLogger(CarService.class);
}
