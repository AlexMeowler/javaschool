package org.retal.logiweb.config.spring.web;

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

}
