package org.retal.config.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


public class RootConfig implements WebMvcConfigurer
{
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) 
	{
        registry.addResourceHandler("/pages/**").addResourceLocations("/pages/");
        registry.addResourceHandler("/static/**").addResourceLocations("/js/", "/css/", "/img/");
    }
}
