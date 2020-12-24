package org.retal.config.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "org.retal")
public class WebConfig 
{
	@Bean
	public ViewResolver viewResolver()
	{
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/pages/");
		resolver.setSuffix(".jsp");
		return resolver;
	}
	
	@Bean
	public javax.validation.Validator localValidatorFactoryBean() 
	{
	   return new LocalValidatorFactoryBean();
	}
}
