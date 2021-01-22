package org.retal.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.validation.ConstraintViolation;
import org.apache.log4j.Logger;
import org.retal.dao.CityDAO;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.retal.domain.enums.DriverStatus;
import org.retal.dto.UserWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

@Service
public class UserValidator implements Validator {

  private static final Logger log = Logger.getLogger(UserValidator.class);

  private javax.validation.Validator validator;

  private CityDAO cityDAO;

  public static final String MALICIOUS_REGEX =
      "([\\W&&[^ -]]+)|( create)|( drop)|( table)|( add)|( database)|( select)|( where)|( join)"
          + "|( or)|( and)|( alter)|( update)|(create )|(drop )|(table )|(add )|(database )"
          + "|(select )|(where )|(join )|(or )|(and )|(alter )|(update )|( create )|( drop )"
          + "|( table )|( add )|( database )|( select )|( where )|( join )|( or )|( and )"
          + "|( alter )|( update )";

  @Autowired
  public UserValidator(javax.validation.Validator validator, CityDAO cityDAO) {
    this.validator = validator;
    this.cityDAO = cityDAO;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return UserWrapper.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    log.info("Validating user");
    UserWrapper wrapper = (UserWrapper) target;
    User user = wrapper.getUser();
    Set<ConstraintViolation<Object>> validates = validator.validate(user);
    for (ConstraintViolation<Object> violation : validates) {
      String propertyPath = violation.getPropertyPath().toString();
      String message = violation.getMessage();
      throwError(errors, propertyPath, message);
    }
    log.info("Validating user info");
    UserInfo userInfo = user.getUserInfo();
    validates = validator.validate(userInfo);
    for (ConstraintViolation<Object> violation : validates) {
      String propertyPath = violation.getPropertyPath().toString();
      String message = violation.getMessage();
      throwError(errors, propertyPath, message);
    }
    Pattern digits = Pattern.compile("\\d+");
    if (digits.matcher(userInfo.getName()).find()) {
      throwError(errors, "noDigitsName", "Name must not contain digits.");
    }
    if (digits.matcher(userInfo.getSurname()).find()) {
      throwError(errors, "noDigitsSurname", "Surname must not contain digits.");
    }
    log.info("Checking for malicious input");
    String response = checkForMaliciousInput(user.getLogin());
    if (!response.isEmpty()) {
      user.setLogin("");
    }
    String maliciousInputMessage = response;
    response = checkForMaliciousInput(userInfo.getName());
    if (!response.isEmpty()) {
      userInfo.setName("");
    }
    maliciousInputMessage = setIfEmpty(maliciousInputMessage, response);
    response = checkForMaliciousInput(userInfo.getSurname());
    if (!response.isEmpty()) {
      userInfo.setSurname("");
    }
    maliciousInputMessage = setIfEmpty(maliciousInputMessage, response);
    String password = wrapper.getPassword();
    response = checkForMaliciousInput(password);
    if (!response.isEmpty()) {
      userInfo.setSurname("");
    }
    maliciousInputMessage = setIfEmpty(maliciousInputMessage, response);
    if (password.length() < 6 && !password.isEmpty()) {
      String property = "realPassword";
      String message = "Password must have at least 6 characters.";
      errors.reject(property, message);
      throwError(errors, "realPassword", "Password must have at least 6 characters.");
    }
    DriverStatus[] statuses = DriverStatus.values();
    boolean statusValid = false;
    for (DriverStatus status : statuses) {
      statusValid |= status.toString().replace("_", " ").equalsIgnoreCase(userInfo.getStatus());
    }
    if (!statusValid) {
      throwError(errors, "status", "Invalid status. Please don't try to change page code");
    }
    if (cityDAO.read(userInfo.getCity().getCurrentCity()) == null) {
      throwError(errors, "currentCity", "Invalid value. Please don't try to change page code");
    }
    if (!maliciousInputMessage.isEmpty()) {
      throwError(errors, "userMaliciousInput", maliciousInputMessage);
    }
  }

  /**
   * Checks given string for malicious input (SQL injections, HTML injections, &lt;script&gt;
   * injections.
   * 
   * @param input input string to be validated
   * @return empty string if validation successful, string with message error if forbidden
   *         characters detected
   */
  public static String checkForMaliciousInput(String input) {
    Pattern maliciousMatcher = Pattern.compile(MALICIOUS_REGEX);
    String message = "";
    if (maliciousMatcher.matcher(input.toLowerCase()).find()) {
      message = "No special characters and SQL syntax words allowed";
    }
    return message;
  }

  private String setIfEmpty(String givenString, String givenResult) {
    if (givenString.isEmpty()) {
      return givenResult;
    } else {
      return givenString;
    }
  }

  private void throwError(Errors errors, String property, String message) {
    log.debug(property + " : " + message);
    errors.reject(property, message);
  }

  /**
   * Converts errors from {@linkplain org.springframework.validation.BindingResult BindingResult} to
   * map. For every error code a special prefix "error_" is added. For example: "name:too short" is
   * converted to "error_name:too short". Used for view part of application, to show different
   * validation errors.
   * 
   * @param bindingResult object containing validation errors for conversion.
   * @return <code>Map&lt;String, String&gt;</code> of converted errors.
   */
  public static Map<String, String> convertErrorsToHashMap(BindingResult bindingResult) {
    Map<String, String> map = new HashMap<>();
    if (bindingResult != null) {
      for (ObjectError o : bindingResult.getAllErrors()) {
        map.put("error_" + o.getCode(), o.getDefaultMessage());
        log.debug(o.getCode() + ":" + o.getDefaultMessage());
      }
    }
    return map;
  }
}
