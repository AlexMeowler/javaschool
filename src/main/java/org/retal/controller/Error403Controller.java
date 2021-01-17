package org.retal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for redirecting to 403 (access denied) custom error page.
 * 
 * @author Alexander Retivov
 * @see org.retal.config.spring.security.Error403Handler
 */
@Controller
public class Error403Controller {

  /**
   * Method responsible for showing default version of custom error page.
   */
  @GetMapping("/403")
  public String accessDeniedUnnamed() {
    return "error403";
  }
  
  /**
   * Method responsible for showing default version of custom error page.
   */
  @GetMapping("/403/{name}")
  public RedirectView accessDeniedNamed(@PathVariable String name, RedirectAttributes redir) {
    if (name != null) {
      redir.addFlashAttribute("username", name);
    }
    RedirectView redirectView = new RedirectView("/403", true);
    return redirectView;
  }
}
