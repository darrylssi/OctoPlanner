package nz.ac.canterbury.seng302.portfolio.cucumber;

import io.cucumber.java.en.Given;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * This contains the step definitions that are shared by Milestones, Deadlines
 * and Events acceptance tests.
 */
@SpringBootTest
public class SchedulableStepDefinition {

    public static Project parentProject;

    @Given("the parent project starts at {string} and ends on {string}")
    public void the_parent_project_starts_at_and_ends_on(String startDate, String endDate) {
        Date projStartDate = DateUtils.toDate(startDate);
        Date projEndDate = DateUtils.toDate(endDate);

        parentProject = new Project("name", "desc", projStartDate, projEndDate);
    }

}
