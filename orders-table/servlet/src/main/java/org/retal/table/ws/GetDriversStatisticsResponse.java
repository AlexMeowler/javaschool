
package org.retal.table.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="totalDrivers" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="driversAvailable" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="driversUnavailable" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"totalDrivers", "driversAvailable", "driversUnavailable"})
@XmlRootElement(name = "getDriversStatisticsResponse")
public class GetDriversStatisticsResponse {

  protected int totalDrivers;
  protected int driversAvailable;
  protected int driversUnavailable;

  /**
   * Gets the value of the totalDrivers property.
   * 
   */
  public int getTotalDrivers() {
    return totalDrivers;
  }

  /**
   * Sets the value of the totalDrivers property.
   * 
   */
  public void setTotalDrivers(int value) {
    this.totalDrivers = value;
  }

  /**
   * Gets the value of the driversAvailable property.
   * 
   */
  public int getDriversAvailable() {
    return driversAvailable;
  }

  /**
   * Sets the value of the driversAvailable property.
   * 
   */
  public void setDriversAvailable(int value) {
    this.driversAvailable = value;
  }

  /**
   * Gets the value of the driversUnavailable property.
   * 
   */
  public int getDriversUnavailable() {
    return driversUnavailable;
  }

  /**
   * Sets the value of the driversUnavailable property.
   * 
   */
  public void setDriversUnavailable(int value) {
    this.driversUnavailable = value;
  }

}
