package org.retal.domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 
 * Entity for orders
 *
 */
@Entity
@Table(name="orders")
public class Order {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="isCompleted")
	private Boolean isCompleted;
	
	@OneToOne
	@JoinColumn(name="car_id", referencedColumnName="registration_id")
	private Car car;
	
	@OneToMany(mappedBy="order")
	private Set<RoutePoint> points;
	
	@Column(name="route")
	private String route;
	
	@Transient
	private Set<Cargo> cargo;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Boolean getIsCompleted() {
		return isCompleted;
	}

	public void setIsCompleted(Boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}
	
	public Set<RoutePoint> getPoints() {
		return points;
	}
	
	public void setPoints(Set<RoutePoint> points) {
		this.points = points;
	}
	
	public String getRoute() {
		return route;
	}
	
	public void setRoute(String route) {
		this.route = route;
	}
	
	public Set<Cargo> getCargo() {
		return cargo;
	}
	
	public void setCargo(Set<Cargo> cargo) {
		this.cargo = cargo;
	}
	
	//TODO toString
}
