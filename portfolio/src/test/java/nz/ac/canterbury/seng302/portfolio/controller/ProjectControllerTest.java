package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.STUDENT;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.TEACHER;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockPrincipal(TEACHER)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SprintRepository sprintRepository;
    @MockBean
    private ProjectRepository projectRepository;

    private static final int PROJECT_ID = 0;
    private static final List<Sprint> noSprints = List.of();
    @BeforeEach
    public void before() {
        Project project = new Project(
            "Test name", "Test Desc",
            DateUtils.toDate("2022-06-21"), DateUtils.toDate("2023-06-21")
        );
        // We don't have any tests to see if changing the project dates
        // causes sprints to become invalid, maybe we should though???
        when(sprintRepository.findByParentProjectId(PROJECT_ID))
            .thenReturn(noSprints);
        // Also, we should have a mock project
        when(projectRepository.findProjectById(PROJECT_ID))
            .thenReturn(project);
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void editProjectAsStudent_AccessDenied() throws Exception {
        this.mockMvc.perform(post("/project/"+PROJECT_ID+"/edit-project")
                .param("name", "TEST")
                .param("startDate",  "2021-03-04")
                .param("description", "TEST")
                .param("endDate", "2022-03-05"))
            .andExpect(status().isForbidden());
    }

    @Test
    void postProjectWithNoName_thenShowError() throws Exception {
        this.mockMvc.perform(post("/project/"+PROJECT_ID+"/edit-project")
                .param("name", "")
                .param("description", "desc")
                .param("startDate", "2021-06-20")
                .param("endDate", "2022-03-05"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Name cannot be blank")));
    }

    @Test
    void postProjectWitLongName_thenShowError() throws Exception {
        this.mockMvc.perform(post("/project/"+PROJECT_ID+"/edit-project")
                        .param("name", "blah".repeat(1000))
                        .param("description", "desc")
                        .param("startDate", "2021-06-20")
                        .param("endDate", "2022-03-05"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name must be between 2-32 characters")));
    }

    @Test
    void postProjectWithSymbolName_thenShowError() throws Exception {
        this.mockMvc.perform(post("/project/"+PROJECT_ID+"/edit-project")
                        .param("name", "A@!#@🥰#!")
                        .param("description", "desc")
                        .param("startDate", "2021-06-20")
                        .param("endDate", "2022-03-05"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Name can only have letters, numbers, spaces and punctuation except for commas")));
    }

    @Test
    void postProjectWithInvalidDesc_thenShowError() throws Exception {
        this.mockMvc.perform(post("/project/"+PROJECT_ID+"/edit-project")
                        .param("name", "")
                        .param("description", "Lorem ipsum dolor sit amet, consectetur adipisicing " +
                                "elit, sed do eiusmod cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat " +
                                "cupidatat non proident, sunt in culpa qui officia deserunt moll.")
                        .param("startDate", "2021-06-20")
                        .param("endDate", "2022-03-05"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Description must not exceed 200 characters")));
    }

    @Test
    void postProjectWithEarlyStart_thenShowError() throws Exception {
        this.mockMvc.perform(post("/project/"+PROJECT_ID+"/edit-project")
                .param("name", "Project")
                .param("description", "desc")
                .param("startDate", "2021-01-01")
                .param("endDate", "2022-03-05"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("Project cannot be set to start more than a year before" +
                                                       " it was created")));
    }

}
