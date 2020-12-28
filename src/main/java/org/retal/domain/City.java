package org.retal.domain;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.retal.dto.CityDTO;

@Entity
@Table(name = "map_country_cities")
public class City {
	
	public City() {
		
	}
	
	public City(CityDTO cityDTO) {
		setCurrentCity(cityDTO.getCurrentCity());
		setUserInfos(cityDTO.getUserInfos());
	}
	
	@Id
	@Column(name = "city")
	private String currentCity;
	
	@OneToMany(mappedBy="currentCity")
	private Set<UserInfo> userInfos;
	
	public String getCurrentCity() {
		return currentCity;
	}
	
	public void setCurrentCity(String currentCity) {
		this.currentCity = currentCity;
	}
	
	public Set<UserInfo> getUserInfos() {
		return userInfos;
	}
	
	public void setUserInfos(Set<UserInfo> userInfos) {
		this.userInfos = userInfos;
	}
}
