package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.STUDENT;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.TEACHER;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SprintController.class)
@AutoConfigureMockMvc(addFilters = false)
class SprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAccountClientService userAccountClientService;

    @MockBean
    private SprintService sprintService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private SprintLabelService sprintLabelService;
    
    private Sprint sprint;

    /**
     * Sets up the test sprint
     */
    @BeforeEach
    public void beforeEach() {

        sprint = new Sprint();
        sprint.setId(0);
        sprint.setSprintLabel("Sprint 1");
        sprint.setSprintName("Sprint 1");
        sprint.setSprintDescription("The first.");
        sprint.setParentProjectId(0);
        sprint.setStartDateString("2022-02-05");
        sprint.setEndDateString("2022-03-24");
        sprint.setSprintColour("#abcdef");
        Project project = new Project("Project 2022", "desc", "2022-01-01", "2022-12-31");
        when(projectService.getProjectById(0)).thenReturn(project);
    }

    //add sprint tests
    @Test
    @WithMockPrincipal(TEACHER)
    void addValidSprint_get200Response() throws Exception {
        Mockito.doNothing().when(sprintService).saveSprint(any());
        this.mockMvc.perform(post("/add-sprint/0")
                        .param("name", "Sprint 1")
                        .param("description", "")
                        .param("startDate", "2022-09-09")
                        .param("endDate", "2022-10-09"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void addWithBlankName_thenShowError() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(post("/add-sprint/0")
                        .param("name", "   ")
                        .param("description", "desc")
                        .param("startDate", "2022-06-20")
                        .param("endDate", "2022-06-21"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name cannot be blank")));
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void addNameWithInvalidSymbols_thenShowError() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(post("/add-sprint/0")
                        .param("name", "A@!#@#!")
                        .param("description", "desc")
                        .param("startDate", "2022-06-20")
                        .param("endDate", "2022-06-21"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name can only have alphanumeric and . - _ characters")));
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void addWithShortName_thenShowError() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(post("/add-sprint/0")
                        .param("name", "s")
                        .param("description", "desc")
                        .param("startDate", "2022-06-20")
                        .param("endDate", "2022-06-21"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name must be between 2-32 characters")));
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void addWithLongName_thenShowError() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(post("/add-sprint/0")
                        .param("name", "This name is more than 32 characters long.")
                        .param("description", "desc")
                        .param("startDate", "2022-06-20")
                        .param("endDate", "2022-06-21"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name must be between 2-32 characters")));
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void addWithLongDescription_thenShowError() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(post("/add-sprint/0")
                        .param("name", "sprint 1")
                        .param("description", "I am trying to write a description for this sprint " +
                                "that should be longer than the maximum number of characters which is 200 and hopefully " +
                                "this is long enough to test the said limit. Apparently, that was not long enough.")
                        .param("startDate", "2022-06-20")
                        .param("endDate", "2022-06-21"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Description must not exceed 200 characters")));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankStartDateSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/add-sprint/0")
                        .param("name", "Sprint 1")
                        .param("description", "This is a sprint")
                        .param("startDate", "")
                        .param("endDate", "2022-09-14"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Start date cannot be blank"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankEndDateSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/add-sprint/0")
                        .param("name", "Sprint 1")
                        .param("description", "This is a sprint")
                        .param("startDate", "2022-09-09")
                        .param("endDate", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("End date cannot be blank"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addEarlySprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/add-sprint/0")
                        .param("name", "Sprint 1")
                        .param("description", "This is a sprint")
                        .param("startDate", "2021-12-09")
                        .param("endDate", "2022-01-05"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Sprint dates must be within project date range: 01/Jan/2022 - 31/Dec/2022"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addLateSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/add-sprint/0")
                        .param("name", "Sprint 1")
                        .param("description", "This is a sprint")
                        .param("startDate", "2022-12-09")
                        .param("endDate", "2023-01-05"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Sprint dates must be within project date range: 01/Jan/2022 - 31/Dec/2022"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addStartDateAfterEndDateSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/add-sprint/0")
                        .param("name", "Sprint 1")
                        .param("description", "This is a sprint")
                        .param("startDate", "2022-02-09")
                        .param("endDate", "2022-01-05"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Start date must always be before end date"));
    }

    //delete sprint tests
    @Test
    @WithMockPrincipal(TEACHER)
    void deleteSprintAsTeacher_get200Response() throws Exception {
        Mockito.doNothing().when(sprintService).deleteSprint(anyInt());
        mockMvc.perform(delete("/delete-sprint/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sprint deleted."));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void deleteSprintAsStudent_get401Response() throws Exception {
        mockMvc.perform(delete("/delete-sprint/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authorised."));
    }

    //edit sprint tests
    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void editSprintValidId() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(get("/edit-sprint/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void editSprintInvalidId_thenThrow404() throws Exception {
        when(sprintService.getSprintById(1)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));
        this.mockMvc.perform(get("/edit-sprint/1"))
                .andExpect(status().isNotFound());
    }


}
