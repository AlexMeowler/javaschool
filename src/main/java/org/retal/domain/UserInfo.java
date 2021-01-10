package org.retal.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.*;

import org.retal.dto.UserInfoDTO;

@Entity
@Table(name = "user_info")
public class UserInfo {
	
	public UserInfo() {
		
	}
	
	public UserInfo(UserInfoDTO userInfoDTO)
	{
		setId(userInfoDTO.getId());
		setName(userInfoDTO.getName());
		setSurname(userInfoDTO.getSurname());
		setHoursWorked(userInfoDTO.getHoursWorked());
		setStatus(userInfoDTO.getStatus());
		setCurrentCity(userInfoDTO.getCurrentCity());
	}
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "name")
	@NotEmpty(message = "Please enter name")
	private String name;

	@Column(name = "surname")
	@NotEmpty(message = "Please enter surname")
	private String surname;

	@Column(name = "hours_worked_month")
	private Integer hoursWorked = 0;

	@Column(name = "status")
	private String status;
	
	@Column(name = "hours_drived")
	private Integer hoursDrived;
	
	@OneToOne
	@JoinColumn(name = "car_id", referencedColumnName = "registration_id")
	private Car car;

	@ManyToOne
	@JoinColumn(name="current_city", nullable = false)
	private City currentCity;

	@OneToOne(mappedBy = "userInfo", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@PrimaryKeyJoinColumn
	private User user;
	
	@ManyToOne
	@JoinColumn(name="order_id")
	private Order order;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getHoursWorked() {
		return hoursWorked;
	}

	public void setHoursWorked(Integer hoursWorked) {
		this.hoursWorked = hoursWorked;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public Integer getHoursDrived() {
		return hoursDrived;
	}

	public void setHoursDrived(Integer hoursDrived) {
		this.hoursDrived = hoursDrived;
	}

	public City getCurrentCity() {
		return currentCity;
	}

	public void setCurrentCity(City currentCity) {
		this.currentCity = currentCity;
	}
	
	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}
	
	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public String toString() {
		String owner = user != null ? user.toString() : "null";
		return "UserInfo [owner = " + owner + ", name = " + name + ", surname = " + surname + "]";
	}
}
