package nz.ac.canterbury.seng302.portfolio.controller;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockPrincipal(TEACHER)
public class EditUserControllerTest {

    final Logger logger = LoggerFactory.getLogger(EditUserControllerTest.class);

    // The URL which the controller handles requests on.
    private String CONTROLLER_URL = "/users/1/edit";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void deletePhotoWhenNonePresent_thenShowError() throws Exception {
        this.mockMvc.perform(post(CONTROLLER_URL))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("User does not have a profile photo uploaded")));
    }

    // @Test
    // public void deleteExistingPhoto_getSuccess() throws Exception {
    //     // TODO: mock the response to be valid or (possibly) add a photo through the service.
    //     this.mockMvc.perform(post(CONTROLLER_URL))
    //         .andExpect(status().isOk())
    //         .andExpect(redirectedUrl("/users/1"));
    // }
}
