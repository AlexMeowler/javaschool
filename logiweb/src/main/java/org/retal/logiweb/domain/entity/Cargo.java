package org.retal.logiweb.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import org.retal.logiweb.dto.CargoDTO;

/**
 * Entity class for cargo.
 * @author Alexander Retivov
 *
 */
@Entity
@Table(name = "cargo")
public class Cargo {

  public Cargo() {

  }

  /**
   * Constructor for mapping cargo DTO object to cargo entity.
   * 
   * @param cargoDTO an instance of {@linkplain org.retal.logiweb.dto.CargoDTO CargoDTO}
   */
  public Cargo(CargoDTO cargoDTO) {
    setId(cargoDTO.getId());
    setName(cargoDTO.getName());
    setMass(cargoDTO.getMass());
    setStatus(cargoDTO.getStatus());
    setPoint(cargoDTO.getPoints());
    setDescription(cargoDTO.getDescription());
  }

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "name")
  @NotEmpty(message = "Please enter cargo name.")
  private String name;

  @Column(name = "mass_kg")
  private Integer mass;

  @Column(name = "status")
  private String status;

  @OneToMany(mappedBy = "cargo", fetch = FetchType.EAGER)
  @JsonIgnore
  private Set<RoutePoint> points;

  @Column(name = "description")
  @NotEmpty(message = "Please enter cargo description.")
  private String description;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getMass() {
    return mass;
  }

  public void setMass(Integer mass) {
    this.mass = mass;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Set<RoutePoint> getPoints() {
    return points;
  }

  public void setPoint(Set<RoutePoint> points) {
    this.points = points;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Cargo) {
      return this.id == ((Cargo) o).getId();
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return "Cargo id = " + id + " name = " + name + " mass = " + mass + " status = " + status
        + " description = " + description;
  }
}
