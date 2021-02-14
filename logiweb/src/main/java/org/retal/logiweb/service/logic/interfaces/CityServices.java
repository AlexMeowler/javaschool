package org.retal.logiweb.service.logic.interfaces;

/**
 * Interface, containing list of required business-logic methods regarding
 * {@linkplain org.retal.logiweb.domain.entity.City City} and
 * {@linkplain org.retal.logiweb.domain.entity.CityDistance CityDistance} entities.
 * 
 * @author Alexander Retivov
 *
 */
public interface CityServices {

  /**
   * Loads cities from text file to database.
   * 
   * @param cityNamesFileName name of the text file which contains information about cities
   */
  public void addCitiesFromFile(String cityNamesFileName);
  
  /**
   * Loads city distances from text file to database.
   * 
   * @param distancesFileName name of text file with information about distances between cities
   */
  public void addDistancesFromFile(String distancesFileName);
}
