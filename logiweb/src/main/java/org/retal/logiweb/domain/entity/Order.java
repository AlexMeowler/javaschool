package org.retal.logiweb.domain.entity;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entity class for orders.
 * 
 * @author Alexander Retivov
 *
 */
@Entity
@Table(name = "orders")
public class Order {

  public static final String ROUTE_DELIMETER = ";";

  public Order() {

  }

  public Order(Boolean isCompleted, Car car, Set<RoutePoint> points, String route,
      Float requiredCapacity, Integer requiredShiftLength) {
    setIsCompleted(isCompleted);
    setCar(car);
    setPoints(points);
    setRoute(route);
    setRequiredCapacity(requiredCapacity);
    setRequiredShiftLength(requiredShiftLength);
  }

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "isCompleted")
  private Boolean isCompleted;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "car_id", referencedColumnName = "registration_id")
  private Car car;

  @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
  private Set<RoutePoint> points;

  @Column(name = "route")
  private String route;

  @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
  private Set<UserInfo> driverInfo;

  @OneToOne(mappedBy = "order", fetch = FetchType.EAGER)
  @PrimaryKeyJoinColumn
  private OrderRouteProgression orderRouteProgression;

  @Transient
  private Set<Cargo> cargo;

  @Column(name = "required_capacity")
  private Float requiredCapacity;

  @Column(name = "required_shift_length")
  private Integer requiredShiftLength;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Boolean getIsCompleted() {
    return isCompleted;
  }

  public void setIsCompleted(Boolean isCompleted) {
    this.isCompleted = isCompleted;
  }

  public Car getCar() {
    return car;
  }

  public void setCar(Car car) {
    this.car = car;
  }

  public Set<RoutePoint> getPoints() {
    return points;
  }

  public void setPoints(Set<RoutePoint> points) {
    this.points = points;
  }

  public String getRoute() {
    return route;
  }

  public void setRoute(String route) {
    this.route = route;
  }

  public Set<UserInfo> getDriverInfo() {
    return driverInfo;
  }

  public void setDriverInfo(Set<UserInfo> driverInfo) {
    this.driverInfo = driverInfo;
  }

  public OrderRouteProgression getOrderRouteProgression() {
    return orderRouteProgression;
  }

  public void setOrderRouteProgression(OrderRouteProgression orderRouteProgression) {
    this.orderRouteProgression = orderRouteProgression;
  }

  public Set<Cargo> getCargo() {
    return cargo;
  }

  public void setCargo(Set<Cargo> cargo) {
    this.cargo = cargo;
  }

  public Float getRequiredCapacity() {
    return requiredCapacity;
  }

  public void setRequiredCapacity(Float requiredCapacity) {
    this.requiredCapacity = requiredCapacity;
  }

  public Integer getRequiredShiftLength() {
    return requiredShiftLength;
  }

  public void setRequiredShiftLength(Integer requiredShiftLength) {
    this.requiredShiftLength = requiredShiftLength;
  }

  @Override
  public String toString() {
    return "Order id = " + id + " isCompleted = " + isCompleted;
  }
}
