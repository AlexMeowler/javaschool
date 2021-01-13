package org.retal.dao;

import static org.junit.Assert.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.retal.config.spring.RootConfig;
import org.retal.config.spring.WebConfig;
import org.retal.domain.City;
import org.retal.domain.User;
import org.retal.domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, RootConfig.class})
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DAOTest {
	
	@Configuration
	static class ContextConfiguration {

        @Bean
        public UserDAO getUserDAO() {
            return new UserDAO();
        }
        
        @Bean
        public CityDAO getCityDAO() {
            return new CityDAO();
        }
    }
	
	@Test
	public void testACityWritingAndReading() {
		City city = new City();
		city.setCurrentCity("Moscow");
		cityDAO.add(city);
		City readCity = cityDAO.read("Moscow");
		assertEquals("Moscow", readCity.getCurrentCity());
	}
	
	@Test
	public void testBUserWriteAndRead() {
		User user = new User();
		UserInfo userInfo = new UserInfo();
		City city = cityDAO.read("Moscow");
		userInfo.setCity(city);
		user.setId(1);
		user.setLogin("admin");
		user.setUserInfo(userInfo);
		userDAO.add(user);
		User readUser = userDAO.read(Integer.valueOf(1));
		assertEquals("admin", readUser.getLogin());
	}
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private CityDAO cityDAO;
}
