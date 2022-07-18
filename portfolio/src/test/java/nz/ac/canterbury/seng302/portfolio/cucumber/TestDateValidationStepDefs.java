package nz.ac.canterbury.seng302.portfolio.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@CucumberContextConfiguration
public class TestDateValidationStepDefs extends RunCucumberTest {

    private Project project;
    private Sprint sprint;
    private List<Sprint> sprintList;

    @Before
    public void setUp(){
        sprintList = new ArrayList<>();
        project = new Project("testProject", "test description",
                "2022-01-01", "2022-10-01" );
        sprint = new Sprint(1, "testSprint", "test description",
                "2022-04-01", "2022-05-01", "#aaaaaa");
        sprint.setId(3);
    }

    @Given("The project has the following sprints with dates")
    public void the_project_has_the_following_sprints_with_dates(DataTable table) {
        List<List<String>> dates = table.asLists();

        int count = 0;
        for (List<String> sprintDates : dates) {
            Sprint sprint = new Sprint(1, "testName", "testDescription",
                    DateUtils.toDate(sprintDates.get(0)), DateUtils.toDate(sprintDates.get(1)),
                    "#aaaaaa");
            sprint.setSprintLabel("Sprint " + ++count);
            sprintList.add(sprint);
        }
    }

    @And("The projects creation date is {string}")
    public void theProjectsCreationDateIs(String creation) {
        project.setProjectCreationDate(DateUtils.toDate(creation));
    }

    @When("I set the project start date to {string}")
    public void i_set_the_project_start_date_to(String start) {
        project.setStartDateString(start);
    }

    @When("I set the project end date to {string}")
    public void i_set_the_project_end_date_to(String end) {
        project.setEndDateString(end);
    }
    @Then("Project {string} message should be displayed")
    public void project_message_should_be_displayed(String expected) {
        ValidationError error = ValidationUtils.validateProjectDates(project.getProjectStartDate(),
                project.getProjectEndDate(), project.getProjectCreationDate(), sprintList);
        String actual = error.getFirstError();
        assertEquals(expected, actual);
    }

    @When("I set a sprint's start date to {string}")
    public void iSetASprintSStartDateToStartDate(String start) { sprint.setStartDateString(start); }
    @And("I set a sprint's end date to {string}")
    public void iSetASprintSEndDateToEndDate(String end) { sprint.setEndDateString(end); }
    @Then("Sprint {string} message should be displayed")
    public void sprint_message_should_be_displayed(String expected) {
        sprintList.add(sprint);
        ValidationError error = ValidationUtils.validateSprintDates(3, sprint.getSprintStartDate(),
                sprint.getSprintEndDate(), project, sprintList);
        String actual = error.getFirstError();
        assertEquals(expected, actual);
    }
}
