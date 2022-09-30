package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.*;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.GlobalVars;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller test class for the display project details on the monthly calendar
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = MonthlyCalendarController.class)
@AutoConfigureMockMvc(addFilters = false)
class MonthlyCalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;                            // initializing the MockMvc
    @MockBean
    ProjectService projectService;                      // initializing the ProjectService
    @MockBean
    SprintService sprintService;                        // initializing the SprintService
    @MockBean
    UserAccountClientService userAccountClientService;  // initializing the UserAccountClientService
    @MockBean
    DateUtils utils;                                    // initializing the DateUtils
    @MockBean
    DeadlineService deadlineService;                    // initializing the DeadlineService
    @MockBean
    EventService eventService;                          // initializing the EventService
    @MockBean
    MilestoneService milestoneService;                  // initializing the MilestoneService
    @MockBean
    DetailsController detailsController;                // initializing the DetailsController

    private static final int PROJECT_ID = 0;
    private static Deadline deadline = new Deadline("deadline1", "deaddesc", DateUtils.toDateTime("2022-02-01 17:00"));
    private static Milestone milestone = new Milestone("milestone1", "deaddesc", DateUtils.toDate("2022-02-02"));
    private static Event event = new Event("event1", "eventdesc", DateUtils.toDateTime("2022-02-02 09:00"), DateUtils.toDateTime("2022-02-20 17:00"));
    private static Sprint sprint1 = new Sprint(PROJECT_ID, "Sprint 1", "This is sprint 1", "2022-01-02", "2022-01-10", "#3ea832");
    private static Sprint sprint2 = new Sprint(PROJECT_ID, "Sprint 2", "This is sprint 2", "2022-01-11", "2022-01-22", "#123456");
    private static Project project = new Project("Project 2022", "This is the first project", "2022-01-01", "2022-12-31");

    @BeforeAll
    static void setUp() {
        deadline.setId(1);
        deadline.setParentProject(project);
        milestone.setId(1);
        milestone.setParentProject(project);
        event.setId(1);
        event.setParentProject(project);
        sprint1.setId(1);
        sprint2.setId(2);
    }

    @Test
    @WithMockPrincipal(UserRole.STUDENT)
    void getMonthlyCalendar_whenGivenInvalidProjectId_returnNotFoundErrorMessage() throws Exception{
        Mockito.when(projectService.getProjectById(-1)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        mockMvc.perform(get("/monthlyCalendar/-1"))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockPrincipal(UserRole.STUDENT)
    void getMonthlyCalendar_whenGivenValidProjectId_returnProject() throws Exception {
        Mockito.when(projectService.getProjectById(PROJECT_ID)).thenReturn(project);
        mockMvc.perform(get("/monthlyCalendar/" + PROJECT_ID))
            .andExpect(status().isOk());
    }


    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void whenUpdateSprintOnCalendar_thenReturnUpdatedSprint() throws Exception {
        // mocking project object
        Mockito.when(projectService.getProjectById(PROJECT_ID)).thenReturn(project);

        // mocking sprint object
        Mockito.when(sprintService.getSprintById(1)).thenReturn(sprint1);

        mockMvc.perform(post("/monthlyCalendar/" + PROJECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("sprintId", "1")
                            .param("sprintStartDate", "2022-01-05")
                            .param("sprintEndDate", "2022-01-15")
                )
            .andExpect(status().is3xxRedirection());
    }


    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void whenUpdateNonexistentSprintOnCalendar_thenGetNotFound() throws Exception {
        Mockito.when(projectService.getProjectById(PROJECT_ID)).thenReturn(project);
        Mockito.when(sprintService.getSprintById(1)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found."));

        mockMvc.perform(post("/monthlyCalendar/" + PROJECT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("sprintId", "1")
                        .param("sprintStartDate", "2022-01-05")
                        .param("sprintEndDate", "2022-01-15")
                )
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void whenGetMonthlyCalendar_thenHasAllSchedulablesInProjectInCorrectFormat() throws Exception {
        // get services to return list of schedulables
        Mockito.when(projectService.getProjectById(PROJECT_ID)).thenReturn(project);
        Mockito.when(detailsController.getAllSchedulablesInProject(PROJECT_ID))
                .thenReturn(List.of(deadline, milestone, event));


        // create expected lists
        String expectedNames = String.join(", ", List.of("deadline1", "milestone1", "event1"));
        String expectedTypes = String.join(", ", List.of(GlobalVars.DEADLINE_TYPE, GlobalVars.MILESTONE_TYPE, GlobalVars.EVENT_TYPE));
        String expectedStarts = String.join(", ", List.of("2022-02-01 17:00", "2022-02-02 00:00", "2022-02-02 09:00"));
        String expectedEnds = String.join(", ", List.of("2022-02-01 17:00", "2022-02-02 00:00", "2022-02-20 17:00"));

        mockMvc.perform(get("/monthlyCalendar/" + PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute("schedulableNames", expectedNames))
                .andExpect(model().attribute("schedulableTypes", expectedTypes))
                .andExpect(model().attribute("schedulableStartDates", expectedStarts))
                .andExpect(model().attribute("schedulableEndDates", expectedEnds));
    }

    // this test is a bit odd because of how the sprint details are sent to the page
    // for some reason the start dates are in a different format to the end dates
    // and the end dates have one day added to them (presumably because end dates are exclusive in FullCalendar)
    // that addition of one day to the date should probably be done in the JS file so that we only 'lie' to the calendar

    // update: this test is seemingly nondeterministic, at least when run locally
    // I have no idea what is going on with these dates
    // expected:<Wed Jan 05,Tue Jan 11> but was:<Sun Jan 02,Tue Jan 11>
    // expected:<2022-01-15,2022-01-23> but was:<2022-01-11,2022-01-23>
    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void whenGetMonthlyCalendar_thenHasAllSprintsInProjectInCorrectFormat() throws Exception {
        Mockito.when(sprintService.getSprintsInProject(PROJECT_ID)).thenReturn(List.of(sprint1, sprint2));
        Mockito.when(projectService.getProjectById(PROJECT_ID)).thenReturn(project);

        String expectedIds = String.join(",", List.of("1", "2"));
        String expectedNames = String.join(",", List.of("Sprint 1", "Sprint 2"));
        String expectedStarts = String.join(",", List.of("2022-01-02", "2022-01-11"));
        String expectedEnds = String.join(",", List.of("2022-01-11", "2022-01-23"));
        String expectedColours = String.join(",", List.of("#3ea832", "#123456"));

        mockMvc.perform(get("/monthlyCalendar/" + PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute("sprintIds", expectedIds))
                .andExpect(model().attribute("sprintNames", expectedNames))
                .andExpect(model().attribute("sprintStartDates", expectedStarts))
                .andExpect(model().attribute("sprintEndDates", expectedEnds))
                .andExpect(model().attribute("sprintColours", expectedColours));
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void whenGetAllSchedulablesOfValidType_thenOk() throws Exception {
        Mockito.when(deadlineService.getDeadlinesInProject(PROJECT_ID)).thenReturn(List.of(deadline));
        Mockito.when(milestoneService.getMilestonesInProject(PROJECT_ID)).thenReturn(List.of(milestone));
        Mockito.when(eventService.getEventByParentProjectId(PROJECT_ID)).thenReturn(List.of(event));
        Mockito.when(projectService.getProjectById(PROJECT_ID)).thenReturn(project);

        mockMvc.perform(get("/project/" + PROJECT_ID + "/schedulables/milestone"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/project/" + PROJECT_ID + "/schedulables/deadline"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/project/" + PROJECT_ID + "/schedulables/event"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void whenGetAllSchedulablesOfInvalidType_then400Error() throws Exception {
        Mockito.when(deadlineService.getDeadlinesInProject(PROJECT_ID)).thenReturn(List.of(deadline));
        Mockito.when(milestoneService.getMilestonesInProject(PROJECT_ID)).thenReturn(List.of(milestone));
        Mockito.when(eventService.getEventByParentProjectId(PROJECT_ID)).thenReturn(List.of(event));
        Mockito.when(projectService.getProjectById(PROJECT_ID)).thenReturn(project);

        mockMvc.perform(get("/project/" + PROJECT_ID + "/schedulables/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("invalid is not a type of schedulable!"));
    }
}
