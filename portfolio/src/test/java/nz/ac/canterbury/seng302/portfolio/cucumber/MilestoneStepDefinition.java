package nz.ac.canterbury.seng302.portfolio.cucumber;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.portfolio.model.Milestone;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class MilestoneStepDefinition extends RunCucumberTest{

    private Milestone milestone;

    /**
     * Validates event against its javax validation annotations
     * @return Constraint errors
     */
    Set<ConstraintViolation<Milestone>> checkJavaxConstraints() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator.validate(milestone);
    }

    @When("the user creates a milestone called {string}, on {string}, with a description {string}")
    public void the_user_creates_a_milestone_called_on_with_a_description(
            String name, String date, String description) throws Exception {
        milestone = new Milestone();
        milestone.setName(name);
        milestone.setStartDate(DateUtils.toDate(date));
        milestone.setDescription(description);
    }

    @Then("there are no errors in creating the milestone")
    public void there_are_no_errors_in_creating_the_milestone() {
        var javaxErrors = checkJavaxConstraints();
        assertThat(javaxErrors, is(empty()));
    }

    @Then("creating the milestone should have {string}")
    public void creating_the_milestone_should_have_error(
            String errorMessage) {
        var javaxErrors = checkJavaxConstraints();
        assertThat(javaxErrors, is(not(empty())));
        assertTrue(javaxErrors.contains(errorMessage));
    }

}
