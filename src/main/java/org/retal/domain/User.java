package org.retal.domain;

import javax.persistence.*;


@Entity
@Table(name = "users")
public class User 
{
	@Id
	//@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "login")
	private String login;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "role")
	private String role;
	
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
	
	public void setLogin(String name)
	{
		this.login = name;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public String getRole()
	{
		return role;
	}
	
	public void setRole(String role)
	{
		this.role = role;
	}
	
	public String toString()
	{
		return "User [id = " + id + ", login = " + login + ", role = " + role + "]";
	}
}
