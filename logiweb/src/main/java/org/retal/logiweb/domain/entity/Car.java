package org.retal.logiweb.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.retal.logiweb.dto.CarDTO;

/**
 * Entity class for cars.
 * 
 * @author Alexander Retivov
 *
 */
@Entity
@Table(name = "cars")
public class Car {

  public Car() {

  }

  /**
   * Constructor for creating new instance of this class. Use it to avoid multiple setters calls.
   */
  public Car(String registrationId, Integer shiftLength, Float capacityTons, Boolean isWorking,
      City location) {
    setRegistrationId(registrationId);
    setShiftLength(shiftLength);
    setCapacityTons(capacityTons);
    setIsWorking(isWorking);
    setLocation(location);
  }

  /**
   * Constructor for mapping car DTO object to car entity.
   * 
   * @param carDTO an instance of {@linkplain org.retal.logiweb.dto.CarDTO CarDTO}
   */
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
  @JoinColumn(name = "location", nullable = false)
  @JsonIgnore
  private City location;

  @OneToOne(mappedBy = "car")
  @JsonIgnore
  private Order order;

  @OneToOne(mappedBy = "car", fetch = FetchType.EAGER)
  @JsonIgnore
  private UserInfo driver;

  public String getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(String registrationId) {
    this.registrationId = registrationId;
  }

  public Integer getShiftLength() {
    return shiftLength;
  }

  public void setShiftLength(Integer shiftLength) {
    this.shiftLength = shiftLength;
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

  public void setIsWorking(Boolean isWorking) {
    this.isWorking = isWorking;
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

  public UserInfo getDriver() {
    return driver;
  }

  public void setDriver(UserInfo driver) {
    this.driver = driver;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    for (int i = 0; i < registrationId.length(); i++) {
      hash += (i + 1) * registrationId.charAt(i);
    }
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Car) {
      Car car = (Car) o;
      return this.registrationId.equals(car.registrationId);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "Car ID = " + registrationId + "; capactity = " + capacityTons + "; location = "
        + location.getCurrentCity() + "; shift length = " + shiftLength;
  }
}
