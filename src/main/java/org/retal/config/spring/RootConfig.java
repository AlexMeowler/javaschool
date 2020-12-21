package org.retal.config.spring;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


public class RootConfig implements WebMvcConfigurer
{
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
		//css?
        registry.addResourceHandler("/pages/**").addResourceLocations("/pages/");
        registry.addResourceHandler("/static/**").addResourceLocations("/js/", "/css/");
    }
	
	
}
