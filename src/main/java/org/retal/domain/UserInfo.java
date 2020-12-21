package org.retal.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "user_info")
public class UserInfo 
{
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "surname")
	private String surname;
	
	@Column(name = "hours_worked_month")
	private Integer hoursWorked;
	
	@Column(name = "status")
	private String status;
	
	//current city
	
	@OneToOne(mappedBy = "userInfo", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
	private User user;
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getSurname()
	{
		return surname;
	}
	
	public void setSurname(String surname)
	{
		this.surname = surname;
	}
	
	public User getUser()
	{
		return user;
	}
	
	public void setUser(User user)
	{
		this.user = user;
	}
	
	public Integer getHoursWorked()
	{
		return hoursWorked;
	}
	
	public void setHoursWorked(Integer hoursWorked)
	{
		this.hoursWorked = hoursWorked;
	}
	
	public String getStatus()
	{
		return status;
	}
	
	public void setStatus(String status)
	{
		this.status = status;
	}
	
	public String toString()
	{
		String owner = user != null ? user.toString() : "null";
		return "UserInfo [owner = " + owner + ", name = " + name + ", surname = " + surname + "]";
	}
}