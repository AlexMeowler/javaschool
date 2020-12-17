package org.retal.service;

import java.util.*;

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
public class UserAuthorizationService implements UserDetailsService
{
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException 
	{
		User user = userDAO.find(username);
		if(user == null)
		{
			throw new UsernameNotFoundException("User not found");
		}
		Set<GrantedAuthority> roles = new HashSet<>();
		/*switch(user.getRole())
		{
			case "admin":
				break;
			case "driver":
				break;
			case "manager":
				break;
		}*/
		roles.add(new SimpleGrantedAuthority(user.getRole().toUpperCase()));
		UserDetails details = new org.springframework.security.core.userdetails.User(user.getLogin(), user.getPassword(), roles);
		return details;
	}
	
	@Autowired
	private UserDAO userDAO;
}
