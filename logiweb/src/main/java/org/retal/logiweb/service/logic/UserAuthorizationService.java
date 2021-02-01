package org.retal.logiweb.service.logic;

import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.retal.logiweb.dao.UserDAO;
import org.retal.logiweb.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service, responsible for user authorization. Implementation of
 * {@linkplain org.springframework.security.core.userdetails.UserDetailsService 
 * UserDetailsService}.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class UserAuthorizationService implements UserDetailsService {
  
  private final UserDAO userDAO;

  private static final Logger log = Logger.getLogger(UserAuthorizationService.class);

  @Autowired
  public UserAuthorizationService(UserDAO userDAO) {
    this.userDAO = userDAO;
  }
  
  @Override
  public UserDetails loadUserByUsername(String username) {
    User user = userDAO.findUser(username);
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
}
