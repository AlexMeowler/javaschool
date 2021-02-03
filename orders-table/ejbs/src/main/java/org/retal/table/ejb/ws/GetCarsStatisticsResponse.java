
package org.retal.table.ejb.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="totalCars" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="carsAvailable" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="carsAssigned" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="carsBroken" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
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
    "totalCars",
    "carsAvailable",
    "carsAssigned",
    "carsBroken"
})
@XmlRootElement(name = "getCarsStatisticsResponse")
public class GetCarsStatisticsResponse {

    protected int totalCars;
    protected int carsAvailable;
    protected int carsAssigned;
    protected int carsBroken;

    /**
     * Gets the value of the totalCars property.
     * 
     */
    public int getTotalCars() {
        return totalCars;
    }

    /**
     * Sets the value of the totalCars property.
     * 
     */
    public void setTotalCars(int value) {
        this.totalCars = value;
    }

    /**
     * Gets the value of the carsAvailable property.
     * 
     */
    public int getCarsAvailable() {
        return carsAvailable;
    }

    /**
     * Sets the value of the carsAvailable property.
     * 
     */
    public void setCarsAvailable(int value) {
        this.carsAvailable = value;
    }

    /**
     * Gets the value of the carsAssigned property.
     * 
     */
    public int getCarsAssigned() {
        return carsAssigned;
    }

    /**
     * Sets the value of the carsAssigned property.
     * 
     */
    public void setCarsAssigned(int value) {
        this.carsAssigned = value;
    }

    /**
     * Gets the value of the carsBroken property.
     * 
     */
    public int getCarsBroken() {
        return carsBroken;
    }

    /**
     * Sets the value of the carsBroken property.
     * 
     */
    public void setCarsBroken(int value) {
        this.carsBroken = value;
    }

}
