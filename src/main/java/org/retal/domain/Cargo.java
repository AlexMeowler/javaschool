package org.retal.domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.retal.dto.CargoDTO;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="cargo")
public class Cargo {
	
	public Cargo() {
		
	}
	
	public Cargo(CargoDTO cargoDTO) {
		setId(cargoDTO.getId());
		setName(cargoDTO.getName());
		setMass(cargoDTO.getMass());
		setStatus(cargoDTO.getStatus());
		setPoint(cargoDTO.getPoints());
		setDescription(cargoDTO.getDescription());
	}
	
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
	
	@OneToMany(mappedBy="cargo")
	@JsonIgnore
	private Set<RoutePoint> points;
	
	@Column(name="description")
	private String description;
	
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
	
	public Set<RoutePoint> getPoints() {
		return points;
	}
	
	public void setPoint(Set<RoutePoint> points) {
		this.points = points;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Cargo) {
			return this.id == ((Cargo)o).getId();
		} else {
			return false;
		}
	}
	
	@Override 
	public int hashCode() {
		return id;
	}
	
	//TODO toString
}
