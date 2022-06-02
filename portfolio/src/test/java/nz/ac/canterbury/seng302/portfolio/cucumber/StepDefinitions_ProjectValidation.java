package nz.ac.canterbury.seng302.portfolio.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.junit.Cucumber;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.ValidationService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class StepDefinitions_ProjectValidation {

    @Mock
    private SprintService sprintService;

    private Project project;
    private List<Sprint> sprintList;

    private final DateUtils utils = new DateUtils();

    @Mock
    private SprintRepository sprintRepository;

    @InjectMocks
    private ValidationService validationService = Mockito.spy(ValidationService.class);

    @Before
    public void setUp(){
        sprintList = new ArrayList<>();
        project = new Project("testName", "test description",
                "2022-01-01", "2022-10-01" );
        project.setProjectCreationDate(utils.toDate("2022-01-01"));
    }

    @Given("The project has the following sprints with dates")
    public void the_project_has_the_following_sprints_with_dates(DataTable table) {
        List<List<String>> dates = table.asLists();

        for (List<String> sprintDates : dates) {
            Sprint sprint = new Sprint(1, "testName", "testDescription",
                    utils.toDate(sprintDates.get(0)), utils.toDate(sprintDates.get(1)),
                    "#aaaaaa");
            sprintList.add(sprint);
        }
    }

    @When("I set the project start date to {string}")
    public void i_set_the_project_start_date_to(String start) {
       project.setStartDateString(start);
    }

    @When("I set the project end date to {string}")
    public void i_set_the_project_end_date_to(String end) {
        project.setEndDateString(end);
    }
    @Then("{string} message should be displayed")
    public void message_should_be_displayed(String expected) {
        when(sprintService.getAllSprints()).thenReturn(sprintList);

        String actual = validationService.validateProjectDates(project.getProjectStartDate(),
                project.getProjectEndDate(), project.getProjectCreationDate());
        assertEquals(expected, actual);
    }
}
