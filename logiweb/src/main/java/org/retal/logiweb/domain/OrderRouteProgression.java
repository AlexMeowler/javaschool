package org.retal.logiweb.domain;

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
@Table(name = "order_route_progression")
public class OrderRouteProgression {

  @Id
  @Column(name = "order_id")
  private int id;

  @Column(name = "route_counter")
  private Integer routeCounter;

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

  public Integer getRouteCounter() {
    return routeCounter;
  }

  public void setRouteCounter(Integer routeCounter) {
    this.routeCounter = routeCounter;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public void incrementCounter() {
    routeCounter++;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof OrderRouteProgression) {
      return this.id == ((OrderRouteProgression) o).id;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return (routeCounter + 1) * (int) Math.pow((double) id + 1, 2);
  }

  @Override
  public String toString() {
    return "Progression for order id = " + id + " is " + routeCounter;
  }
}
