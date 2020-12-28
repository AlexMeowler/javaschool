package org.retal.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.retal.dao.CityDAO;
import org.retal.domain.City;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CityService {
	
	public void addCitiesFromFile() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(CityService.class.getResourceAsStream("/citynames.txt")));
			String line;
			while((line = reader.readLine()) != null) {
				City city = new City();
				city.setCurrentCity(line);
				cityDAO.add(city);
			}
			reader.close();
		} catch (IOException e) {
			log.error("File citynames.txt not found");
		}
	}
	
	public List<City> getAllCities() {
		return cityDAO.readAll();
	}
	
	@Autowired 
	private CityDAO cityDAO;
	
	private static final Logger log = Logger.getLogger(CityService.class);
}
