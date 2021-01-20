package org.retal.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.log4j.Logger;
import org.retal.dao.CityDAO;
import org.retal.dao.CityDistanceDAO;
import org.retal.domain.City;
import org.retal.domain.CityDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service, containing business-logic methods regarding {@linkplain org.retal.domain.City City}
 * and {@linkplain org.retal.domain.CityDistance CityDistance} entities.
 * 
 * @author Alexander Retivov
 *
 */
@Service
public class CityService {
  
  private final CityDAO cityDAO;

  private final CityDistanceDAO cityDistanceDAO;

  private static final Logger log = Logger.getLogger(CityService.class);
  
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
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(CityService.class.getResourceAsStream("/citynames.txt")));
      String line;
      while ((line = reader.readLine()) != null) {
        City city = new City();
        city.setCurrentCity(line);
        cityDAO.add(city);
      }
      reader.close();
    } catch (IOException e) {
      String message = "File citynames.txt not found";
      log.error(message);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
  }

  /**
   * Loads city distances from text file to database.
   */
  public void addDistancesFromFile() {
    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(CityService.class.getResourceAsStream("/graph.txt")));
      String line;
      while ((line = reader.readLine()) != null) {
        CityDistance citydist = new CityDistance();
        String[] args = line.split(" ");
        citydist.setCityA(args[0].replaceAll("_", " "));
        citydist.setCityB(args[1].replaceAll("_", " "));
        citydist.setDistance((int) Double.parseDouble(args[2]));
        cityDistanceDAO.add(citydist);
      }
      reader.close();
    } catch (IOException e) {
      String message = "File graph.txt not found";
      log.error(message);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
  }

  public List<City> getAllCities() {
    return cityDAO.readAll();
  }
}
