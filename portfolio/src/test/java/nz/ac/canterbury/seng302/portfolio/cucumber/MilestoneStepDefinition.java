package nz.ac.canterbury.seng302.portfolio.cucumber;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.portfolio.model.Event;
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
        milestone.setMilestoneName(name);
        milestone.setMilestoneDate(DateUtils.toDate(date));
        milestone.setMilestoneDescription(description);
    }

    @Then("a milestone called {string} exists on {string}, with a description {string}")
    public void a_milestone_called_exists_on_with_a_description(
            String name, String date, String description) {
        var javaxErrors = checkJavaxConstraints();
        assertThat(javaxErrors, is(empty()));
    }

    @Then("creating the milestone should fail")
    public void creating_the_milestone_should_fail() {
        var javaxErrors = checkJavaxConstraints();
        assertThat(javaxErrors, is(not(empty())));
    }

}
