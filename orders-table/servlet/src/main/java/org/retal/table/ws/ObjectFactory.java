
package org.retal.table.ws;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each Java content interface and Java element interface
 * generated in the org.retal.table.ejb.ws package.
 * 
 * <p>An ObjectFactory allows you to programatically construct new instances of the Java 
 * representation for XML content. The Java representation of XML content can consist of schema 
 * derived interfaces and classes representing the binding of schema type definitions, element 
 * declarations and model groups. Factory methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


  /**
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes
   * for package: org.retal.table.ejb.ws
   * 
   */
  public ObjectFactory() {
    // Empty constructor
  }

  /**
   * Create an instance of {@link GetLatestOrdersRequest }.
   * 
   */
  public GetLatestOrdersRequest createGetLatestOrdersRequest() {
    return new GetLatestOrdersRequest();
  }

  /**
   * Create an instance of {@link GetLatestOrdersResponse }.
   * 
   */
  public GetLatestOrdersResponse createGetLatestOrdersResponse() {
    return new GetLatestOrdersResponse();
  }

  /**
   * Create an instance of {@link OrderList }.
   * 
   */
  public OrderList createOrderList() {
    return new OrderList();
  }

  /**
   * Create an instance of {@link GetDriversStatisticsRequest }.
   * 
   */
  public GetDriversStatisticsRequest createGetDriversStatisticsRequest() {
    return new GetDriversStatisticsRequest();
  }

  /**
   * Create an instance of {@link GetDriversStatisticsResponse }.
   * 
   */
  public GetDriversStatisticsResponse createGetDriversStatisticsResponse() {
    return new GetDriversStatisticsResponse();
  }

  /**
   * Create an instance of {@link GetCarsStatisticsRequest }.
   * 
   */
  public GetCarsStatisticsRequest createGetCarsStatisticsRequest() {
    return new GetCarsStatisticsRequest();
  }

  /**
   * Create an instance of {@link GetCarsStatisticsResponse }.
   * 
   */
  public GetCarsStatisticsResponse createGetCarsStatisticsResponse() {
    return new GetCarsStatisticsResponse();
  }

  /**
   * Create an instance of {@link DriverList }.
   * 
   */
  public DriverList createDriverList() {
    return new DriverList();
  }

  /**
   * Create an instance of {@link CargoList }.
   * 
   */
  public CargoList createCargoList() {
    return new CargoList();
  }

  /**
   * Create an instance of {@link CargoWS }.
   * 
   */
  public CargoWS createCargoWS() {
    return new CargoWS();
  }

  /**
   * Create an instance of {@link OrderWS }.
   * 
   */
  public OrderWS createOrderWS() {
    return new OrderWS();
  }

}
