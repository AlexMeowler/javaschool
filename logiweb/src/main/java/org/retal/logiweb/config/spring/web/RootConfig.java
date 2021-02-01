package org.retal.logiweb.config.spring.web;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.log4j.Logger;
import org.retal.logiweb.config.spring.app.logic.MonthChecker;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Root configuration class.
 * Only resource handler is configured.
 * @author Alexander Retivov
 */

public class RootConfig implements WebMvcConfigurer {
  
  private static final Logger log = Logger.getLogger(RootConfig.class);
  
  private Thread monthChecker = null;
  
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/pages/**").addResourceLocations("/pages/");
    registry.addResourceHandler("/static/**").addResourceLocations("/js/", "/css/", "/img/");
  }
  
  /**
   * A method which starts daemon thread to check month change.
   * Check is done every minute. If month does change, amount of hours at work
   * will be set to 0 for all users.
   * @see org.retal.logiweb.service.logic.UserService#setUsersWorkedHoursToZero()
   */
  @PostConstruct
  public void startMonthChecker() {
    log.info("Starting month checker");
    monthChecker = new MonthChecker();
    monthChecker.setDaemon(true);
    monthChecker.start();
  }
  
  @PreDestroy
  public void stopMonthChecker() {
    log.info("Stopping month checker");
    monthChecker.interrupt();
  }
}
