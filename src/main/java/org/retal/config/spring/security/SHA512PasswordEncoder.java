package org.retal.config.spring.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Custom implementation of {@linkplain org.springframework.security.crypto.password.PasswordEncoder
 * PasswordEncoder}. Password is encoded using SHA512 algorithm,
 * {@linkplain java.security.MessageDigest standard java realization}.
 * 
 * @author Alexander Retivov
 */
@Configuration
public class SHA512PasswordEncoder implements PasswordEncoder {
  
  private static final Logger log = Logger.getLogger(SHA512PasswordEncoder.class);

  @Override
  public String encode(CharSequence rawPassword) {
    try {
      MessageDigest md = MessageDigest.getInstance("sha-512");
      byte[] password = rawPassword.toString().getBytes(StandardCharsets.UTF_8);
      password = md.digest(password);
      return Base64.getEncoder().encodeToString(password);
    } catch (NoSuchAlgorithmException e) {
      log.info("NoSuchAlgorithmException, returning null. How did this even happen?");
      return null;
    }
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    String rawHash = encode(rawPassword);
    return rawHash != null && rawHash.equals(encodedPassword);
  }
}
