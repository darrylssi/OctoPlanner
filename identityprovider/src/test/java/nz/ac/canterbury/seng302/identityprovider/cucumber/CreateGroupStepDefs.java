package nz.ac.canterbury.seng302.identityprovider.cucumber;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.identityprovider.model.Group;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNot.not;

@SpringBootTest
public class CreateGroupStepDefs {

    private Group testGroup;

    Set<ConstraintViolation<Group>> checkJavaxConstraints(Group group) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator.validate(group);
    }

    @When("I try to create a group with short name {string} and long name {string}")
    public void iTryToCreateAGroupWithShortNameShortNameAndLongNameLongName(String shortName, String longName) {
        testGroup = new Group(shortName, longName);
    }

    @Then("the group should be saved to the database")
    public void theGroupShouldBeSavedToTheDatabase() {
        var javaxErrors = checkJavaxConstraints(testGroup);
        assertThat(javaxErrors, is(empty()));
    }

    @Then("the group should not be saved to the database")
    public void theGroupShouldNotBeSavedToTheDatabase() {
        var javaxErrors = checkJavaxConstraints(testGroup);
        assertThat(javaxErrors, is(not(empty())));
    }
}
