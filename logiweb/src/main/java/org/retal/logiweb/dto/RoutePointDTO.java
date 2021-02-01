package org.retal.logiweb.dto;

import org.retal.logiweb.domain.RoutePoint;

/**
 * DTO for entity {@linkplain org.retal.logiweb.domain.RoutePoint RoutePoint}.
 * @author Alexander Retivov
 *
 */
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
