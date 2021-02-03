package org.retal.logiweb.config.spring.security;

import org.retal.logiweb.controller.GlobalExceptionHandler;
import org.retal.logiweb.domain.enums.UserRole;
import org.retal.logiweb.service.logic.UserAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Spring Security configuration class.
 * 
 * @author Alexander Retivov
 *
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@ComponentScan(basePackages = "org.retal.logiweb", excludeFilters = @Filter(type = FilterType.REGEX,
    pattern = "org.retal.logiweb.config.spring.app.jms.*"))
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final UserAuthorizationService authService;

  private final AuthenticationSuccessHandler authSuccessHandler;

  private final Error403Handler authEntryPointAndAccessDeniedHandler;

  /**
   * This method sets up password encoding for user authorization. Currently used password encodes
   * is {@linkplain org.retal.logiweb.config.spring.security.SHA512PasswordEncoder
   * SHA512PasswordEncoder}
   */
  @Autowired
  public void registerGlobalAuthentication(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(authService).passwordEncoder(new SHA512PasswordEncoder());

  }

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public SecurityConfig(UserAuthorizationService authService,
      AuthenticationSuccessHandler authSuccessHandler,
      Error403Handler authEntryPointAndAccessDeniedHandler) {
    this.authService = authService;
    this.authSuccessHandler = authSuccessHandler;
    this.authEntryPointAndAccessDeniedHandler = authEntryPointAndAccessDeniedHandler;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().ignoringAntMatchers("/ws/**").and().authorizeRequests()
        .antMatchers("/static/**", "/home", "/", "/403", "/404", "/spring_auth", "/ws/**",
            GlobalExceptionHandler.ERROR_PAGE)
        .permitAll().antMatchers("static/my_js_library.js").authenticated()
        .antMatchers("/adminPage", "/addNewUser", "/deleteUser/*")
        .hasAuthority(UserRole.ADMIN.toString())
        .antMatchers("/managerPage", "/deleteDriver/*", "/editUser", "/addNewCar", "/deleteCar/*",
            "/editCar", "/cargoAndOrders", "/getCityAndCargoInfo", "/getCarsForOrder/*",
            "/changeCarForOrder/*")
        .hasAnyAuthority(UserRole.MANAGER.toString(), UserRole.ADMIN.toString())
        .antMatchers("/driverPage", "/changeStatus/*", "/changeLocation/*", "/updateCargo/*")
        .hasAuthority(UserRole.DRIVER.toString()).antMatchers("/log_out")
        .hasAuthority(UserRole.DRIVER.toString()).anyRequest().authenticated().and()
        .exceptionHandling().authenticationEntryPoint(authEntryPointAndAccessDeniedHandler)
        .accessDeniedHandler(authEntryPointAndAccessDeniedHandler);
    http.formLogin().loginPage("/home").loginProcessingUrl("/spring_auth")
        .successHandler(authSuccessHandler).failureUrl("/spring_auth?error")
        .usernameParameter("j_login").passwordParameter("j_password").permitAll();
    http.logout().permitAll().logoutUrl("/logout").logoutSuccessUrl("/spring_auth?logout")
        .invalidateHttpSession(true).deleteCookies("JSESSIONID");
  }
}
