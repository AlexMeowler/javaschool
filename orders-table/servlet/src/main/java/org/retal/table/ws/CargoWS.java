
package org.retal.table.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Java class for cargoWS complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cargoWS"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="from" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="to" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cargoWS", propOrder = {"id", "name", "from", "to"})
public class CargoWS {

  protected int id;
  @XmlElement(required = true)
  protected String name;
  @XmlElement(required = true)
  protected String from;
  @XmlElement(required = true)
  protected String to;

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
   * Gets the value of the name property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setName(String value) {
    this.name = value;
  }

  /**
   * Gets the value of the from property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getFrom() {
    return from;
  }

  /**
   * Sets the value of the from property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setFrom(String value) {
    this.from = value;
  }

  /**
   * Gets the value of the to property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getTo() {
    return to;
  }

  /**
   * Sets the value of the to property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setTo(String value) {
    this.to = value;
  }

}
