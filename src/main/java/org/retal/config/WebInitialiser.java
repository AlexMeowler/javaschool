package org.retal.config;

import org.retal.config.spring.RootConfig;
import org.retal.config.spring.WebConfig;
import org.retal.config.spring_security.SecurityConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebInitialiser extends AbstractAnnotationConfigDispatcherServletInitializer
{

	@Override
	protected Class<?>[] getRootConfigClasses() 
	{
		return new Class[] {RootConfig.class, SecurityConfig.class};
	}

	@Override
	protected Class<?>[] getServletConfigClasses() 
	{	
		return new Class[] {WebConfig.class};
	}

	@Override
	protected String[] getServletMappings() 
	{	
		return new String[] {"/"};
	}
	
}
