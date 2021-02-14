package org.retal.logiweb.service.logic.interfaces;

import java.util.List;
import org.retal.logiweb.domain.entity.Car;
import org.springframework.validation.BindingResult;

/**
 * Interface, containing list of required business-logic methods regarding
 * {@linkplain org.retal.logiweb.domain.entity.Car Car} entities.
 * 
 * @author Alexander Retivov
 *
 */
public interface CarServices {

  /**
   * Validates and adds new car to database.
   * 
   * @param car {@linkplain org.retal.logiweb.domain.entity.Car Car} to be added
   * @param bindingResult object to store validation result
   * @param capacity car capacity (user input) to be parsed
   * @param shiftlength shift length (user input) to be parsed
   */
  public void addNewCar(Car car, BindingResult bindingResult, String capacity, String shiftlength);
  
  /**
   * Deletes car from database. Can not delete car if it's assigned to order.
   * 
   * @param car car {@linkplain org.retal.logiweb.domain.entity.Car Car} to be deleted
   * @return true if deletion was successful, false otherwise
   */
  public boolean deleteCar(Car car);
  
  /**
   * Updates car in database.
   * 
   * @param car car {@linkplain org.retal.logiweb.domain.entity.Car Car} to be updated
   */
  public void updateCar(Car car, BindingResult bindingResult, String capacity, String shiftlength);
  
  /**
   * Gets all available cars for given order ID.
   * 
   * @param id {@linkplain org.retal.logiweb.domain.entity.Order Order} related ID
   * @return list of all cars which can be re-assigned to that order or null if order is completed
   */
  public List<Car> getAllAvailableCarsForOrderId(Integer id);
}
