package org.retal.logiweb.dto;

import org.retal.logiweb.domain.entity.RoutePoint;

/**
 * DTO for entity {@linkplain org.retal.logiweb.domain.entity.RoutePoint RoutePoint}.
 * @author Alexander Retivov
 *
 */
public class RoutePointDTO extends RoutePoint {
  
  public RoutePointDTO() {
    
  }
  
  /**
   * Constructor for creating new instance of this class. Use it to avoid multiple setters calls.
   */
  public RoutePointDTO(String cityName, Boolean isLoading, Integer cargoId) {
    setCityName(cityName);
    setIsLoading(isLoading);
    setCargoId(cargoId);
  }

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
