package org.retal.logiweb.domain.entity;

import java.util.Arrays;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Entity class for order route progression. This is a utility entity to track movements for drivers
 * assigned to some order and show them their next route point.
 * 
 * @author Alexander Retivov
 *
 */
@Entity
@Table(name = "completed_orders_info")
public class CompletedOrderInfo {
  
  public static final String DRIVERS_DELIMETER = ";";

  public CompletedOrderInfo() {

  }

  /**
   * Constructor for creating new instance of this class. Use it to avoid multiple setters calls.
   */
  public CompletedOrderInfo(String carId, String drivers, Order order) {
    setCarId(carId);
    setDrivers(drivers);
    setOrder(order);
  }

  @Id
  @Column(name = "order_id")
  private int id;

  @Column(name = "car_id")
  private String carId;

  @Column(name = "drivers")
  private String drivers;

  @OneToOne(fetch = FetchType.EAGER)
  @MapsId
  @JoinColumn(name = "order_id")
  private Order order;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getCarId() {
    return carId;
  }

  public void setCarId(String carId) {
    this.carId = carId;
  }

  public String getDrivers() {
    return drivers;
  }

  public void setDrivers(String drivers) {
    this.drivers = drivers;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }
  
  public List<String> driverStringToList() {
    return Arrays.asList(drivers.split(DRIVERS_DELIMETER));
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof CompletedOrderInfo) {
      return this.id == ((CompletedOrderInfo) o).id;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return ((carId + drivers).hashCode() + 1) * (int) Math.pow((double) id + 1, 2);
  }

  @Override
  public String toString() {
    return "Stored info for completed for order id = " + id + " is: car id = " + carId
        + "; drivers = " + drivers;
  }
}
