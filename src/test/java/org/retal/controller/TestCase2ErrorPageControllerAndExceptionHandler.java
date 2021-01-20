package org.retal.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
public class TestCase2ErrorPageControllerAndExceptionHandler {

  private MockMvc mockMvc;

  @Autowired
  public void setMockMvc(WebApplicationContext wac) {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void testA1getErrorsPageAndExceptionHandling() throws Exception {
    mockMvc.perform(get("/exception")).andExpect(status().is5xxServerError())
        .andExpect(redirectedUrl(GlobalExceptionHandler.ERROR_PAGE))
        .andExpect(flash().attribute("errorCode", "Error 500"));
    mockMvc.perform(get("/exception502")).andExpect(status().is5xxServerError())
        .andExpect(redirectedUrl(GlobalExceptionHandler.ERROR_PAGE))
        .andExpect(flash().attribute("errorCode", "Error 502"));
    mockMvc.perform(get("/exception503")).andExpect(status().is5xxServerError())
    .andExpect(redirectedUrl(GlobalExceptionHandler.ERROR_PAGE))
    .andExpect(flash().attribute("errorCode", "Error 503"));
    mockMvc.perform(get("/" + GlobalExceptionHandler.ERROR_PAGE)).andExpect(status().isOk())
        .andExpect(forwardedUrl("/pages/errorPage.jsp"));
  }

  @Test
  public void testA2Get403ErrorPage() throws Exception {
    mockMvc.perform(get("/403")).andExpect(status().isOk())
        .andExpect(forwardedUrl("/pages/error403.jsp"));
    mockMvc.perform(get("/403/name")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/403")).andExpect(flash().attribute("username", "name"));
    mockMvc.perform(get("/403/null")).andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/403")).andExpect(flash().attribute("username", "null"));
  }

  @Test
  public void testA3Get404ErrorPage() throws Exception {
    mockMvc.perform(get("/404")).andExpect(status().isOk())
        .andExpect(forwardedUrl("/pages/error404.jsp"));
    mockMvc.perform(get("/abc")).andExpect(status().is4xxClientError());
  }
}
