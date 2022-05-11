package nz.ac.canterbury.seng302.portfolio.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.grpc.StatusException;
import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
public class RoleEditingPermissionsStepDefs {
    @Autowired
    private MockMvc mvc;

    @Autowired
    @MockBean
    private UserAccountClientService userAccountClientService = Mockito.mock(UserAccountClientService.class);

    private User testUser;
    private int testUserId = 0;
    private UserRole mockRole;
    private UserRole testRole;

    @Given("There is a user with the role {string}")
    public void there_is_a_user_with_the_role(String string) throws StatusException {
        testUser = new User(
                "Name",
                "Jane",
                "",
                "Doe",
                "J",
                "She/her",
                "password",
                "password",
                "a@b.c",
                ""
        );
        testRole = UserRole.valueOf(string);
        when(userAccountClientService.addRoleToUser(testUserId, testRole)).thenReturn(false);
    }

    @Given("I have the role {string}")
    public void i_have_the_role(String string) {
        mockRole = UserRole.valueOf(string);
    }

    @When("I try to add the role {string} to the user")
    public void i_try_to_add_the_role_to_the_user(String string) throws Exception {
        switch (mockRole){
            case STUDENT -> teacher_add_role(UserRole.valueOf(string));
            case TEACHER -> student_add_role(UserRole.valueOf(string));
            case COURSE_ADMINISTRATOR -> admin_add_role(UserRole.valueOf(string));
        }
    }

    @WithMockPrincipal(UserRole.TEACHER)
    private void teacher_add_role(UserRole role) throws Exception {
        this.mvc.perform(get("/edit-project/-1"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(containsString("Project not found")));
    }

    @WithMockPrincipal(UserRole.TEACHER)
    private void student_add_role(UserRole role) {
//        return false;
    }

    @WithMockPrincipal(UserRole.COURSE_ADMINISTRATOR)
    private void admin_add_role(UserRole role) {
//        return false;
    }

    @Then("I get the message {string}")
    public void i_get_the_message(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("I get a {string} response")
    public void i_get_a_response(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @When("I delete the role {string} from the user")
    public void i_delete_the_role_from_the_user(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Given("There is a user with the roles {string} and {string}")
    public void thereIsAUserWithTheRolesAnd(String arg0, String arg1) {
    }
}
