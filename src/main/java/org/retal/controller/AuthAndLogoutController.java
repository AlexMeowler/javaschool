package org.retal.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller class for logging in and out.
 * 
 * @author Alexander Retivov
 *
 */
@Controller
public class AuthAndLogoutController {

  private static final Logger log = Logger.getLogger(AuthAndLogoutController.class);

  /**
   * Method responsible for calling Spring Security authentication procedures. Redirects to home
   * page with flags for both invalid credentials and loggin out successfully.
   */
  @GetMapping(value = "/spring_auth")
  public RedirectView logInAuthGet(@RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "logout", required = false) String logout, RedirectAttributes redir) {
    if (error != null) {
      log.info("auth fail");
      redir.addFlashAttribute("message", "Invalid login or password");
    }
    if (logout != null) {
      log.info("logout success");
      redir.addFlashAttribute("message", "Logged out successfully");
    }
    RedirectView redirectView = new RedirectView("/home", true);
    return redirectView;
  }

  /**
   * Method responsible for logging out using Spring Security.
   */
  @PostMapping(value = "/logout")
  public void logOut() {
    log.info("logout attempt");
  }
}
