package org.retal.logiweb.dto;

import java.util.List;

/**
 * Wrapper for list of {@linkplain org.retal.logiweb.domain.RoutePoint RoutePoint}. Used in controllers to
 * get input from form on page and in validation process.
 * 
 * @author Alexander Retivov
 *
 */
public class RoutePointListWrapper {

  private List<RoutePointDTO> list;

  public List<RoutePointDTO> getList() {
    return list;
  }

  public void setList(List<RoutePointDTO> list) {
    this.list = list;
  }
}
