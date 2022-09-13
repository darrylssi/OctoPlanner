package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.*;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller test class for the display project details on the monthly calendar
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = MonthlyCalendarController.class)
@AutoConfigureMockMvc(addFilters = false)
class MonthlyCalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;                                    // initializing the MockMvc

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
        Project project = new Project("Project 2022", "This is first project", "2022-01-01", "2022-12-31");
        
        Mockito.when(projectService.getProjectById(0)).thenReturn(project);
        mockMvc.perform(get("/monthlyCalendar/0"))
            .andExpect(status().isOk());
    }


    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void getMonthlyCalendar_whenGivenPostMapping_returnUpdatedSprint() throws Exception {
        // initializing project object and mocking it
        Project project = new Project("Project 2022", "This is first project", "2022-01-01", "2022-12-31");
        Mockito.when(projectService.getProjectById(0)).thenReturn(project);

        // initializing sprint object and mocking it
        Sprint sprint = new Sprint(0, "Sprint 1", "This is sprint 1", "2022-01-02", "2022-01-10", "#3ea832");
        Mockito.when(sprintService.getSprintById(1)).thenReturn(sprint);

        mockMvc.perform(post("/monthlyCalendar/0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("sprintId", "1")
                            .param("sprintStartDate", "2022-01-05")
                            .param("sprintEndDate", "2022-01-15")
                )
            .andExpect(status().is3xxRedirection());
    }

}
