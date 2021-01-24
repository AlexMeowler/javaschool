package org.retal.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.retal.dao.CityDAO;
import org.retal.dao.CityDistanceDAO;
import org.retal.domain.City;
import org.retal.domain.CityDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service, containing business-logic methods regarding {@linkplain org.retal.domain.City City} and
 * {@linkplain org.retal.domain.CityDistance CityDistance} entities.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class CityService {

  private final CityDAO cityDAO;

  private final CityDistanceDAO cityDistanceDAO;

  /**
   * Creates an instance of this class using constructor-based dependency injection.
   */
  @Autowired
  public CityService(CityDAO cityDAO, CityDistanceDAO cityDistanceDAO) {
    this.cityDAO = cityDAO;
    this.cityDistanceDAO = cityDistanceDAO;
  }

  /**
   * Loads cities from text file to database.
   */
  public void addCitiesFromFile() {
    addCitiesFromFile("citynames");
  }

  /**
   * Loads cities from text file to database.
   * 
   * @param cityNamesFileName name of the text file which contains information about cities
   */
  public void addCitiesFromFile(String cityNamesFileName) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          CityService.class.getResourceAsStream("/" + cityNamesFileName + ".txt")));
      String line;
      while ((line = reader.readLine()) != null) {
        City city = new City();
        city.setCurrentCity(line);
        cityDAO.add(city);
      }
      reader.close();
    } catch (IOException e) {
      String message = "I/O error has occurred";
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message, e);
    } catch (NullPointerException e) {
      String message = "File" + cityNamesFileName + ".txt not found";
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, message, e);
    }
  }

  /**
   * Loads city distances from text file to database.
   */
  public void addDistancesFromFile() {
    addDistancesFromFile("graph");
  }

  /**
   * Loads city distances from text file to database.
   * 
   * @param distancesFileName name of text file with information about distances between cities
   */
  public void addDistancesFromFile(String distancesFileName) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          CityService.class.getResourceAsStream("/" + distancesFileName + ".txt")));
      String line;
      while ((line = reader.readLine()) != null) {
        CityDistance citydist = new CityDistance();
        String[] args = line.split(" ");
        citydist.setCityA(args[0].replace("_", " "));
        citydist.setCityB(args[1].replace("_", " "));
        citydist.setDistance((int) Double.parseDouble(args[2]));
        cityDistanceDAO.add(citydist);
      }
      reader.close();
    } catch (IOException e) {
      String message = "I/O error has occurred";
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message, e);
    } catch (NullPointerException e) {
      String message = "File " + distancesFileName + ".txt not found";
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, message, e);
    }
  }

  public List<City> getAllCities() {
    return cityDAO.readAll();
  }
}
