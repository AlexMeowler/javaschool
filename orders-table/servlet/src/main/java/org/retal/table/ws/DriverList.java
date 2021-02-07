
package org.retal.table.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Java class for driverList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="driverList"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="drivers" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "driverList", propOrder = {"drivers"})
public class DriverList {

  @XmlElement(required = true)
  protected List<String> drivers;

  /**
   * Gets the value of the drivers property.
   * 
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the drivers property.
   * 
   * <p>For example, to add a new item, do as follows:
   * 
   * <pre>
   * getDrivers().add(newItem);
   * </pre>
   * 
   * 
   * <p>Objects of the following type(s) are allowed in the list {@link String }
   * 
   * 
   */
  public List<String> getDrivers() {
    if (drivers == null) {
      drivers = new ArrayList<>();
    }
    return this.drivers;
  }

}
