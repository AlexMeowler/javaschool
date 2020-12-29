package org.retal.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "map_country_distance")
@IdClass(CityDistance.CityDistancePK.class)
public class CityDistance {

	@Id
	@Column(name="city1")
	private String cityA;
	
	@Id
	@Column(name="city2")
	private String cityB;
	
	@Column(name="distance")
	private Integer distance;
	
	public String getCityA() {
		return cityA;
	}
	
	public void setCityA(String cityA) {
		this.cityA = cityA;
	}
	
	public String getCityB() {
		return cityB;
	}
	
	public void setCityB(String cityB) {
		this.cityB = cityB;
	}
	
	public Integer getDistance() {
		return distance;
	}
	
	public void setDistance(Integer distance) {
		this.distance = distance;
	}
	
	public static class CityDistancePK implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private String cityA;
		private String cityB;
		
		public CityDistancePK() {}
		
		public CityDistancePK(String cityA, String cityB) {
			this.cityA = cityA;
			this.cityB = cityB;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof CityDistancePK) {
				CityDistancePK obj = (CityDistancePK)o;
				boolean straightMatch = cityA.equals(obj.cityA) && cityB.equals(obj.cityB);
				boolean reverseMatch = cityA.equals(obj.cityB) && cityB.equals(obj.cityA);
				return obj == this || straightMatch || reverseMatch;
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			int hash = 0;
			for(int x : cityA.chars().toArray()) {
				hash += x;
			}
			return hash;
		}
	}
}
