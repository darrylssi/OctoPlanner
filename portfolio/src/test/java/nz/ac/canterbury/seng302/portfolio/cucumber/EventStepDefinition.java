package nz.ac.canterbury.seng302.portfolio.cucumber;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNot.not;

import java.text.SimpleDateFormat;
import java.util.Set;

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
 * Class containing the step definitions for the account_credited Cucumber
 * feature
 */
@SpringBootTest
public class EventStepDefinition extends RunCucumberTest {

    private static final int ID = 1;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat dateTimeFormatter;
    private Project parentProject;
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
        return ValidationUtils.validateEventDates(event.getStartDate(), event.getEndDate(), parentProject);
    }

    public EventStepDefinition() {
        this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        this.dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    @Given("the parent project starts at {string} and ends on {string}")
    public void the_parent_project_has_an_id_of(String startDate_s, String endDate_s) throws Exception {
        var projStartDate = this.dateFormatter.parse(startDate_s);
        var projEndDate = this.dateFormatter.parse(endDate_s);

        parentProject = new Project("name", "desc", projStartDate, projEndDate);
    }

    @When("the user creates an event called {string}, starting at {string}, ending on {string}, with a description {string}")
    public void the_user_creates_an_event_called_starting_at_ending_on_with_a_description(
            String name, String startDate, String endDate, String description) throws Exception {
        event = new Event();
        event.setName(name);
        event.setDescription(description);
        event.setStartDate(dateTimeFormatter.parse(startDate));
        event.setEndDate(dateTimeFormatter.parse(endDate));
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
        assertThat(javaxErrors, is(not(empty())));
        // Note: Unfortunately I don't know how to check if EITHER
        // javaxErrors or validationErrors is(not(empty())), so the
        // feature file must have constaint errors to see any
        // validation errors
        var validationErrors = checkValidator();
        assertThat(validationErrors.getErrorMessages(), is(not(empty())));
    }

}
