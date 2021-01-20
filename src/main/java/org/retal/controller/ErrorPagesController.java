package org.retal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for redirecting to custom error pages.
 * 
 * @author Alexander Retivov
 * @see org.retal.config.spring.security.Error403Handler
 */
@Controller
public class ErrorPagesController {

  /**
   * Method responsible for showing default version of custom 403 (access denied) error page.
   */
  @GetMapping("/403")
  public String accessDeniedUnnamed() {
    return "error403";
  }

  /**
   * Method responsible for showing default version of custom 403 (access denied) error page with
   * name attribute.
   */
  @GetMapping("/403/{name}")
  public RedirectView accessDeniedNamed(@PathVariable String name, RedirectAttributes redir) {
    redir.addFlashAttribute("username", name);
    RedirectView redirectView = new RedirectView("/403", true);
    return redirectView;
  }

  /**
   * Method responsible for showing default version of custom 404 (not found) error page.
   */
  @GetMapping("/404")
  public String notFound() {
    return "error404";
  }

  /**
   * Method responsible for showing default version of custom error page.
   */
  @GetMapping(GlobalExceptionHandler.ERROR_PAGE)
  public String showErrorPage() {
    return GlobalExceptionHandler.ERROR_PAGE;
  }

  /**
   * Method for checking redirection to error page.
   */
  @GetMapping(value = "/exception")
  public void checkExceptionPage() {
    throw new NullPointerException();
  }

  /**
   * Method for checking redirection to error page with 502 error code. This method throws
   * ResponseStatusException.
   */
  @GetMapping(value = "/exception502")
  public void checkExceptionPageWithCustomCode() {
    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
  }

  /**
   * Method for checking redirection to error page with 502 error code. This method throws
   * ResponseStatusException.
   */
  @GetMapping(value = "/exception503")
  public void checkExceptionPageWithAnnotatedException() {
    throw new Annotated503ErrorCodeException();
  }

  @SuppressWarnings("serial")
  @ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE)
  private static class Annotated503ErrorCodeException extends RuntimeException {
    public Annotated503ErrorCodeException() {
      super();
    }
  }
}
