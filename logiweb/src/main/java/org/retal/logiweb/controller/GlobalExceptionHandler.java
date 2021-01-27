package org.retal.logiweb.controller;

import org.apache.log4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Exception handler to redirect users to user-friendly error page.
 * 
 * @author Alexander Retivov
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  public static final String ERROR_PAGE = "errorPage";

  private static final Logger log = Logger.getLogger(GlobalExceptionHandler.class);

  /**
   * Handler for all types of exceptions. This method logs given exception and redirects to error
   * page.
   * 
   * @param e exception being thrown somewhere
   * @param redir redirect attributes object to pass HTTP error code
   * @return view for error page
   */
  @ExceptionHandler(value = Exception.class)
  public RedirectView customErrorPage(Exception e, RedirectAttributes redir) {
    log.error(e, e);
    ResponseStatus status = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
    Integer code = status != null ? status.code().value() : 500;
    code = status == null && e instanceof ResponseStatusException
        ? ((ResponseStatusException) e).getRawStatusCode()
        : code;
    RedirectView redirectView = new RedirectView(ERROR_PAGE, true);
    redir.addFlashAttribute("errorCode", "Error " + code);
    return redirectView;
  }
}
