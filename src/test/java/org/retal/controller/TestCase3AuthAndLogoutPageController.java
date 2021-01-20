package org.retal.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.retal.config.spring.RootConfig;
import org.retal.config.spring.WebConfig;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  public void setMockMvc(WebApplicationContext wac) {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void testA1getAuthFail() throws Exception {
    mockMvc.perform(get("/spring_auth?error=")).andExpect(redirectedUrl("/home"))
        .andExpect(flash().attribute("message", "Invalid login or password"));
  }
  
  @Test
  public void testA2getAuthLogout() throws Exception {
    mockMvc.perform(get("/spring_auth?logout=")).andExpect(redirectedUrl("/home"))
    .andExpect(flash().attribute("message", "Logged out successfully"));
  }
  
  @Test
  public void testA3getLogout() throws Exception {
    mockMvc.perform(post("/logout"));
  }
}
