package org.retal.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

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
	public void test1CityWritingAndReading() {
		City city = new City();
		city.setCurrentCity("Moscow");
		cityDAO.add(city);
		city = new City();
		city.setCurrentCity("Samara");
		cityDAO.add(city);
		city = new City();
		city.setCurrentCity("Omsk");
		cityDAO.add(city);
		City readCity = cityDAO.read("Moscow");
		assertEquals("Moscow", readCity.getCurrentCity());
	}
	
	@Test(expected = MethodUndefinedException.class)
	public void test2CityDeletion() {
		City city = cityDAO.read("Omsk");
		cityDAO.delete(city);
	}
	
	@Test(expected = MethodUndefinedException.class)
	public void test3CityUpdate() {
		City city = cityDAO.read("Omsk");
		cityDAO.update(city);
	}
	
	@Test
	public void test4CityReadAll() {
		List<City> cities = cityDAO.readAll();
		String[] cityNames = {"Moscow", "Samara", "Omsk"};
		for(String name : cityNames) {
			assertTrue(cities.stream()
					.filter(c -> c.getCurrentCity().equals(name)).count() == 1);
		}
	}
	
	@Test
	public void test5UserWriteAndRead() {
		User user = new User();
		UserInfo userInfo = new UserInfo();
		City city = cityDAO.read("Moscow");
		userInfo.setCity(city);
		user.setId(1);
		user.setLogin("admin");
		user.setUserInfo(userInfo);
		user.setRole("admin");
		userDAO.add(user);
		User readUser = userDAO.read(Integer.valueOf(1));
		assertEquals("admin", readUser.getLogin());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test6CityWrongPKRead() {
		cityDAO.read(25.8);
	}
	
	@Test
	public void test7CityNotFound() {
		assertEquals(null, cityDAO.read("ABC"));
	}
	
	@Test
	public void test8UserNotFound() {
		assertEquals(null, userDAO.read(Integer.valueOf(100)));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test9UserWrongPK() {
		userDAO.read("1");
	}
	
	@Test
	public void testA1UserUpdate() {
		User user = userDAO.read(Integer.valueOf(1));
		user.getUserInfo().setName("Alex");
		userDAO.update(user);
		user = userDAO.read(Integer.valueOf(1));
		assertEquals("Alex", user.getUserInfo().getName());
	}
	
	@Test
	public void testA2UserUpdate() {
		List<User> users = userDAO.readAll();
		assertTrue(users.size() == 1);
	}
	
	@Test
	public void testA3UserFindByName() {
		User user = userDAO.findUser("admin");
		assertNotEquals(null, user);
	}
	
	@Test
	public void testA4UserGetAllWithRole() {
		List<User> admins = userDAO.readAllWithRole("admin");
		assertTrue(admins.size() == 1);
	}
	
	@Test
	public void testA5UserDelete() {
		User user = userDAO.read(Integer.valueOf(1));
		userDAO.delete(user);
		user = userDAO.read(Integer.valueOf(1));
		assertEquals(null, user);
	}
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private CityDAO cityDAO;
}
