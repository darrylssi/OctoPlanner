package nz.ac.canterbury.seng302.portfolio.controller;
import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DetailsController.class)
@AutoConfigureMockMvc(addFilters = false)
class DetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    EventService eventService;
    @MockBean
    SprintService sprintService;
    @MockBean
    DeadlineService deadlineService;
    @MockBean
    ProjectService projectService;
    @MockBean
    DeadlineService deadlineService;
    @MockBean
    private SprintLabelService labelUtils;
    @MockBean
    private UserAccountClientService userAccountClientService;

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
        Mockito.doNothing().when(sprintService).deleteSprint(anyInt());
        mockMvc.perform(delete("/delete-sprint/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authorised."));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void deleteEventAsTeacher_get200Response() throws Exception {
        Mockito.doNothing().when(eventService).deleteEvent(anyInt());
        mockMvc.perform(delete("/delete-event/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Event deleted."));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void deleteEventAsStudent_get401Response() throws Exception {
        Mockito.doNothing().when(eventService).deleteEvent(anyInt());
        mockMvc.perform(delete("/delete-event/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User not authorised."));
    }
}
