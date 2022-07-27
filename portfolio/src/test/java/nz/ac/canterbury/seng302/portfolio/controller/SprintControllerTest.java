package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.builder.MockUserResponseBuilder;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private SprintService sprintService;
    
    private Sprint sprint;

    /**
     * Authentication is done by getting the user ID from the AuthState,
     * then checking it against the gRPC. This mocks the gRPC check.
     * @param testInfo Gives us info about the test that's running next.
     */
    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        // Might as well reuse that WithMockPrincipal annotation I made
        UserResponse user = MockUserResponseBuilder.buildUserResponseFromMockPrincipalAnnotatedTest(testInfo);
        int annotatedUserId = user.getId();

        // When a controller checks the user's role, return what they expect.
        when(mockedGRPCUserAccount.getUserAccountById(annotatedUserId))
                .thenReturn(user);


        sprint = new Sprint();
        sprint.setId(0);
        sprint.setSprintLabel("Sprint 1");
        sprint.setSprintName("Sprint 1");
        sprint.setSprintDescription("The first.");
        sprint.setParentProjectId(0);
        sprint.setStartDateString("2022-02-05");
        sprint.setEndDateString("2022-03-24");
        sprint.setSprintColour("#abcdef");
    }

    @Test
    void getSprintValidId() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        assertThat(sprintService.getSprintById(1)).isEqualTo(sprint);
        this.mockMvc.perform(get("/edit-sprint/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getSprintInvalidId_thenThrow404() throws Exception {
        this.mockMvc.perform(get("/edit-sprint/1"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(containsString("Sprint not found")));
    }

    @Test
    void addValidSprint_thenRedirect() throws Exception {
        when(sprintService.getSprintsInProject(0)).thenReturn(Arrays.asList(sprint));
        this.mockMvc.perform(post("/add-sprint/0")
                        .param("sprintName", "sprint 1")
                        .param("sprintDescription", "desc")
                        .param("sprintStartDate", "2022-06-20")
                        .param("sprintEndDate", "2022-06-21"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void editWithValidDetails_thenRedirect() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(post("/edit-sprint/1")
                        .param("sprintName", "new Name")
                        .param("projectId", "0")
                        .param("sprintDescription", "new Description")
                        .param("sprintStartDate", "2022-06-19")
                        .param("sprintEndDate", "2022-06-22"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void editWithBlankName_thenShowError() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(post("/edit-sprint/1")
                        .param("sprintName", "   ")
                        .param("projectId", "0")
                        .param("sprintDescription", "desc")
                        .param("sprintStartDate", "2022-06-20")
                        .param("sprintEndDate", "2022-06-21"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sprint name is required")));
    }

    @Test
    void editWithShortName_thenShowError() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(post("/edit-sprint/1")
                        .param("sprintName", "s")
                        .param("projectId", "0")
                        .param("sprintDescription", "desc")
                        .param("sprintStartDate", "2022-06-20")
                        .param("sprintEndDate", "2022-06-21"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("The sprint name must be between 2 and 32 characters")));
    }

    @Test
    void editWithLongName_thenShowError() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(post("/edit-sprint/1")
                        .param("sprintName", "This name is more than 32 characters long.")
                        .param("projectId", "0")
                        .param("sprintDescription", "desc")
                        .param("sprintStartDate", "2022-06-20")
                        .param("sprintEndDate", "2022-06-21"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("The sprint name must be between 2 and 32 characters")));
    }

    @Test
    void editWithLongDescription_thenShowError() throws Exception {
        when(sprintService.getSprintById(1)).thenReturn(sprint);
        this.mockMvc.perform(post("/edit-sprint/1")
                        .param("projectId", "0")
                        .param("sprintName", "sprint 1")
                        .param("sprintDescription", "I am trying to write a description for this sprint " +
                                "that should be longer than the maximum number of characters which is 200 and hopefully " +
                                "this is long enough to test the said limit. Apparently, that was not long enough.")
                        .param("sprintStartDate", "2022-06-20")
                        .param("sprintEndDate", "2022-06-21"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("The sprint description must not exceed 200 characters")));
    }

}
