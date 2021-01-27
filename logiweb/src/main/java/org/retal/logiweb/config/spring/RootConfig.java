package org.retal.logiweb.config.spring;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Root configuration class.
 * Only resource handler is configured.
 * @author Alexander Retivov
 */

public class RootConfig implements WebMvcConfigurer {
  
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/pages/**").addResourceLocations("/pages/");
    registry.addResourceHandler("/static/**").addResourceLocations("/js/", "/css/", "/img/");
  }
}
