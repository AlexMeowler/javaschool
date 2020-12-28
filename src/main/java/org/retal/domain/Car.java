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

	@Column(name = "location")
	private String location;

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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
