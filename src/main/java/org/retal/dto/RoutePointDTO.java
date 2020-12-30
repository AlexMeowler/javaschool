package org.retal.dto;

import org.retal.domain.RoutePoint;

public class RoutePointDTO extends RoutePoint {
	
	public Integer cargoId;
	public String cityName;
	
	public Integer getCargoId() {
		return cargoId;
	}
	
	public void setCargoId(Integer cargoId) {
		this.cargoId = cargoId;
	}
	
	public String getCityName() {
		return cityName;
	}
	
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
}
