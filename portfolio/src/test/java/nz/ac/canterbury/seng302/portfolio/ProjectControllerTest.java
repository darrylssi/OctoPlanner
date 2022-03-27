package nz.ac.canterbury.seng302.portfolio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getProjectMissingId_throw404() throws Exception {
        this.mockMvc.perform(get("/edit-project/999"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(containsString("Project not found")));
    }

    @Test
    public void getProjectValidId() throws Exception {
        this.mockMvc.perform(get("/edit-project/0"))
                .andExpect(status().isOk());
    }

    @Test
    public void postValidProject_thenRedirect() throws Exception {
        this.mockMvc.perform(post("/edit-project/0")
                .param("projectName", "name")
                .param("projectDescription", "desc")
                .param("projectStartDate", "2021-03-04")
                .param("projectEndDate", "2022-03-05"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void postProjectWithInvalidName_thenShowError() throws Exception {
        this.mockMvc.perform(post("/edit-project/0")
                .param("projectName", "")
                .param("projectDescription", "desc")
                .param("projectStartDate", "2021-03-04")
                .param("projectEndDate", "2022-03-05"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Project name is required")));
    }

}
