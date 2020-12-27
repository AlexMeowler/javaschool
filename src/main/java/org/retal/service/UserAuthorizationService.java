package org.retal.service;

import java.util.*;

import org.apache.log4j.Logger;
import org.retal.dao.UserDAO;
import org.retal.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserAuthorizationService implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) {
		User user = userDAO.find(username);
		if (user == null) {
			log.info("User " + username + " not found");
			throw new UsernameNotFoundException("User not found");
		}
		log.info("User " + username + " found");
		Set<GrantedAuthority> roles = new HashSet<>();
		roles.add(new SimpleGrantedAuthority(user.getRole().toUpperCase()));
		return new org.springframework.security.core.userdetails.User(user.getLogin(),
				user.getPassword(), roles);
	}

	@Autowired
	private UserDAO userDAO;

	private static final Logger log = Logger.getLogger(UserAuthorizationService.class);
}
