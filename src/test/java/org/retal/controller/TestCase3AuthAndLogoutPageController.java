package org.retal.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.retal.config.spring.RootConfig;
import org.retal.config.spring.WebConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfig.class, RootConfig.class})
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCase3AuthAndLogoutPageController {

  private MockMvc mockMvc;

  private static final Logger log = Logger.getLogger(TestCase4AdminPageController.class);

  private static int counter = 1;

  @Autowired
  public void setMockMvc(WebApplicationContext wac) {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @BeforeClass
  public static void addUsersToDataBase() {
    TestCase4AdminPageController.createAdminAndDriverUsers();
  }

  @AfterClass
  public static void cleanup() {
    TestCase4AdminPageController.cleanup();
  }

  @Before
  public void startTest() {
    log.info("---Starting test " + counter + "---");
  }

  @After
  public void endTest() {
    log.info("---Ending test " + counter + "---");
    counter++;
  }

  @Test
  public void testA1AuthFail() throws Exception {
    mockMvc
        .perform(formLogin("/spring_auth").user("j_login", "admin").password("j_password", "passw"))
        .andExpect(redirectedUrl("/spring_auth?error"));
  }

  @Test
  public void testA2AuthSuccess() throws Exception {
    mockMvc.perform(formLogin("/spring_auth").user("j_login", "administrator")
        .password("j_password", "password"))
        .andExpect(redirectedUrl(AdminPageController.ADMIN_PAGE));
  }

  @Test
  public void testA3AuthLogoutController() throws Exception {
    mockMvc.perform(get("/spring_auth?logout=")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/home"));
  }

  @Test
  public void testA4AuthLogoutDriver() throws Exception {
    mockMvc.perform(get("/spring_auth?error=")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/home"));
  }

  @Test
  @WithMockUser(username = "administrator", password = "password", authorities = "ADMIN")
  public void testA5logOut() throws Exception {
    mockMvc.perform(logout("/logout")).andExpect(redirectedUrl("/spring_auth?logout"));
  }
}
