package org.retal.logiweb.service.logic.interfaces;

import org.springframework.validation.BindingResult;

/**
 * Interface, containing list of required business-logic methods regarding
 * {@linkplain org.retal.logiweb.domain.entity.User User} entities with role 'driver'.
 * 
 * @author Alexander Retivov
 *
 */
public interface DriverServices {

  /**
   * Updates driver status. For successful status change, status value should be equal to String
   * representation for any value in {@linkplain org.retal.logiweb.domain.enums.DriverStatus
   * DriverStatus} enumeration.
   * 
   * @param newStatus new status
   * @param bindingResult object to store validation result
   */
  public void changeStatus(String newStatus, BindingResult bindingResult);
  
  /**
   * Changes location of driver.
   * 
   * @param bindingResult object for storing validating result
   */
  public void changeLocation(BindingResult bindingResult);
}
