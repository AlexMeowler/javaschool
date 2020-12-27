package org.retal.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.log4j.Logger;
import org.retal.dao.UserDAO;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

@Service
@Qualifier("userValidator")
public class UserValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return UserService.UserWrapper.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		log.info("Validating user");
		UserService.UserWrapper wrapper = (UserService.UserWrapper) target;
		User user = wrapper.getUser();
		String password = wrapper.getPassword();
		Set<ConstraintViolation<Object>> validates = validator.validate(user);
		for (ConstraintViolation<Object> violation : validates) {
			String propertyPath = violation.getPropertyPath().toString();
			String message = violation.getMessage();
			log.info(propertyPath + " : " + message);
			errors.reject(propertyPath, message);
		}
		log.info("Validating user info");
		UserInfo userInfo = user.getUserInfo();
		validates = validator.validate(userInfo);
		for (ConstraintViolation<Object> violation : validates) {
			String propertyPath = violation.getPropertyPath().toString();
			String message = violation.getMessage();
			log.info(propertyPath + " : " + message);
			errors.reject(propertyPath, message);
		}
		// add special characters checking
		if (password.length() < 6 && !password.isEmpty()) {
			String property = "realPassword";
			String message = "Password must have at least 6 characters";
			log.info(property + " : " + message);
			errors.reject(property, message);
		}
	}

	public static Map<String, String> convertErrorsToHashMap(BindingResult bindingResult) {
		Map<String, String> map = new HashMap<>();
		if (bindingResult != null) {
			for (ObjectError o : bindingResult.getAllErrors()) {
				map.put("error_" + o.getCode(), o.getDefaultMessage());
				log.info(o.getCode() + ":" + o.getDefaultMessage());
			}
		}
		return map;
	}

	@Autowired
	private static final Logger log = Logger.getLogger(UserValidator.class);

	@Autowired
	private javax.validation.Validator validator;
}
