package nz.ac.canterbury.seng302.identityprovider.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

public class AddAndDeleteRolesStepDefs {
    private User testUser;

    private UserRole stringToRole(String input) {
        String role = input.toUpperCase(Locale.ROOT);
        UserRole roleEnum = UserRole.valueOf(input);
        return roleEnum;
    }

    @Given("There is a user with the role {string}")
    public void there_is_a_user_with_the_role(String string) {
        testUser = new User(
                "Name",
                "password",
                "Jane",
                "Dorothy",
                "Doe",
                "???",
                "Hi",
                "They/them",
                "a@b.c"
        );
        String role = string.toUpperCase(Locale.ROOT);
        UserRole roleEnum = UserRole.valueOf(string);
        testUser.addRole(roleEnum);
        assertTrue(testUser.getRoles().contains(roleEnum));
    }

    @When("I add the role {string} to the user")
    public void i_add_the_role_to_the_user(String string) {
        String role = string.toUpperCase(Locale.ROOT);
        UserRole roleEnum = UserRole.valueOf(string);
        testUser.addRole(roleEnum);
    }

    @Given("The user has the role {string}")
    public void the_user_has_the_role(String string) {
        String role = string.toUpperCase(Locale.ROOT);
        UserRole roleEnum = UserRole.valueOf(string);
        assertTrue(testUser.getRoles().contains(roleEnum));
    }

    @When("I delete the role {string} from the user")
    public void i_delete_the_role_from_the_user(String string) {
        String role = string.toUpperCase(Locale.ROOT);
        UserRole roleEnum = UserRole.valueOf(string);
        testUser.removeRole(roleEnum);
    }


    @Then("The user has {int} roles")
    public void the_user_has_roles(Integer expectedSize) {
        assertTrue(testUser.getRoles().size() == expectedSize);
    }
}
