package org.retal.logiweb.service.logic.interfaces;

import org.retal.logiweb.domain.entity.User;
import org.springframework.validation.BindingResult;

/**
 * Interface, containing list of required business-logic methods regarding
 * {@linkplain org.retal.logiweb.domain.entity.User User} and
 * {@linkplain org.retal.logiweb.domain.entity.UserInfo UserInfo} entities.
 * 
 * @author Alexander Retivov
 *
 */
public interface UserServices {

  /**
   * Validates and adds new user to database.
   * 
   * @param user {@linkplain org.retal.logiweb.domain.entity.User User} to be added
   * @param bindingResult object to store validation result
   * @param password not hashed password to be validated
   */
  public void addNewUser(User user, BindingResult bindingResult, String password);

  /**
   * Deletes user from database.
   * 
   * @param target user to be deleted
   * @return redirect address (for controllers) if method invoker has no rights to delete
   *         <b>target</b> user
   */
  public String deleteUser(User target);

  /**
   * Updates user in database.
   * 
   * @param updatedUser user to be updates
   * @param bindingResult object to store validation result
   * @param password not hashed password to be validated
   * @return redirect address (for controllers) if method invoker has no rights to edit
   *         <b>updatedUser</b> user
   */
  public String updateUser(User updatedUser, BindingResult bindingResult, String password);

  /**
   * Checks if user A has rights to edit or delete user B.
   * 
   * @param caller user A
   * @param target user B
   * @return true is user A has right to edit or delete user B, false otherwise
   */
  public boolean userHasRightsToEditOrDeleteUser(User caller, User target);

  /**
   * Sets the amount of hours at work during current month at 0 for all users. This method is only
   * called when month change occurred.
   */
  public void setUsersWorkedHoursToZero();
}
