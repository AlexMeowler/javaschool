package org.retal.logiweb.domain.entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import org.apache.log4j.Logger;
import org.retal.logiweb.dto.UserDTO;

@Entity
@Table(name = "users")
public class User {

  private static final Logger log = Logger.getLogger(User.class);

  public User() {

  }

  /**
   * Constructor for mapping user DTO object to user entity.
   * 
   * @param userDTO an instance of {@linkplain org.retal.logiweb.dto.UserDTO UserDTO}
   */
  public User(UserDTO userDTO) {
    setId(userDTO.getId());
    setLogin(userDTO.getLogin());
    setPassword(userDTO.getPassword());
    setRole(userDTO.getRole());
    setUserInfo(userDTO.getUserInfo());
  }

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "login")
  @Size(min = 5, max = 25, message = "Login must have at least 5 and at most 25 characters.")
  private String login;

  @Column(name = "password")
  private String password;

  @Column(name = "role")
  private String role;

  @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @MapsId
  @JoinColumn(name = "id")
  private UserInfo userInfo;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  /**
   * Password setter which automatically converts it to SHA-512. This is done to avoid storing
   * "naked" passwords in entities.
   * 
   * @param password password to be hashed and set
   */
  public void setPassword(String password) {
    if (password != null) {
      String hashedPassword = getPasswordAsBase64Hash(password);
      this.password = hashedPassword;
    }
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public UserInfo getUserInfo() {
    return userInfo;
  }

  public void setUserInfo(UserInfo userInfo) {
    this.userInfo = userInfo;
  }

  public String toString() {
    return "User [id = " + id + ", login = " + login + ", role = " + role + "]";
  }

  private String getPasswordAsBase64Hash(String pass) {
    try {
      MessageDigest mg = MessageDigest.getInstance("sha-512");
      byte[] passwordBytes = pass.getBytes(StandardCharsets.UTF_8);
      return Base64.getEncoder().encodeToString(mg.digest(passwordBytes));
    } catch (Exception e) {
      log.info("NoSuchAlgorithmException, returning null. How did this even happen?");
      return null;
    }
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof User) {
      return ((User) o).id == this.id;
    } else {
      return false;
    }
  }
}
