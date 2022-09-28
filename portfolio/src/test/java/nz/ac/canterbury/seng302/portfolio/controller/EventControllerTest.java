package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.annotation.WithMockPrincipal;
import nz.ac.canterbury.seng302.portfolio.controller.forms.SchedulableForm;
import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.junit.jupiter.api.Assertions;
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
        this.mockMvc.perform(post("/project/0/edit-event/1"))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(containsString("Event not found")));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editValidEvent_get200Response() throws Exception {
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
    void editEventAsStudent_forbidden() throws Exception {
        this.mockMvc.perform(post("/project/0/edit-event/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to access this endpoint"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankNameEventAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(event.getParentProject());
        String resultString = mockMvc.perform(post("/project/0/add-event")
                        .param("name", "")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00")
                        .param("endDate", "2022-09-14")
                        .param("endTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(resultString.contains("Name cannot be blank"));
        Assertions.assertTrue(resultString.contains("Name must be between 2-32 characters"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addInvalidNameEventAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(event.getParentProject());
        mockMvc.perform(post("/project/0/add-event")
                        .param("name", "New Event 🏋️")
                        .param("description", "This is an event")
                        .param("startDate", "2022-09-09")
                .param("startTime", "12:00")
                .param("endDate", "2022-09-14")
                .param("endTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name can only have letters, numbers, spaces and punctuation except for commas"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addInvalidDescriptionEventAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(event.getParentProject());
        mockMvc.perform(post("/project/0/add-event")
                        .param("name", "New Event")
                        .param("description", "This is invalid 🥺")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00")
                        .param("endDate", "2022-09-14")
                        .param("endTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Description can only have letters, numbers, spaces and punctuation"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankDateEventAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(event.getParentProject());
        mockMvc.perform(post("/project/0/add-event")
                        .param("name", "New Event")
                        .param("description", "This is an event")
                        .param("startDate", "")
                        .param("startTime", "12:00")
                        .param("endDate", "2022-09-14")
                        .param("endTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Start date cannot be blank"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addBlankEndDateEventAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(event.getParentProject());
        mockMvc.perform(post("/project/0/add-event")
                        .param("name", "New Event")
                        .param("description", "This is an event")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00")
                        .param("endDate", "")
                        .param("endTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("End date cannot be blank"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addEarlyDateEventAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(event.getParentProject());
        mockMvc.perform(post("/project/0/add-event")
                        .param("name", "New Event")
                        .param("description", "This is an event")
                        .param("startDate", "2021-09-09")
                        .param("startTime", "12:00")
                        .param("endDate", "2022-09-14")
                        .param("endTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Event dates must be within project date range: 01/Jan/2022 - 31/Dec/2022"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addLateDateEventAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(event.getParentProject());
        mockMvc.perform(post("/project/0/add-event")
                        .param("name", "New Event")
                        .param("description", "This is an event")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00")
                        .param("endDate", "2023-09-14")
                        .param("endTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Event dates must be within project date range: 01/Jan/2022 - 31/Dec/2022"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void addEventStartAfterEndAsTeacher_get400Response() throws Exception {
        Mockito.when(projectService.getProjectById(0)).thenReturn(event.getParentProject());
        mockMvc.perform(post("/project/0/add-event")
                        .param("name", "New Event")
                        .param("description", "This is an event")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "12:00")
                        .param("endDate", "2022-09-03")
                        .param("endTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Start date must always be before end date"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editValidEventAsTeacher_get200Response() throws Exception {
        Event editEvent = new Event("New Event", "This is an event", DateUtils.toDate("2022-09-09"), DateUtils.toDate("2022-09-14"));
        editEvent.setId(1);
        editEvent.setParentProject(event.getParentProject());
        Mockito.when(eventService.saveEvent(any())).thenReturn(editEvent);
        Mockito.when(eventService.getEventById(1)).thenReturn(editEvent);
        Mockito.when(projectService.getProjectById(0)).thenReturn(event.getParentProject());
        mockMvc.perform(post("/project/0/edit-event/1")
                        .param("name", "New Event")
                        .param("description", "This is an event")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "00:00")
                        .param("endDate", "2022-09-14")
                        .param("endTime", "00:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editInvalidNameEventAsTeacher_get400Response() throws Exception {
        Event editEvent = new Event("New Event", "This is an event", DateUtils.toDate("2022-09-09"), DateUtils.toDate("2022-09-14"));
        editEvent.setId(1);
        editEvent.setParentProject(event.getParentProject());
        Mockito.when(eventService.saveEvent(any())).thenReturn(editEvent);
        Mockito.when(eventService.getEventById(1)).thenReturn(editEvent);
        Mockito.when(projectService.getProjectById(0)).thenReturn(event.getParentProject());
        mockMvc.perform(post("/project/0/edit-event/1")
                        .param("name", "🤯🤯")
                        .param("description", "This is an event")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "00:00")
                        .param("endDate", "2022-09-14")
                        .param("endTime", "00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name can only have letters, numbers, spaces and punctuation except for commas"));
    }

    @Test
    @WithMockPrincipal(TEACHER)
    void editInvalidDescriptionEventAsTeacher_get400Response() throws Exception {
        Event editEvent = new Event("New Event", "This is an event", DateUtils.toDate("2022-09-09"), DateUtils.toDate("2022-09-14"));
        editEvent.setId(1);
        editEvent.setParentProject(event.getParentProject());
        Mockito.when(eventService.saveEvent(any())).thenReturn(editEvent);
        Mockito.when(eventService.getEventById(1)).thenReturn(editEvent);
        Mockito.when(projectService.getProjectById(0)).thenReturn(event.getParentProject());
        mockMvc.perform(post("/project/0/edit-event/1")
                        .param("name", "Event")
                        .param("description", "This is an event 🤯")
                        .param("startDate", "2022-09-09")
                        .param("startTime", "00:00")
                        .param("endDate", "2022-09-14")
                        .param("endTime", "00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Description can only have letters, numbers, spaces and punctuation"));
    }
}
