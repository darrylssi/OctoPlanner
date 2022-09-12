package nz.ac.canterbury.seng302.portfolio.cucumber;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.portfolio.model.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import org.hamcrest.Matchers;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Class containing the step definitions for the milestone_validation Cucumber
 * feature
 */
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

    /**
     * Checks against the validation utils validator
     * @return Validation errors
     */
    ValidationError checkValidator() {
        // TODO this is SUS!!! Should really just be parentProject without having to get it from EventStepDefinition
        return ValidationUtils.validateMilestoneDates(milestone.getStartDate(), EventStepDefinition.getParentProject());
    }


    @When("the user creates a milestone called {string}, on {string}, with a description {string}")
    public void the_user_creates_a_milestone_called_on_with_a_description(
            String name, String date, String description) {
        milestone = new Milestone();
        milestone.setName(name);
        milestone.setStartDate(DateUtils.toDate(date));
        milestone.setDescription(description);
    }

    @Then("there are no errors in creating the milestone")
    public void there_are_no_errors_in_creating_the_milestone() {
        // 1. Check the constraint annotations
        var javaxErrors = checkJavaxConstraints();
        assertThat(javaxErrors, is(empty()));
        // 2. Check the validation utils errors
        var validationErrors = checkValidator();
        assertThat(validationErrors.getErrorMessages(), is(empty()));

    }

    @Then("creating the milestone should have {string}")
    public void creating_the_milestone_should_have_error(
            String errorMessage) {
        var javaxErrors = checkJavaxConstraints();
        if (!javaxErrors.isEmpty()) {
            assertEquals(errorMessage, javaxErrors.iterator().next().getMessage());
        }

        var validationErrors = checkValidator();
        assertTrue(!javaxErrors.isEmpty() || !validationErrors.getErrorMessages().isEmpty());
    }

}
