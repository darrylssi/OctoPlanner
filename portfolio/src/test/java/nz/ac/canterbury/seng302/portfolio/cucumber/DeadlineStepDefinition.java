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
import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;

/**
 * Class containing the step definitions for the account_credited Cucumber
 * feature
 */
@SpringBootTest
public class DeadlineStepDefinition extends RunCucumberTest {

    private static final int ID = 1;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat dateTimeFormatter;
    private Project parentProject;
    private Deadline deadline;

    /**
     * Validates deadline against its javax validation annotations
     * @return Constraint errors
     */
    Set<ConstraintViolation<Deadline>> checkJavaxConstraints() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator.validate(deadline);
    }

    /**
     * Checks against the validation utils validator
     * @return Validation errors
     */
    ValidationError checkValidator() {
        return ValidationUtils.validateDeadlineDate(deadline.getStartDate(), parentProject);
    }

    public DeadlineStepDefinition() {
        this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        this.dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    @Given("the parent project starts at {string} and ends on {string}")
    public void the_parent_project_has_an_id_of(String startDate_s, String endDate_s) throws Exception {
        var projStartDate = this.dateFormatter.parse(startDate_s);
        var projEndDate = this.dateFormatter.parse(endDate_s);

        parentProject = new Project("name", "desc", projStartDate, projEndDate);
    }

    @When("the user creates a deadline called {string} on {string}, with a description {string}")
    public void the_user_creates_a_deadline_called_on_with_a_description(
            String name, String date, String description) throws Exception {
        deadline = new Deadline();
        deadline.setName(name);
        deadline.setDescription(description);
        deadline.setStartDate(dateTimeFormatter.parse(date));
    }

    @Then("a deadline called {string} exists on {string}, with a description {string}")
    public void a_deadline_called_exists_on_with_a_description(
            String name, String date, String description
    ) {
        // 1. Check the constraint annotations
        var javaxErrors = checkJavaxConstraints();
        assertThat(javaxErrors, is(empty()));
        // 2. Check the validation utils errors
        var validationErrors = checkValidator();
        assertThat(validationErrors.getErrorMessages(), is(empty()));
    }

    @Then("creating the deadline should fail")
    public void adding_deadline_should_fail() {
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

