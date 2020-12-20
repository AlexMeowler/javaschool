package org.retal.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.persistence.*;

import org.retal.dao.UserDAO;


@Entity
@Table(name = "users")
public class User 
{
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "login")
	private String login;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "role")
	private String role;
	
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
	private UserInfo userInfo;
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public String getLogin()
	{
		return login;
	}
	
	public void setLogin(String login)
	{
		this.login = login;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setPassword(String password)
	{
		String hashedPassword = getPasswordAsBase64Hash(password);
		this.password = hashedPassword;
	}
	
	public String getRole()
	{
		return role;
	}
	
	public void setRole(String role)
	{
		this.role = role;
	}
	
	public UserInfo getUserInfo()
	{
		return userInfo;
	}
	
	public void setUserInfo(UserInfo userInfo)
	{
		this.userInfo = userInfo;
	}
	
	public String toString()
	{
		return "User [id = " + id + ", login = " + login + ", role = " + role + "]";
	}
	
	private String getPasswordAsBase64Hash(String pass)
	{
		MessageDigest mg = null;
		try
		{
			mg = MessageDigest.getInstance("sha-512");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		byte[] password = pass.getBytes(StandardCharsets.UTF_8);
		String hashedPassword = Base64.getEncoder().encodeToString(mg.digest(password));
		return hashedPassword;
	}
}
