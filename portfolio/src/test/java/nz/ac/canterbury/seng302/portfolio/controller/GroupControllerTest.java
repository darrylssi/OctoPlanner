package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.controller.forms.GroupForm;
import nz.ac.canterbury.seng302.portfolio.model.Group;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.GroupClientService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.STUDENT;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.TEACHER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for post and delete requests for groups handled by the group controller
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = GroupController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupClientService groupClientService;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserAccountClientService userAccountClientService;

    private GroupForm groupForm;                                // Initialises the group form object
    private Group group;                                        // Initialises the group object

    @BeforeEach
    void setup() {
        // Creates and sets the details to group form
        groupForm = new GroupForm();
        groupForm.setShortName("Test Group");
        groupForm.setLongName("Test Project Group 2022");

        // Creates and sets the details to the new project
        Project parentProject = new Project("Project 2022", "Test Parent Project", "2022-01-01", "2022-12-31");

        // Creates and sets the details to the group object
        group = new Group();
        group.setId(1);
        group.setParentProject(parentProject);
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addValidGroupAsTeacher_get200Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).
                thenReturn(group.getParentProject());
        mockMvc.perform(post("/project/0/add-group")
                        .param("shortName", "Test Group 1")
                        .param("longName", "Test Project Group"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankShortNameGroupAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).
                thenReturn(group.getParentProject());
        String resultString = mockMvc.perform(post("/project/0/add-group")
                        .param("shortName", "")
                        .param("longName", "Test Project Group 2022"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(resultString.contains("Short name cannot be blank"));
        Assertions.assertTrue(resultString.contains("Short name must be between 2-32 characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankLongNameGroupAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).
                thenReturn(group.getParentProject());
        String resultString = mockMvc.perform(post("/project/0/add-group")
                        .param("shortName", "Test Group")
                        .param("longName", ""))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(resultString.contains("Long name cannot be blank"));
        Assertions.assertTrue(resultString.contains("Long name must be between 2-128 characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addInvalidShortNameGroupAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).
                thenReturn(group.getParentProject());
        mockMvc.perform(post("/project/0/add-group")
                        .param("shortName", "Test Group 🏋")
                        .param("longName", "Test Project Group 2022"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name can only have letters, numbers, punctuations except commas, and spaces."));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addInvalidLongNameGroupAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).
                thenReturn(group.getParentProject());
        mockMvc.perform(post("/project/0/add-group")
                        .param("shortName", "Test Group")
                        .param("longName", "Test Project Group 2022 🏋"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name can only have letters, numbers, punctuations except commas, and spaces."));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void addGroupAsStudent_forbidden() throws Exception {
        this.mockMvc.perform(post("/project/0/add-group"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

}
