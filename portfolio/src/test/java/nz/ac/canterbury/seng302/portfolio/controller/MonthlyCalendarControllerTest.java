package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Schedulable;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.*;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.GlobalVars;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    EventService eventService;
    @MockBean
    MilestoneService milestoneService;

    private static Deadline deadline = new Deadline("deadline1", "deaddesc", DateUtils.toDate("2022-02-01"));
    private static Milestone milestone = new Milestone("milestone1", "deaddesc", DateUtils.toDate("2022-02-02"));
    private static Event event = new Event("event1", "eventdesc", DateUtils.toDate("2022-02-02"), DateUtils.toDate("2022-02-20"));
    private static Project project = new Project("Project 2022", "This is the first project", "2022-01-01", "2022-12-31");
    private final int PROJECT_ID = 0;

    @BeforeAll
    static void setUp() {
        deadline.setId(1);
        deadline.setParentProject(project);
        milestone.setId(1);
        milestone.setParentProject(project);
        event.setId(1);
        event.setParentProject(project);
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
    void getMonthlyCalendar_whenGivenPostMapping_returnUpdatedSprint() throws Exception {
        // mocking project object
        Mockito.when(projectService.getProjectById(PROJECT_ID)).thenReturn(project);

        // initializing sprint object and mocking it
        Sprint sprint = new Sprint(PROJECT_ID, "Sprint 1", "This is sprint 1", "2022-01-02", "2022-01-10", "#3ea832");
        Mockito.when(sprintService.getSprintById(1)).thenReturn(sprint);

        mockMvc.perform(post("/monthlyCalendar/0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("sprintId", "1")
                            .param("sprintStartDate", "2022-01-05")
                            .param("sprintEndDate", "2022-01-15")
                )
            .andExpect(status().is3xxRedirection());
    }


    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void getMonthlyCalendar_hasAllSchedulablesInProjectInCorrectFormat() throws Exception {
        // get services to return lists
        Mockito.when(deadlineService.getDeadlinesInProject(PROJECT_ID)).thenReturn(List.of(deadline));
        Mockito.when(milestoneService.getMilestonesInProject(PROJECT_ID)).thenReturn(List.of(milestone));
        Mockito.when(eventService.getEventByParentProjectId(PROJECT_ID)).thenReturn(List.of(event));
        Mockito.when(projectService.getProjectById(PROJECT_ID)).thenReturn(project);

        // create expected lists
        String expectedNames = String.join(", ", List.of("deadline1", "milestone1", "event1"));
        String expectedTypes = String.join(", ", List.of(GlobalVars.DEADLINE_TYPE, GlobalVars.MILESTONE_TYPE, GlobalVars.EVENT_TYPE));
        String expectedStarts = String.join(", ", List.of("2022-02-01", "2022-02-02", "2022-02-02"));
        String expectedEnds = String.join(", ", List.of("2022-02-01", "2022-02-02", "2022-02-20"));

        mockMvc.perform(get("/monthlyCalendar/" + PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute("schedulableNames", expectedNames))
                .andExpect(model().attribute("schedulableTypes", expectedTypes))
                .andExpect(model().attribute("schedulableStartDates", expectedStarts))
                .andExpect(model().attribute("schedulableEndDates", expectedEnds));
    }
}
