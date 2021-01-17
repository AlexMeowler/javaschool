package org.retal.config.spring.security;

import org.retal.domain.enums.UserRole;
import org.retal.service.UserAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
@ComponentScan(basePackages = "org.retal")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final UserAuthorizationService authService;

  private final AuthenticationSuccessHandler authSuccessHandler;

  private final Error403Handler authEntryPointAndAccessDeniedHandler;

  /**
   * This method sets up password encoding for user authorization. Currently used password encodes
   * is {@linkplain org.retal.config.spring.security.SHA512PasswordEncoder SHA512PasswordEncoder}
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
    http.authorizeRequests().antMatchers("/static/**", "/home", "/", "/403").permitAll()
        .antMatchers("/spring_auth").anonymous().antMatchers("static/my_js_library.js")
        .authenticated().antMatchers("/adminPage", "/addNewUser", "/deleteUser/*")
        .hasAuthority(UserRole.ADMIN.toString())
        .antMatchers("/managerPage", "/deleteDriver/*", "/editUser", "/addNewCar", "/deleteCar/*",
            "/editCar", "/cargoAndOrders", "/getCityAndCargoInfo", "/getCarsForOrder/*",
            "/changeCarForOrder/*")
        .hasAnyAuthority(UserRole.MANAGER.toString(), UserRole.ADMIN.toString())
        .antMatchers("/driverPage", "/changeStatus/*", "/changeLocation/*", "/updateCargo/*")
        .hasAnyAuthority(UserRole.DRIVER.toString(), UserRole.ADMIN.toString()).anyRequest()
        .authenticated().and().exceptionHandling()
        .authenticationEntryPoint(authEntryPointAndAccessDeniedHandler)
        .accessDeniedHandler(authEntryPointAndAccessDeniedHandler);
    http.formLogin().loginPage("/home").loginProcessingUrl("/spring_auth")
        .successHandler(authSuccessHandler).failureUrl("/spring_auth?error")
        .usernameParameter("j_login").passwordParameter("j_password").permitAll();
    http.logout().permitAll().logoutUrl("/logout").logoutSuccessUrl("/spring_auth?logout")
        .invalidateHttpSession(true).deleteCookies("JSESSIONID");
  }
}
