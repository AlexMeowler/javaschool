package org.retal.logiweb.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "route_points")
public class RoutePoint {

  public RoutePoint() {}

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;

  @ManyToOne
  @JoinColumn(name = "city")
  private City city;

  @Column(name = "isLoading")
  private Boolean isLoading;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "cargo_id", nullable = false)
  private Cargo cargo;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public City getCity() {
    return city;
  }

  public void setCity(City city) {
    this.city = city;
  }

  public Boolean getIsLoading() {
    return isLoading;
  }

  public void setIsLoading(Boolean isLoading) {
    this.isLoading = isLoading;
  }

  public Cargo getCargo() {
    return cargo;
  }

  public void setCargo(Cargo cargo) {
    this.cargo = cargo;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof RoutePoint) {
      RoutePoint rp = (RoutePoint) o;
      return this.cargo.equals(rp.cargo) && this.isLoading.equals(rp.isLoading)
          && this.city.equals(rp.city);
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "Route point id = " + id + " city " + city.getCurrentCity() + " isLoading " + isLoading;
  }
}
