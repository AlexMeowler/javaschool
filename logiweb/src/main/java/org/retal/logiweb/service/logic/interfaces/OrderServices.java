package org.retal.logiweb.service.logic.interfaces;

import org.retal.logiweb.domain.entity.Order;
import org.retal.logiweb.dto.RoutePointListWrapper;
import org.springframework.validation.BindingResult;

/**
 * Interface, containing list of required business-logic methods regarding
 * {@linkplain org.retal.logiweb.domain.entity.Cargo Cargo} and
 * {@linkplain org.retal.logiweb.domain.entity.Order Order} with
 * {@linkplain org.retal.logiweb.domain.entity.RoutePoint RoutePoint} entities.
 * 
 * @author Alexander Retivov
 *
 */
public interface OrderServices {

  /**
   * Checks if order has been started by drivers. Order is considered started if both statements
   * below are true:<br>
   * 1) it is not completed 2) car assigned has already moved from starting city or cargo has been
   * already loaded to that car
   * 
   * @param order order to check
   * @return true if order has been started, false otherwise
   */
  public boolean isOrderStarted(Order order);
  
  /**
   * Attempts to change order's assigned car to another.
   * 
   * @param data input for defining order and car to perform changes
   * @return null if car was changed successfully, error message otherwise
   */
  public String changeOrderCar(String data);
  
  /**
   * Checks order for completion.
   * 
   * @param order order to be checked
   * @return true if order is completed, false otherwise
   */
  public boolean checkOrderForCompletion(Order order);
  
  /**
   * Validates {@linkplain org.retal.logiweb.domain.entity.RoutePoint RoutePoint} input and if no
   * errors found, attempts to calculate optimal path and assign car and drivers according to that
   * path. If path, car and drivers are selected, then
   * {@linkplain org.retal.logiweb.domain.entity.Order Order} is created and saved to database.
   * 
   * @param wrapper {@linkplain org.retal.logiweb.dto.RoutePointListWrapper wrapper} for list of
   *        route points to be validated and used for order creation
   * @param bindingResult object for storing validation result
   */
  public void createOrderFromRoutePoints(RoutePointListWrapper wrapper,
      BindingResult bindingResult);
}
