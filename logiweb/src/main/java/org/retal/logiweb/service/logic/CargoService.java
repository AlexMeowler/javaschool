package org.retal.logiweb.service.logic;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.retal.logiweb.dao.CargoDAO;
import org.retal.logiweb.domain.entity.Cargo;
import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.domain.entity.RoutePoint;
import org.retal.logiweb.domain.entity.SessionInfo;
import org.retal.logiweb.domain.entity.User;
import org.retal.logiweb.domain.enums.CargoStatus;
import org.retal.logiweb.service.validators.CargoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

/**
 * Service, containing business-logic methods regarding
 * {@linkplain org.retal.logiweb.domain.entity.Cargo Cargo} and
 * {@linkplain org.retal.logiweb.domain.entity.Order Order} with
 * {@linkplain org.retal.logiweb.domain.entity.RoutePoint RoutePoint} entities.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class CargoService {

  private final CargoDAO cargoDAO;

  private final Validator cargoValidator;

  private final SessionInfo sessionInfo;

  private OrderService orderService;

  private static final Logger log = Logger.getLogger(CargoService.class);

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public CargoService(CargoDAO cargoDAO, CargoValidator cargoValidator, SessionInfo sessionInfo) {
    this.cargoDAO = cargoDAO;
    this.cargoValidator = cargoValidator;
    this.sessionInfo = sessionInfo;
  }

  public List<Cargo> getAllCargo() {
    return cargoDAO.readAll();
  }

  public Cargo getCargo(Integer id) {
    return cargoDAO.read(id);
  }
  
  @Autowired
  public void setOrderService(OrderService orderService) {
    this.orderService = orderService;
  }

  /**
   * Validates and adds new cargo row to database if validation was successful.
   * 
   * @param cargo cargo to be added
   * @param bindingResult object for storing validation result
   * @param weight input from submitted form to be parsed to integer
   */
  public void addNewCargo(Cargo cargo, BindingResult bindingResult, String weight) {
    try {
      Integer weightInt = Integer.parseInt(weight);
      cargo.setMass(weightInt);
    } catch (NumberFormatException e) {
      bindingResult.reject("mass", "Cargo weight length must be non-negative integer");
    }
    cargoValidator.validate(cargo, bindingResult);
    if (!bindingResult.hasErrors()) {
      cargoDAO.add(cargo);
    }
  }

  /**
   * Updates cargo status following chain "prepared -> loaded -> unloaded". Additionally checks if
   * order is completed after changing cargo's status.
   * 
   * @see CargoService#checkOrderForCompletion(Order)
   * @param id cargo id to be updated
   * @param bindingResult object for storing validation result
   * @return true if order is completed, false otherwise
   */
  public boolean updateCargoAndCheckOrderCompletion(Integer id, BindingResult bindingResult) {
    Cargo cargo = getCargo(id);
    User user = sessionInfo.getCurrentUser();
    Order order = orderService.getOrder(user.getUserInfo().getOrder().getId());
    if (cargo == null) {
      bindingResult.reject("globalCargo", "Cargo not found");
      log.warn("Cargo not found");
    } else {
      if (!order.getCargo().contains(cargo)) {
        bindingResult.reject("globalCargo", "Attempt to change unassigned to your current"
            + " order cargo. Please don't try to cahnge page code");
        log.warn("Attempt to access unassigned cargo");
      }

    }
    if (!bindingResult.hasErrors()) {
      CargoStatus status = CargoStatus.valueOf(cargo.getStatus().toUpperCase());
      switch (status) {
        case PREPARED:
          status = CargoStatus.LOADED;
          checkCityMatch(cargo, true, bindingResult);
          break;
        case LOADED:
          checkCityMatch(cargo, false, bindingResult);
          status = CargoStatus.UNLOADED;
          break;
        case UNLOADED:
        default:
          break;
      }
      if (!bindingResult.hasErrors()) {
        String newStatus = status.toString().toLowerCase();
        log.info("Cargo id=" + cargo.getId() + ": changed status from " + cargo.getStatus() + " to "
            + newStatus);
        cargo.setStatus(newStatus);
        cargoDAO.update(cargo);
        return orderService.checkOrderForCompletion(order);
      }
    }
    return false;
  }

  /**
   * Checks if city where the driver is matches the city to load/unload cargo.
   * 
   * @param cargo {@linkplain org.retal.logiweb.domain.entity.Cargo Cargo} to be loaded/unloaded
   * @param isLoading true if we are looking for match between city of load and current location,
   *        false if we are looking for match between city of unload and current location
   * @param bindingResult object to store validation errors
   */
  private void checkCityMatch(Cargo cargo, boolean isLoading, BindingResult bindingResult) {
    User driver = sessionInfo.getCurrentUser();
    RoutePoint point = cargo.getPoints().stream().filter(c -> c.getIsLoading().equals(isLoading))
        .collect(Collectors.toList()).get(0);
    if (!point.getCity().equals(driver.getUserInfo().getCity())) {
      String action = isLoading ? "load" : "unload";
      bindingResult.reject("cityNotMatching", "You must be in " + point.getCity().getCurrentCity()
          + " to " + action + " cargo \"" + cargo.getName() + "\"");
    }
  }
}
