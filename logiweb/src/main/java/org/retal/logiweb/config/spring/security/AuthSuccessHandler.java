package org.retal.logiweb.config.spring.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.retal.logiweb.controller.AdminPageController;
import org.retal.logiweb.controller.DriverPageController;
import org.retal.logiweb.controller.ManagerUserPageController;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Custom implementation of 
 * {@linkplain org.springframework.security.web.authentication.AuthenticationSuccessHandler 
 * AuthenticationSuccessHandler} interface.
 * @author Alexander Retivov
 *
 */
@Configuration
public class AuthSuccessHandler implements AuthenticationSuccessHandler {
  
  private static final Logger log = Logger.getLogger(AuthSuccessHandler.class);
  
  /**
   * Functional interface for checking authenticated user role.
   * @param role - role for checking (if user has it).
   * @param redirectAddress - address for redirection in case of user having associated role.
   * @author Alexander Retivov
   *
   */
  @FunctionalInterface
  private interface RoleChecker {
    public void checkAndRedirect(String role, String redirectAddress) throws IOException;
  }
  
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    log.info("auth success");
    Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
    log.debug(Arrays.toString(roles.toArray()));
    RoleChecker roleChecker = (role, redirectAddress) -> {
      if (roles.contains(role)) {
        response.sendRedirect(request.getContextPath() + redirectAddress);
      }
    };
    roleChecker.checkAndRedirect("ADMIN", AdminPageController.ADMIN_PAGE);
    roleChecker.checkAndRedirect("MANAGER", ManagerUserPageController.MANAGER_USERS_PAGE);
    roleChecker.checkAndRedirect("DRIVER", DriverPageController.DRIVER_PAGE);
  }
}
