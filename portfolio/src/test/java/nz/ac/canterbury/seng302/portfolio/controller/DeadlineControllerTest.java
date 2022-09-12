package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.controller.forms.SchedulableForm;
import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.DeadlineRepository;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
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

import java.time.LocalDate;
import java.time.LocalTime;

import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.STUDENT;
import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.TEACHER;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    DeadlineRepository deadlineRepository;

    @MockBean
    DeadlineService deadlineService;

    @MockBean
    private UserAccountClientService userAccountClientService;

    private SchedulableForm deadlineForm;
    private Deadline deadline;

    @BeforeEach
    void setup() {
        deadlineForm = new SchedulableForm();
        deadlineForm.setName("Deadline");
        deadlineForm.setStartDate(LocalDate.now());
        deadlineForm.setStartTime(LocalTime.now());

        deadline = new Deadline();
        deadline.setId(1);
    }

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
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void postDeadlineMissingId_throw404() throws Exception {
        when(deadlineService.getDeadlineById(anyInt()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Deadline not found"));
        this.mockMvc.perform(post("/project/0/edit-deadline/1")
                        .param("deadlineForm", String.valueOf(deadlineForm)))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(containsString("Deadline not found")));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void postValidDeadline_returnDeadlineId() throws Exception {
        when(deadlineService.getDeadlineById(anyInt()))
                .thenReturn(deadline);
        when(deadlineService.saveDeadline(any()))
                .thenReturn(deadline);
        this.mockMvc.perform(post("/project/0/edit-deadline/1")
                        .param("name", deadlineForm.getName())
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void postDeadlineEditPage_forbidden() throws Exception {
        this.mockMvc.perform(post("/project/0/edit-deadline/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }
}
