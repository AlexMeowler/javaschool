package org.retal.config.spring.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
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
    roleChecker.checkAndRedirect("ADMIN", "/adminPage");
    roleChecker.checkAndRedirect("MANAGER", "/managerPage");
    roleChecker.checkAndRedirect("DRIVER", "/driverPage");
    /*if (roles.contains("ADMIN")) {
      response.sendRedirect(request.getContextPath() + "/adminPage");
    }
    if (roles.contains("MANAGER")) {
      response.sendRedirect(request.getContextPath() + "/managerPage");
    }
    if (roles.contains("DRIVER")) {
      response.sendRedirect(request.getContextPath() + "/driverPage");
    }*/
  }
}
