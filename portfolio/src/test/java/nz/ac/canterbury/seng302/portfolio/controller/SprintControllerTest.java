package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
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
import java.util.List;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.STUDENT;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.TEACHER;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for post and delete requests for sprint handled by the sprint controller
 */
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

    private static final int PROJECT_ID = 0;
    private static final int SPRINT_ID = 0;
    private Sprint sprint;

    /**
     * Sets up the test sprint
     */
    @BeforeEach
    public void beforeEach() {
        // Creates and sets the parent project details
        Project project = new Project("Project 2022", "desc", DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-12-31"));
        project.setId(PROJECT_ID);

        // Creates and sets the sprint object details
        sprint = new Sprint(PROJECT_ID, "Sprint 1", "The first sprint", DateUtils.toDate("2022-02-05"),
                DateUtils.toDate("2022-03-24"),"#abcdef");
        sprint.setId(SPRINT_ID);

        // Gets the sprint list at the given project id
        List<Sprint> sprintList = sprintService.getSprintsInProject(PROJECT_ID);

        // Mocking sprint list at project id
        when(sprintService.getSprintsInProject(anyInt()))
                .thenReturn(sprintList);
        // Also, we should have a mock project
        when(projectService.getProjectById(anyInt()))
                .thenReturn(project);
        // Also, we should have a mock sprint
        when(sprintService.getSprintById(anyInt()))
                .thenReturn(sprint);
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
                        .param("name", "<Sprint, name>")
                        .param("description", "desc")
                        .param("startDate", "2022-06-20")
                        .param("endDate", "2022-06-21"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name can only have letters, numbers, spaces and punctuation except for commas")));
    }

    @Test
    @WithMockPrincipal(UserRole.TEACHER)
    void addDescriptionWithInvalidSymbols_thenShowError() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(post("/add-sprint/0")
                        .param("name", "sprint name")
                        .param("description", "\uD83D\uDE03\uD83D\uDE03")
                        .param("startDate", "2022-06-20")
                        .param("endDate", "2022-06-21"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Description can only have letters, numbers, spaces and punctuation")));
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

    // Edit sprint tests
    @Test
    @WithMockPrincipal(TEACHER)
    void editValidSprint_get200Response() throws Exception {
        mockMvc.perform(post("/project/0/edit-sprint/0")
                        .param("name", "TEST")
                        .param("description", "TEST")
                        .param("startDate",  "2022-02-01")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editBlankNameSprintAsTeacher_get400Response() throws Exception {
        String resultString = mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "")
                        .param("startDate",  "2022-02-01")
                        .param("description", "TEST")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(resultString.contains("Name cannot be blank"));
        Assertions.assertTrue(resultString.contains("Name must be between 2-32 characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editInvalidNameSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "TEST 🏋️")
                        .param("startDate",  "2022-02-01")
                        .param("description", "TEST")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name can only have letters, numbers, spaces and punctuation except for commas"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editShortNameSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "T")
                        .param("startDate",  "2022-02-01")
                        .param("description", "TEST")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name must be between 2-32 characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editLongNameSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "blah".repeat(1000))
                        .param("startDate",  "2022-02-01")
                        .param("description", "TEST")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name must be between 2-32 characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editLongDescriptionSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "TEST")
                        .param("startDate",  "2022-02-01")
                        .param("description", "Lorem ipsum dolor sit amet, consectetur adipisicing " +
                                "elit, sed do eiusmod cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat " +
                                "cupidatat non proident, sunt in culpa qui officia deserunt moll.")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Description must not exceed 200 characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editBlankStartDateSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "TEST")
                        .param("startDate",  "")
                        .param("description", "TEST")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Start date cannot be blank"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editBlankEndDateSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "TEST")
                        .param("startDate",  "2022-02-01")
                        .param("description", "TEST")
                        .param("endDate", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("End date cannot be blank"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editEarlyStartDateSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "TEST")
                        .param("startDate",  "2021-02-01")
                        .param("description", "TEST")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Sprint dates must be within project date range: 01/Jan/2022 - 31/Dec/2022"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editLateEndDateSprintAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "TEST")
                        .param("startDate",  "2022-02-01")
                        .param("description", "TEST")
                        .param("endDate", "2023-03-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Sprint dates must be within project date range: 01/Jan/2022 - 31/Dec/2022"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editSprintStartAfterEndAsTeacher_get400Response() throws Exception {
        mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "TEST")
                        .param("startDate",  "2022-09-09")
                        .param("description", "TEST")
                        .param("endDate", "2022-09-03"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Start date must always be before end date"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void editSprintAsStudent_AccessDenied() throws Exception {
        this.mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "TEST")
                        .param("startDate",  "2022-02-01")
                        .param("description", "TEST")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editSprintMissingId_throw404() throws Exception {
        when(sprintService.getSprintById(anyInt()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Sprint not found"));
        this.mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/1")
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "TEST")
                        .param("startDate",  "2022-02-01")
                        .param("description", "TEST")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(containsString("Sprint not found")));
    }
}
