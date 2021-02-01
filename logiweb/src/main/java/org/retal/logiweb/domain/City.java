package org.retal.logiweb.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.retal.logiweb.dto.CityDTO;

/**
 * Entity class for city.
 * @author Alexander Retivov
 *
 */
@Entity
@Table(name = "map_country_cities")
public class City {

  public City() {

  }

  /**
   * Constructor for mapping city DTO object to city entity.
   * 
   * @param cityDTO an instance of {@linkplain org.retal.logiweb.dto.CityDTO CityDTO}
   */
  public City(CityDTO cityDTO) {
    setCurrentCity(cityDTO.getCurrentCity());
    setUserInfos(cityDTO.getUserInfos());
    setCars(cityDTO.getCars());
    setPoints(cityDTO.getPoints());
  }

  @Id
  @Column(name = "city")
  private String currentCity;

  @OneToMany(mappedBy = "city", fetch = FetchType.EAGER)
  @JsonIgnore
  private Set<UserInfo> userInfos;

  @OneToMany(mappedBy = "location", fetch = FetchType.EAGER)
  @JsonIgnore
  private Set<Car> cars;

  @OneToMany(mappedBy = "city")
  @JsonIgnore
  private Set<RoutePoint> points;

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

  public Set<Car> getCars() {
    return cars;
  }

  public void setCars(Set<Car> cars) {
    this.cars = cars;
  }

  public Set<RoutePoint> getPoints() {
    return points;
  }

  public void setPoints(Set<RoutePoint> points) {
    this.points = points;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof City) {
      return currentCity.equals(((City) o).getCurrentCity());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 0;
    char[] chars = currentCity.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      hash += chars[i] * (i + 1);
    }
    return hash;
  }

  @Override
  public String toString() {
    return "City " + currentCity;
  }
}
