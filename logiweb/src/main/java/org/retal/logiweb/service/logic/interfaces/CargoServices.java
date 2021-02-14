package org.retal.logiweb.service.logic.interfaces;

import org.retal.logiweb.domain.entity.Cargo;
import org.retal.logiweb.service.logic.impl.CargoService;
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
public interface CargoServices {
  
  /**
   * Validates and adds new cargo row to database if validation was successful.
   * 
   * @param cargo cargo to be added
   * @param bindingResult object for storing validation result
   * @param weight input from submitted form to be parsed to integer
   */
  public void addNewCargo(Cargo cargo, BindingResult bindingResult, String weight);
  
  /**
   * Updates cargo status following chain "prepared -> loaded -> unloaded". Additionally checks if
   * order is completed after changing cargo's status.
   * 
   * @see CargoService#checkOrderForCompletion(Order)
   * @param id cargo id to be updated
   * @param bindingResult object for storing validation result
   * @return true if order is completed, false otherwise
   */
  public boolean updateCargoWithOrder(Integer id, BindingResult bindingResult);
}
