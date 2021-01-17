package org.retal.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {

  private static final Logger log = Logger.getLogger(HomePageController.class);

  /**
   * Method for redirecting to "/home" page from default "/" address.
   * @see HomePageController#getHome(Model)
   */
  @GetMapping(value = "/")
  public String getHomeFromBlankPath(Model model) {
    return "redirect:/home";
  }

  /**
   * Method responsible for showing the home page of web application.
   */
  @GetMapping("/home")
  public String getHome(Model model) {
    log.info("Redirected to home page");
    return "home";
  }
}
