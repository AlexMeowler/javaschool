package org.retal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(securedEnabled = true)
@ComponentScan(basePackages = "org.retal")
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
	
	@Autowired
	public void registerGlobalAuthentication(AuthenticationManagerBuilder auth) throws Exception
	{
		MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder("sha-512");
		encoder.setEncodeHashAsBase64(true);
		auth.userDetailsService(authService).passwordEncoder(encoder);
		
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http.authorizeRequests().antMatchers("/resources/*", "home.jsp").permitAll()
			.antMatchers("spring_auth").anonymous()
			.antMatchers("logged.jsp").authenticated()
			.antMatchers("adminPage.jsp").access("hasRole('ADMIN')")
			.antMatchers("managerPage.jsp").access("hasRole('MANAGER')")
			.antMatchers("logiweb/driverPage.jsp").access("hasRole('DRIVER')")
			.anyRequest().authenticated()
			.and();
		http.formLogin()
			.loginPage("/home")
			.loginProcessingUrl("/spring_auth")
			.successForwardUrl("/logged")
			.failureUrl("/spring_auth?error")
			.usernameParameter("j_login")
			.passwordParameter("j_password")
			.permitAll();
		http.logout()
			.permitAll()
			.logoutUrl("/logout") //URL trigger for log out
			.logoutSuccessUrl("/spring_auth?logout")
			.invalidateHttpSession(true);
		
	}
	
	@Autowired
	private UserDetailsService authService;
}
