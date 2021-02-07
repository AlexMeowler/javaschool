
package org.retal.table.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Java class for orderWS complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="orderWS"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="isCompleted" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="car" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="route" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="driverList" type="{http://retal.org/logiweb/ws}driverList"/&gt;
 *         &lt;element name="cargoList" type="{http://retal.org/logiweb/ws}cargoList"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orderWS",
    propOrder = {"id", "isCompleted", "car", "route", "driverList", "cargoList"})
public class OrderWS {

  protected int id;
  protected boolean isCompleted;
  @XmlElement(required = true)
  protected String car;
  @XmlElement(required = true)
  protected String route;
  @XmlElement(required = true)
  protected DriverList driverList;
  @XmlElement(required = true)
  protected CargoList cargoList;

  /**
   * Gets the value of the id property.
   * 
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the value of the id property.
   * 
   */
  public void setId(int value) {
    this.id = value;
  }

  /**
   * Gets the value of the isCompleted property.
   * 
   */
  public boolean isIsCompleted() {
    return isCompleted;
  }

  /**
   * Sets the value of the isCompleted property.
   * 
   */
  public void setIsCompleted(boolean value) {
    this.isCompleted = value;
  }

  /**
   * Gets the value of the car property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getCar() {
    return car;
  }

  /**
   * Sets the value of the car property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setCar(String value) {
    this.car = value;
  }

  /**
   * Gets the value of the route property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getRoute() {
    return route;
  }

  /**
   * Sets the value of the route property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setRoute(String value) {
    this.route = value;
  }

  /**
   * Gets the value of the driverList property.
   * 
   * @return possible object is {@link DriverList }
   * 
   */
  public DriverList getDriverList() {
    return driverList;
  }

  /**
   * Sets the value of the driverList property.
   * 
   * @param value allowed object is {@link DriverList }
   * 
   */
  public void setDriverList(DriverList value) {
    this.driverList = value;
  }

  /**
   * Gets the value of the cargoList property.
   * 
   * @return possible object is {@link CargoList }
   * 
   */
  public CargoList getCargoList() {
    return cargoList;
  }

  /**
   * Sets the value of the cargoList property.
   * 
   * @param value allowed object is {@link CargoList }
   * 
   */
  public void setCargoList(CargoList value) {
    this.cargoList = value;
  }

}
