package org.retal.config.spring_security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		log.info("auth success");
		Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
		log.info(Arrays.toString(roles.toArray()));
		if (roles.contains("ADMIN")) {
			response.sendRedirect(request.getContextPath() + "/adminPage");
		}
		if (roles.contains("MANAGER")) {
			response.sendRedirect(request.getContextPath() + "/managerPage");
		}
		if (roles.contains("DRIVER")) {
			response.sendRedirect(request.getContextPath() + "/driverPage");
		}
	}

	private static final Logger log = Logger.getLogger(AuthSuccessHandler.class);
}
