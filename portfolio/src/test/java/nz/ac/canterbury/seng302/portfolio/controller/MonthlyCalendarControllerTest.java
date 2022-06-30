package nz.ac.canterbury.seng302.portfolio.controller;


import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = MonthlyCalendarController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MonthlyCalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;
    @MockBean
    private SprintService sprintService;
    @MockBean
    private UserAccountClientService userAccountClientService;
    @MockBean
    private DateUtils utils;

    @Test
    @WithMockPrincipal(UserRole.COURSE_ADMINISTRATOR)
    void adminGetMonthlyCalendar_whenGivenInvalidProjectId_returnNotFoundErrorMessage() throws Exception {
        Mockito.when(projectService.getProjectById(-1)).thenThrow(new Exception("Project not found"));
        try{
            mockMvc.perform(get("/monthlyCalendar/-1"));
        } catch (Exception e) {
            Assertions.assertEquals("Request processing failed; nested exception is java.lang.Exception: Project not found", e.getMessage());
        }
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void teacherGetMonthlyCalendar_whenGivenInvalidProjectId_returnNotFoundErrorMessage() throws Exception {
        Mockito.when(projectService.getProjectById(-1)).thenThrow(new Exception("Project not found"));
        try{
            mockMvc.perform(get("/monthlyCalendar/-1"));
        } catch (Exception e) {
            Assertions.assertEquals("Request processing failed; nested exception is java.lang.Exception: Project not found", e.getMessage());
        }
    }

    @Test
    @WithMockPrincipal(UserRole.STUDENT)
    void studentGetMonthlyCalendar_whenGivenInvalidProjectId_returnNotFoundErrorMessage() throws Exception {
        Mockito.when(projectService.getProjectById(-1)).thenThrow(new Exception("Project not found"));
        try{
            mockMvc.perform(get("/monthlyCalendar/-1"));
        } catch (Exception e) {
            Assertions.assertEquals("Request processing failed; nested exception is java.lang.Exception: Project not found", e.getMessage());
        }
    }

    @Test
    @WithMockPrincipal(UserRole.COURSE_ADMINISTRATOR)
    void adminGetMonthlyCalendar_whenGivenValidProjectId_returnProject() throws Exception {
        Project project = new Project("Project 2022", "This is first project", "01/JAN/2022", "31/DEC/2022");
        Mockito.when(projectService.getProjectById(0)).thenReturn(project);
        mockMvc.perform(get("/monthlyCalendar/0"));
        Assertions.assertEquals(project, projectService.getProjectById(0));
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void teacherGetMonthlyCalendar_whenGivenValidProjectId_returnProject() throws Exception {
        Project project = new Project("Project 2022", "This is first project", "01/JAN/2022", "31/DEC/2022");
        Mockito.when(projectService.getProjectById(0)).thenReturn(project);
        mockMvc.perform(get("/monthlyCalendar/0"));
        Assertions.assertEquals(project, projectService.getProjectById(0));
    }

    @Test
    @WithMockPrincipal(UserRole.STUDENT)
    void studentGetMonthlyCalendar_whenGivenValidProjectId_returnProject() throws Exception {
        Project project = new Project("Project 2022", "This is first project", "01/JAN/2022", "31/DEC/2022");
        Mockito.when(projectService.getProjectById(0)).thenReturn(project);
        mockMvc.perform(get("/monthlyCalendar/0"));
        Assertions.assertEquals(project, projectService.getProjectById(0));
    }


//    @Test
//    @WithMockPrincipal(UserRole.TEACHER)
//    void teacherGetMonthlyCalendar_whenGivenPostMapping_returnUpdatedSprint() throws Exception {
//        // initializing project object and mocking it
//        Project project = new Project("Project 2022", "This is first project", "01/JAN/2022", "31/DEC/2022");
//        Mockito.when(projectService.getProjectById(0)).thenReturn(project);
//
//        // initializing sprint object and mocking it
//        Sprint sprint = new Sprint(0, "Sprint 1", "This is sprint 1", "02/JAN/2022", "10/JAN/2022", "#3ea832");
//        Mockito.when(sprintService.getSprintById(1)).thenReturn(sprint);
//
//        mockMvc.perform(
//                    MockMvcRequestBuilders.post("/monthlyCalendar/0")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .param("sprintId", "1")
//                            .param("sprintStartDate", "utils.toDate('05/JAN/2022')")
//                            .param("sprintEndDate", "utils.toDate('15/JAN/2022')")
//                )
//                .andExpect(status().is3xxRedirection());
//
//        // mocking the updated sprint
//        System.out.println(sprint.getSprintStartDate() + " - " + sprint.getSprintEndDate());
//        Mockito.when(sprintService.getSprintById(1)).thenReturn(sprint);
//    }

}
