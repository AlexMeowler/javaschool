package org.retal.config.spring.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Custom error 403 (access denied) handler for both authorized and unauthorized users.
 * @author Alexander Retivov
 *
 */
@Configuration
public class Error403Handler implements AuthenticationEntryPoint, AccessDeniedHandler {

  private static final Logger log = Logger.getLogger(Error403Handler.class);
  
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    log.warn("Access denied for unauthorized user: " + request.getRequestURI());
    response.sendRedirect(request.getContextPath() + "/403");
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      log.warn("User: " + auth.getName() + " attempted to access the protected URL: "
          + request.getRequestURI());
    }
    String param = auth != null ? "/" + auth.getName() : "";
    response.sendRedirect(request.getContextPath() + "/403" + param);
  }
}
