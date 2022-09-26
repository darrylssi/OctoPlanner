package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.builder.MockUserResponseBuilder;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.STUDENT;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.TEACHER;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockPrincipal(UserRole.TEACHER)
class SprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAccountClientService mockedGRPCUserAccount;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private SprintService sprintService;

    private static final int PROJECT_ID = 0;
    private static final int SPRINT_ID = 0;
    private Sprint sprint;

    /**
     * Authentication is done by getting the user ID from the AuthState,
     * then checking it against the gRPC. This mocks the gRPC check.
     * @param testInfo Gives us info about the test that's running next.
     */
    @BeforeEach
    public void before(TestInfo testInfo) {
        // Might as well reuse that WithMockPrincipal annotation I made
        UserResponse user = MockUserResponseBuilder.buildUserResponseFromMockPrincipalAnnotatedTest(testInfo);
        int annotatedUserId = user.getId();

        // When a controller checks the user's role, return what they expect.
        when(mockedGRPCUserAccount.getUserAccountById(annotatedUserId))
                .thenReturn(user);

        // Creates and sets the project object details
        Project project = new Project(
                "Test name", "Test Desc",
                DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-12-31")
        );

        // Creates and sets the sprint object details
        sprint = new Sprint(PROJECT_ID, "Sprint 1", "The first sprint",
                DateUtils.toDate("2022-02-05"), DateUtils.toDate("2022-03-24"), "#abcdef");
        sprint.setId(SPRINT_ID);

        // Gets the sprint list at the given project id
        List<Sprint> sprintList = sprintService.getSprintsInProject(PROJECT_ID);

        // Mocking sprint list at project id
        when(sprintService.getSprintsInProject(PROJECT_ID))
                .thenReturn(sprintList);
        // Also, we should have a mock project
        when(projectService.getProjectById(PROJECT_ID))
                .thenReturn(project);
        // Also, we should have a mock sprint
        when(sprintService.getSprintById(SPRINT_ID))
                .thenReturn(sprint);
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editValidSprint_get200Response() throws Exception {
        when(sprintService.getSprintById(anyInt()))
                .thenReturn(sprint);
        this.mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "TEST")
                        .param("startDate",  "2022-02-01")
                        .param("description", "TEST")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editBlankNameSprintAsTeacher_get400Response() throws Exception {
        when(sprintService.getSprintById(anyInt())).
                thenReturn(sprint);
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
        when(sprintService.getSprintById(anyInt())).
                thenReturn(sprint);
        mockMvc.perform(post("/project/" + PROJECT_ID + "/edit-sprint/" + SPRINT_ID)
                        .param("projectId", String.valueOf(PROJECT_ID))
                        .param("name", "TEST 🏋️")
                        .param("startDate",  "2022-02-01")
                        .param("description", "TEST")
                        .param("endDate", "2022-03-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name can only have letters, numbers, punctuations except commas, and spaces."));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editShortNameSprintAsTeacher_get400Response() throws Exception {
        when(sprintService.getSprintById(anyInt())).
                thenReturn(sprint);
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
        when(sprintService.getSprintById(anyInt())).
                thenReturn(sprint);
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
        when(sprintService.getSprintById(anyInt())).
                thenReturn(sprint);
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
        when(sprintService.getSprintById(anyInt())).
                thenReturn(sprint);
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
        when(sprintService.getSprintById(anyInt())).
                thenReturn(sprint);
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
        when(sprintService.getSprintById(anyInt())).
                thenReturn(sprint);
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
        when(sprintService.getSprintById(anyInt())).
                thenReturn(sprint);
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
        when(sprintService.getSprintById(anyInt())).
                thenReturn(sprint);
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
