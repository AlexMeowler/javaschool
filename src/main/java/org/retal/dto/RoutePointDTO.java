package org.retal.dto;

import org.retal.domain.RoutePoint;

public class RoutePointDTO extends RoutePoint {
	
	private Integer cargoId;
	private String cityName;
	private String error;
	
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
	
	public String getError() {
		return error;
	}
	
	public void setError(String error) {
		this.error = error;
	}
}
