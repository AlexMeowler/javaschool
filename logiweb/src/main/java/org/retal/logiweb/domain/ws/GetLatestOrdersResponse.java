//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.3.2
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2021.02.03 at 02:52:51 PM MSK
//


package org.retal.logiweb.domain.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element name="orderList" type="{http://retal.org/logiweb/ws}orderList"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"orderList"})
@XmlRootElement(name = "getLatestOrdersResponse")
public class GetLatestOrdersResponse {

  @XmlElement(required = true)
  protected OrderList orderList;

  /**
   * Gets the value of the orderList property.
   * 
   * @return possible object is {@link OrderList }
   * 
   */
  public OrderList getOrderList() {
    return orderList;
  }

  /**
   * Sets the value of the orderList property.
   * 
   * @param value allowed object is {@link OrderList }
   * 
   */
  public void setOrderList(OrderList value) {
    this.orderList = value;
  }

}
