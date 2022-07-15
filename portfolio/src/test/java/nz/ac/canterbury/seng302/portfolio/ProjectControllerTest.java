package nz.ac.canterbury.seng302.portfolio;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockPrincipal(TEACHER)
public class ProjectControllerTest {

    final Logger logger = LoggerFactory.getLogger(ProjectControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getProjectMissingId_throw404() throws Exception {
        this.mockMvc.perform(get("/edit-project/-1"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(containsString("Project not found")));
    }

    @Test
    public void getProjectValidId() throws Exception {
        this.mockMvc.perform(get("/edit-project/0"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockPrincipal(STUDENT)
    public void getProjectEditPage_AccessDenied() throws Exception {
        this.mockMvc.perform(get("/edit-project/0"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockPrincipal(STUDENT)
    public void editProjectAsStudent_AccessDenied() throws Exception {
        this.mockMvc.perform(post("/edit-project/0")
                .param("projectName", "TEST")
                .param("projectStartDate",  "2021-03-04")
                .param("projectDescription", "TEST")
                .param("projectEndDate", "2022-03-05"))
            .andExpect(status().isForbidden());
    }

    // TODO: This test FAILS, and honestly should be mocked.
    // @Test
    // public void postValidProject_thenRedirect() throws Exception {
    //     this.mockMvc.perform(post("/edit-project/0")
    //             .param("projectName", "name")
    //             .param("projectDescription", "desc")
    //             .param("projectStartDate", "2021-05-15")
    //             .param("projectEndDate", "2022-03-05"))
    //         .andExpect(status().is3xxRedirection());
    // }

    @Test
    public void postProjectWithNoName_thenShowError() throws Exception {
        this.mockMvc.perform(post("/edit-project/0")
                .param("projectName", "")
                .param("projectDescription", "desc")
                .param("projectStartDate", "2021-06-20")
                .param("projectEndDate", "2022-03-05"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Project name is required")));
    }

    @Test
    public void postProjectWitLongName_thenShowError() throws Exception {
        this.mockMvc.perform(post("/edit-project/0")
                        .param("projectName", "blah".repeat(1000))
                        .param("projectDescription", "desc")
                        .param("projectStartDate", "2021-06-20")
                        .param("projectEndDate", "2022-03-05"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Project name cannot be more than 50 characters")));
    }

    @Test
    public void postProjectWithInvalidDesc_thenShowError() throws Exception {
        this.mockMvc.perform(post("/edit-project/0")
                        .param("projectName", "")
                        .param("projectDescription", "Lorem ipsum dolor sit amet, consectetur adipisicing " +
                                "elit, sed do eiusmod cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat " +
                                "cupidatat non proident, sunt in culpa qui officia deserunt moll.")
                        .param("projectStartDate", "2021-06-20")
                        .param("projectEndDate", "2022-03-05"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Description cannot be more than 200 characters")));
    }

    @Test
    public void postProjectWithEarlyStart_thenShowError() throws Exception {
        this.mockMvc.perform(post("/edit-project/0")
                .param("projectName", "")
                .param("projectDescription", "desc")
                .param("projectStartDate", "2021-01-01")
                .param("projectEndDate", "2022-03-05"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Project cannot be set to start more than a year before" +
                                                       " it was created")));
    }

}
