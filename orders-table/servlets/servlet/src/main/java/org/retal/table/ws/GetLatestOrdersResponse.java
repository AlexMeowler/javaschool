
package org.retal.table.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
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
@XmlType(name = "", propOrder = {
    "orderList"
})
@XmlRootElement(name = "getLatestOrdersResponse")
public class GetLatestOrdersResponse {

    @XmlElement(required = true)
    protected OrderList orderList;

    /**
     * Gets the value of the orderList property.
     * 
     * @return
     *     possible object is
     *     {@link OrderList }
     *     
     */
    public OrderList getOrderList() {
        return orderList;
    }

    /**
     * Sets the value of the orderList property.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderList }
     *     
     */
    public void setOrderList(OrderList value) {
        this.orderList = value;
    }

}
