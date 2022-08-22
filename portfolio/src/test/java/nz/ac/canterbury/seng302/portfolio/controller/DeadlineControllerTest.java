package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DeadlineController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeadlineControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    ProjectService projectService;
    @MockBean
    DeadlineService deadlineService;
    @MockBean
    private UserAccountClientService userAccountClientService;

    @Test
    @WithMockPrincipal(TEACHER)
    void deleteDeadlineAsTeacher_get200Response() throws Exception {
        Mockito.doNothing().when(deadlineService).deleteDeadline(anyInt());
        mockMvc.perform(delete("/delete-deadline/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deadline deleted."));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void deleteDeadlineAsStudent_get403Response() throws Exception {
        Mockito.doNothing().when(deadlineService).deleteDeadline(anyInt());
        mockMvc.perform(delete("/delete-deadline/1"))
                .andExpect(status().isForbidden());
    }

}
