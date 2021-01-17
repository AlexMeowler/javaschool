package org.retal.service;

import java.util.Set;
import javax.validation.ConstraintViolation;
import org.apache.log4j.Logger;
import org.retal.domain.Cargo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Custom {@linkplain org.retal.domain.Cargo Cargo} validator.
 * @author Alexander Retivov
 *
 */
@Service
public class CargoValidator implements Validator {
  
  private final javax.validation.Validator validator;

  private static final Logger log = Logger.getLogger(CargoValidator.class);
  
  @Autowired
  public CargoValidator(javax.validation.Validator validator) {
    this.validator = validator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return Cargo.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    // logging
    log.info("Validating cargo");
    Cargo cargo = (Cargo) target;
    Set<ConstraintViolation<Object>> validates = validator.validate(cargo);
    for (ConstraintViolation<Object> violation : validates) {
      String propertyPath = violation.getPropertyPath().toString();
      String message = violation.getMessage();
      throwError(errors, propertyPath, message);
    }
    Integer weight = cargo.getMass();
    if (weight != null && weight < 0) {
      throwError(errors, "mass", "Cargo weight length must non-negative integer.");
    }
  }

  private void throwError(Errors errors, String property, String message) {
    log.debug(property + " : " + message);
    errors.reject(property, message);
  }
}
