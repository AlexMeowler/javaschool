package org.retal.logiweb.service.logic.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.retal.logiweb.dao.impl.CityDAO;
import org.retal.logiweb.dao.impl.CityDistanceDAO;
import org.retal.logiweb.domain.entity.City;
import org.retal.logiweb.domain.entity.CityDistance;
import org.retal.logiweb.service.logic.interfaces.CityServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Implementation of {@link CityServices} interface.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class CityService implements CityServices {

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

  @Override
  public void addCitiesFromFile(String cityNamesFileName) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          CityService.class.getResourceAsStream("/" + cityNamesFileName + ".txt")));
      String line;
      while ((line = reader.readLine()) != null) {
        City city = new City(line);
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

  @Override
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
