package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.controller.forms.SchedulableForm;
import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for post and delete requests for events handled by the event controller
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    EventService eventService;
    @MockBean
    SprintService sprintService;
    @MockBean
    ProjectService projectService;
    @MockBean
    DetailsController detailsController;
    @MockBean
    private UserAccountClientService userAccountClientService;

    private SchedulableForm eventForm;
    private Event event;

    @BeforeEach
    void setup() {
        eventForm = new SchedulableForm();
        eventForm.setName("Event");
        eventForm.setStartDate(LocalDate.now());
        eventForm.setStartTime(LocalTime.now());

        Project parentProject = new Project("Project 2022", "Test Parent Project", "2022-01-01", "2022-12-31");

        event = new Event();
        event.setId(1);
        event.setParentProject(parentProject);
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
    void deleteEventAsStudent_get403Response() throws Exception {
        Mockito.doNothing().when(eventService).deleteEvent(anyInt());
        mockMvc.perform(delete("/delete-event/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }
    
    @Test
    @WithMockPrincipal(TEACHER)
    void postEventMissingId_throw404() throws Exception {
        when(eventService.getEventById(anyInt()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        this.mockMvc.perform(post("/project/0/edit-event/1")
                        .param("eventForm", String.valueOf(eventForm)))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(containsString("Event not found")));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void postValidEvent_redirect() throws Exception {
        when(eventService.getEventById(anyInt()))
                .thenReturn(event);
        when(eventService.saveEvent(any()))
                .thenReturn(event);
        this.mockMvc.perform(post("/project/0/edit-event/1")
                        .param("name", eventForm.getName())
                        .param("description", "")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00")
                        .param("endDate", "2022-10-09")
                        .param("endTime", "12:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockPrincipal(STUDENT)
    void postEventEditPage_forbidden() throws Exception {
        this.mockMvc.perform(post("/project/0/edit-event/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }
}
