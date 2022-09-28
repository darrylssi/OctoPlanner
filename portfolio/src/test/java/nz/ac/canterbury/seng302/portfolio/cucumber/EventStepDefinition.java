package nz.ac.canterbury.seng302.portfolio.cucumber;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Set;

import javax.persistence.ManyToOne;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.boot.test.context.SpringBootTest;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;

/**
 * Class containing the step definitions for the event_validation Cucumber
 * feature
 */
@SpringBootTest
public class EventStepDefinition extends RunCucumberTest {

    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat dateTimeFormatter;
    private Event event;

    /**
     * Validates event against its javax validation annotations
     * @return Constraint errors
     */
    Set<ConstraintViolation<Event>> checkJavaxConstraints() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator.validate(event);
    }

    /**
     * Checks against the validation utils validator
     * @return Validation errors
     */
    ValidationError checkValidator() {
        return ValidationUtils.validateEventDates(event.getStartDate(), event.getEndDate(), event.getParentProject());
    }

    public EventStepDefinition() {
        this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        this.dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    @When("the user creates an event called {string}, starting at {string}, ending on {string}, with a description {string}")
    public void the_user_creates_an_event_called_starting_at_ending_on_with_a_description(
            String name, String startDate, String endDate, String description) throws Exception {
        event = new Event();
        event.setName(name);
        event.setDescription(description);
        event.setStartDate(dateTimeFormatter.parse(startDate));
        event.setEndDate(dateTimeFormatter.parse(endDate));
        event.setParentProject(SchedulableStepDefinition.parentProject);
    }

    @Then("an event called {string} exists starting at {string}, ending at {string}, with a description {string}")
    public void an_event_called_exists_starting_at_ending_on_with_a_description(
        String name, String startDate, String endDate, String description
    ) {
        // 1. Check the constraint annotations
        var javaxErrors = checkJavaxConstraints();
        assertThat(javaxErrors, is(empty()));
        // 2. Check the validation utils errors
        var validationErrors = checkValidator();
        assertThat(validationErrors.getErrorMessages(), is(empty()));
    }

    @Then("creating the event should fail")
    public void adding_event_should_fail() {
        var javaxErrors = checkJavaxConstraints();
        var validationErrors = checkValidator();
        assertTrue(!javaxErrors.isEmpty() || !validationErrors.getErrorMessages().isEmpty());
    }

}
