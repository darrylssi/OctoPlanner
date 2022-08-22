package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.EventMessage;
import nz.ac.canterbury.seng302.portfolio.utils.EventMessageOutput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Holds tests for the MessageMappingController class.
 *
 * Currently, it tests that EventMessageOutputs are returned correctly when the specified
 * event does or does not exist. The other methods in the controller aren't worth testing.
 */
@SpringBootTest
class MessageMappingControllerTest {

    @MockBean
    private EventService eventService;
    @MockBean
    private SprintService sprintService;

    @Autowired
    MessageMappingController messageMappingController = new MessageMappingController(new SimpMessagingTemplate((message1, timeout) -> false));


    @Test
    void whenEventDoesNotExist_thenReturnEmptyResponse() {
        when(eventService.getEventById(1)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        EventMessage message = new EventMessage();
        message.setId(1);
        EventMessageOutput eventMessageOutput = messageMappingController.sendEventData(message);
        assertEquals(1, eventMessageOutput.getId());
        assertEquals(new ArrayList<>(), eventMessageOutput.getEventListIds());
        assertNull(eventMessageOutput.getName());
    }

    @Test
    void whenEventDoesExist_thenReturnFullResponse() {
        Event event = new Event("Name", "desc", DateUtils.toDate("2022-01-05"), DateUtils.toDate("2022-02-01"));
        event.setId(1);
        Project project = new Project("ProjName", "desc", DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-04-01"));
        project.setId(1);
        event.setParentProject(project);
        when(eventService.getEventById(1)).thenReturn(event);
        when(eventService.getEventByParentProjectId(1)).thenReturn(new ArrayList<>(List.of(event)));
        when(sprintService.getSprintsInProject(1)).thenReturn(new ArrayList<>());

        EventMessage message = new EventMessage();
        message.setId(1);
        EventMessageOutput eventMessageOutput = messageMappingController.sendEventData(message);
        assertEquals(1, eventMessageOutput.getId());
        assertEquals(1, eventMessageOutput.getEventListIds().size());
        assertEquals("Name", eventMessageOutput.getName());
    }
}
