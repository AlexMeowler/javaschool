
package org.retal.table.ejb.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.3.2
 * Generated source version: 2.2
 * 
 */
@WebService(name = "statistics", targetNamespace = "http://retal.org/logiweb/ws")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    ObjectFactory.class
})
public interface Statistics {


    /**
     * 
     * @param getDriversStatisticsRequest
     * @return
     *     returns org.retal.table.ejb.ws.GetDriversStatisticsResponse
     */
    @WebMethod
    @WebResult(name = "getDriversStatisticsResponse", targetNamespace = "http://retal.org/logiweb/ws", partName = "getDriversStatisticsResponse")
    public GetDriversStatisticsResponse getDriversStatistics(
        @WebParam(name = "getDriversStatisticsRequest", targetNamespace = "http://retal.org/logiweb/ws", partName = "getDriversStatisticsRequest")
        GetDriversStatisticsRequest getDriversStatisticsRequest);

    /**
     * 
     * @param getCarsStatisticsRequest
     * @return
     *     returns org.retal.table.ejb.ws.GetCarsStatisticsResponse
     */
    @WebMethod
    @WebResult(name = "getCarsStatisticsResponse", targetNamespace = "http://retal.org/logiweb/ws", partName = "getCarsStatisticsResponse")
    public GetCarsStatisticsResponse getCarsStatistics(
        @WebParam(name = "getCarsStatisticsRequest", targetNamespace = "http://retal.org/logiweb/ws", partName = "getCarsStatisticsRequest")
        GetCarsStatisticsRequest getCarsStatisticsRequest);

    /**
     * 
     * @param getLatestOrdersRequest
     * @return
     *     returns org.retal.table.ejb.ws.GetLatestOrdersResponse
     */
    @WebMethod
    @WebResult(name = "getLatestOrdersResponse", targetNamespace = "http://retal.org/logiweb/ws", partName = "getLatestOrdersResponse")
    public GetLatestOrdersResponse getLatestOrders(
        @WebParam(name = "getLatestOrdersRequest", targetNamespace = "http://retal.org/logiweb/ws", partName = "getLatestOrdersRequest")
        GetLatestOrdersRequest getLatestOrdersRequest);

}
