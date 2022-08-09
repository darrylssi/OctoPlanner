package nz.ac.canterbury.seng302.portfolio.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import nz.ac.canterbury.seng302.portfolio.factory.WithMockCustomUserSecurityContextFactory;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.utils.RoleUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Contains Cucumber acceptance tests for events.
 *
 * Tests the event controller to make sure that added events redirect properly and provide the correct message.
 */
public class EventStepDefinition {

    private User user;
    private Map<String, Object> sessionAttributes;

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    private static final int ID = 1;
    private String name;
    private String startDate;
    private String endDate;
    private String description;

    @Given("the user has a role of {string}")
    public void the_user_has_a_role_of(String sRole) {
        user = new User("username", "firstName", "middleName", "lastName", "nickname", "personalPronouns",
                "password", "confirmPassword", "email", "bio");
        UserRole role = RoleUtils.fromString(sRole);    // If this fails, check the spelling
        var auth = new WithMockCustomUserSecurityContextFactory().createSecurityContext(role, ID);
        sessionAttributes = Map.of("SPRING_SECURITY_CONTEXT", auth);
    }

    @When("the user creates an event with {string}, {string}, {string}, {string}")
    public void the_user_creates_an_event_with(String name, String startDate, String endDate, String description) throws Exception {
        result = this.mockMvc.perform(post("/add-event/0").sessionAttrs(sessionAttributes)
                        .param("eventName", name)
                        .param("eventDescription", description)
                        .param("eventStartDate", startDate)
                        .param("eventEndDate", endDate))
                .andReturn();
    }

    @Then("an event called {string} exists with {string}, {string}, {string}")
    public void an_event_called_exists_with(String name, String startDate, String endDate, String description) throws Exception {
        this.mockMvc.perform(post("/add-event/0").sessionAttrs(sessionAttributes)
                        .param("eventName", name)
                        .param("eventDescription", description)
                        .param("eventStartDate", startDate)
                        .param("eventEndDate", endDate))
                .andExpect(status().is3xxRedirection());
    }

    @Then("{string} message should be displayed")
    public void message_should_be_displayed(String message) {
        assertEquals(message, String.valueOf(result.getResponse().getStatus()));
    }

}