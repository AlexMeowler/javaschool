package org.retal.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="cargo")
public class Cargo {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="mass_kg")
	private Integer mass;
	
	@Column(name="status")
	private String status;
	
	@OneToOne(mappedBy="cargo")
	private RoutePoint point;
	
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
	
	public Integer getMass() {
		return mass;
	}
	
	public void setMass(Integer mass) {
		this.mass = mass;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public RoutePoint getPoint() {
		return point;
	}
	
	public void setPoint(RoutePoint point) {
		this.point = point;
	}
}
