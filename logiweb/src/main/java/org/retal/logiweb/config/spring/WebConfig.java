package org.retal.logiweb.config.spring;

import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import org.retal.logiweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Web configuration class. Provides 
 * {@linkplain org.springframework.web.servlet.ViewResolver ViewResolver}
 * and {@linkplain org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
 * LocalValidatorFactoryBean} beans.
 * @author Alexander Retivov
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "org.retal")
public class WebConfig {
  
  private static final Logger log = Logger.getLogger(WebConfig.class);
  
  
  /**
   * Configures {@linkplain org.springframework.web.servlet.ViewResolver ViewResolver}
   * for .jsp pages.
   * @return configured instance of 
   * {@linkplain org.springframework.web.servlet.ViewResolver ViewResolver}
   */
  @Bean
  public ViewResolver viewResolver() {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setPrefix("/pages/");
    resolver.setSuffix(".jsp");
    return resolver;
  }

  /**
   * Provides {@linkplain org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
   * LocalValidatorFactoryBean} for validating user input using annotations.
   * @return instance of 
   * {@linkplain org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
   * LocalValidatorFactoryBean}
   */
  @Bean
  public javax.validation.Validator localValidatorFactoryBean() {
    return new LocalValidatorFactoryBean();
  }
  
  /**
   * A method which starts daemon thread to check month change.
   * Check is done every minute. If month does change, amount of hours at work
   * will be set to 0 for all users.
   * @see org.retal.logiweb.service.UserService#setUsersWorkedHoursToZero()
   */
  @PostConstruct
  public void init() {
    Thread monthChangeChecker = new Thread() {
      @Autowired
      private UserService userService;

      private Calendar calendar = new GregorianCalendar();

      private static final long SLEEP_TIME_MINUTES = 1;
      
      @Override
      public void run() {
        while (!isInterrupted()) {
          Calendar now = new GregorianCalendar();
          if (now.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
            log.debug("Month changed");
            userService.setUsersWorkedHoursToZero();
          }
          calendar = now;
          try {
            sleep(SLEEP_TIME_MINUTES * 60 * 1000);
          } catch (InterruptedException e) {
            log.info("Trying to stop month checker");
            interrupt();
          }
        }
      }
    };
    log.info("Starting month checker");
    monthChangeChecker.setDaemon(true);
    monthChangeChecker.start();
  }

}
