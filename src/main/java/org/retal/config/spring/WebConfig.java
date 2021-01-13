package org.retal.config.spring;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.retal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@PostConstruct
	public void init() {
		Thread monthChangeChecker = new Thread() {
			public void run() {
				while(!isInterrupted()) {
					Calendar now = new GregorianCalendar();
					if(now.get(Calendar.MONTH) != calendar.get(Calendar.MONTH)) {
						log.debug("Month changed");
						userService.setUsersWorkedHoursToZero();
					}
					calendar = now;
					try {
						sleep(SLEEP_TIME_MINUTES * 60 * 1000);
					} catch(InterruptedException e) {
						log.info("Trying to stop month checker");
						interrupt();
					}
				}
			}
			
			@Autowired
			private UserService userService;
			
			private Calendar calendar = new GregorianCalendar();
			
			private static final long SLEEP_TIME_MINUTES = 1;
		};
		log.info("Starting month checker");
		monthChangeChecker.setDaemon(true);
		monthChangeChecker.start();
	}
	
	private static final Logger log = Logger.getLogger(WebConfig.class);
}
