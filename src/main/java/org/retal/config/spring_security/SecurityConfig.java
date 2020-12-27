package org.retal.config.spring_security;

import org.retal.domain.UserRole;
import org.retal.service.UserAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@ComponentScan(basePackages = "org.retal")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	public void registerGlobalAuthentication(AuthenticationManagerBuilder auth) throws Exception {
		// need to rethink
		MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder("sha-512");
		encoder.setEncodeHashAsBase64(true);
		auth.userDetailsService(authService).passwordEncoder(encoder);

	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/static/*", "/home", "/", "/403").permitAll()
			.antMatchers("/spring_auth").anonymous()
			.antMatchers("/adminPage", "/addNewUser", "/deleteUser/*").hasAuthority(UserRole.ADMIN.toString())
			.antMatchers("/managerPage", "/deleteDriver/*", "/editUser", "/addNewCar", "/deleteCar/*", "/editCar").hasAnyAuthority(UserRole.MANAGER.toString(), UserRole.ADMIN.toString())
			.antMatchers("/driverPage").hasAnyAuthority(UserRole.DRIVER.toString(), UserRole.ADMIN.toString())
			.anyRequest().authenticated()
			.and().exceptionHandling().authenticationEntryPoint(authEntryPointAndAccessDeniedHandler).accessDeniedHandler(authEntryPointAndAccessDeniedHandler);
		http.formLogin().loginPage("/home").loginProcessingUrl("/spring_auth").successHandler(authSuccessHandler)
				.failureUrl("/spring_auth?error").usernameParameter("j_login").passwordParameter("j_password")
				.permitAll();
		http.logout().permitAll().logoutUrl("/logout") // URL trigger for log out
				.logoutSuccessUrl("/spring_auth?logout").invalidateHttpSession(true).deleteCookies("JSESSIONID");
	}

	@Autowired
	private UserAuthorizationService authService;

	@Autowired
	private AuthenticationSuccessHandler authSuccessHandler;

	@Autowired
	private Error403Handler authEntryPointAndAccessDeniedHandler;
}
