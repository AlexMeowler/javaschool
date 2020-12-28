package org.retal.domain;

import javax.persistence.*;

import org.retal.dto.CarDTO;

@Entity
@Table(name = "cars")
public class Car {
	
	public Car() {
		
	}
	
	public Car(CarDTO carDTO) {
		setRegistrationId(carDTO.getRegistrationId());
		setShiftLength(carDTO.getShiftLength());
		setCapacityTons(carDTO.getCapacityTons());
		setIsWorking(carDTO.getIsWorking());
		setLocation(carDTO.getLocation());
		setOrder(carDTO.getOrder());
	}
	
	@Id
	@Column(name = "registration_id")
	private String registrationId;

	@Column(name = "shift_length")
	private Integer shiftLength;

	@Column(name = "capacity_tons")
	private Float capacityTons;

	@Column(name = "is_working")
	private Boolean isWorking;

	@ManyToOne
	@JoinColumn(name="location", nullable = false)
	private City location;
	
	@OneToOne(mappedBy="car")
	private Order order;

	public String getRegistrationId() {
		return registrationId;
	}

	public void setRegistrationId(String registrationid) {
		this.registrationId = registrationid;
	}

	public Integer getShiftLength() {
		return shiftLength;
	}

	public void setShiftLength(Integer length) {
		shiftLength = length;
	}

	public Float getCapacityTons() {
		return capacityTons;
	}

	public void setCapacityTons(Float capacity) {
		capacityTons = capacity;
	}

	public Boolean getIsWorking() {
		return isWorking;
	}

	public void setIsWorking(Boolean flag) {
		isWorking = flag;
	}

	public City getLocation() {
		return location;
	}

	public void setLocation(City location) {
		this.location = location;
	}
	
	public Order getOrder() {
		return order;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
}
