
package org.retal.table.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Java class for orderList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="orderList"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="orders" type="{http://retal.org/logiweb/ws}orderWS" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orderList", propOrder = {"orders"})
public class OrderList {

  @XmlElement(required = true)
  protected List<OrderWS> orders;

  /**
   * Gets the value of the orders property.
   * 
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the orders property.
   * 
   * <p>For example, to add a new item, do as follows:
   * 
   * <pre>
   * getOrders().add(newItem);
   * </pre>
   * 
   * 
   * <p>Objects of the following type(s) are allowed in the list {@link OrderWS }
   * 
   * 
   */
  public List<OrderWS> getOrders() {
    if (orders == null) {
      orders = new ArrayList<>();
    }
    return this.orders;
  }

}
