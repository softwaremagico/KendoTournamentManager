package com.softwaremagico.kt.security;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/***
 * Checks if a url path is protected or not by spring security.
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@Test(groups = "restAccess")
public class RestAccessTests extends AbstractTestNGSpringContextTests {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @BeforeClass
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void checkPublicRestService() throws Exception {
        //Info services are opened in rest-server library
        mockMvc.perform(get("/info/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void checkForbiddenRestService() throws Exception {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("user", "1");

        mockMvc.perform(get("/files/users")
                .params(requestParams))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void checkForbiddenRestServiceWithAuth() throws Exception {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("user", "1");

        mockMvc.perform(get("/files/users")
                .params(requestParams))
                .andExpect(status().isUnauthorized());
    }
}
