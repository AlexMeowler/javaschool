package org.retal.domain;

import org.retal.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.ScopedProxyMode;

@Component
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionInfo {
	public User getCurrentUser() {
		if (user == null) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			user = userDAO.findUser(auth.getName());
		}
		return user;
	}

	private User user;

	@Autowired
	private UserDAO userDAO;
}
