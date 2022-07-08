package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller test class for the display project details on the monthly calendar
 */
@WebMvcTest(controllers = MonthlyCalendarController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MonthlyCalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;                                    // initializing the MockMvc

    @MockBean
    private ProjectService projectService;                      // initializing the ProjectService
    @MockBean
    private SprintService sprintService;                        // initializing the SprintService
    @MockBean
    private UserAccountClientService userAccountClientService;  // initializing the UserAccountClientService
    @MockBean
    private DateUtils utils;                                    // initializing the DateUtils

    @Test
    @WithMockPrincipal(UserRole.STUDENT)
    void getMonthlyCalendar_whenGivenInvalidProjectId_returnNotFoundErrorMessage() throws Exception {
        Mockito.when(projectService.getProjectById(-1)).thenThrow(new Exception("Project not found"));
        try{
            mockMvc.perform(get("/monthlyCalendar/-1"));
        } catch (Exception e) {
            Assertions.assertEquals("Request processing failed; nested exception is java.lang.Exception: Project not found", e.getMessage());
        }
    }

    @Test
    @WithMockPrincipal(UserRole.STUDENT)
    void getMonthlyCalendar_whenGivenValidProjectId_returnProject() throws Exception {
        Project project = new Project("Project 2022", "This is first project", "01/JAN/2022", "31/DEC/2022");
        
        Mockito.when(projectService.getProjectById(0)).thenReturn(project);
        mockMvc.perform(get("/monthlyCalendar/0"))
            .andExpect(status().isOk());
    }


    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void getMonthlyCalendar_whenGivenPostMapping_returnUpdatedSprint() throws Exception {
        // initializing project object and mocking it
        Project project = new Project("Project 2022", "This is first project", "01/JAN/2022", "31/DEC/2022");
        Mockito.when(projectService.getProjectById(0)).thenReturn(project);

        // initializing sprint object and mocking it
        Sprint sprint = new Sprint(0, "Sprint 1", "This is sprint 1", "02/JAN/2022", "10/JAN/2022", "#3ea832");
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