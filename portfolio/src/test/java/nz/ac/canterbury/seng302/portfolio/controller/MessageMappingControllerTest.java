package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.ProjectMessage;
import nz.ac.canterbury.seng302.portfolio.utils.ProjectMessageOutput;
import nz.ac.canterbury.seng302.portfolio.utils.SchedulableMessage;
import nz.ac.canterbury.seng302.portfolio.utils.SchedulableMessageOutput;
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
import static org.mockito.Mockito.when;
import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * Holds tests for the MessageMappingController class.
 *
 * Currently, it tests that SchedulableMessageOutputs are returned correctly when the specified
 * event does or does not exist. The other methods in the controller aren't worth testing.
 */
@SpringBootTest
class MessageMappingControllerTest {

    @MockBean
    private EventService eventService;
    @MockBean
    private DeadlineService deadlineService;
    @MockBean
    private SprintService sprintService;
    @MockBean
    private ProjectService projectService;

    @Autowired
    MessageMappingController messageMappingController = new MessageMappingController(new SimpMessagingTemplate((message1, timeout) -> false));


    @Test
    void whenEventDoesNotExist_thenReturnEmptyResponse() {
        when(eventService.getEventById(1)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));
        SchedulableMessage message = new SchedulableMessage();
        message.setId(1);
        message.setType(EVENT_TYPE);
        SchedulableMessageOutput SchedulableMessageOutput = messageMappingController.sendSchedulableData(message);
        assertEquals(1, SchedulableMessageOutput.getId());
        assertEquals(new ArrayList<>(), SchedulableMessageOutput.getSchedulableListIds());
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

        SchedulableMessage message = new SchedulableMessage();
        message.setId(1);
        message.setType(EVENT_TYPE);
        SchedulableMessageOutput SchedulableMessageOutput = messageMappingController.sendSchedulableData(message);
        assertEquals(1, SchedulableMessageOutput.getId());
        assertEquals(1, SchedulableMessageOutput.getSchedulableListIds().size());
    }

    @Test
    void whenDeadlineDoesNotExist_thenReturnEmptyResponse() {
        when(deadlineService.getDeadlineById(1)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Deadline not found."));
        SchedulableMessage message = new SchedulableMessage();
        message.setId(1);
        message.setType(DEADLINE_TYPE);
        SchedulableMessageOutput SchedulableMessageOutput = messageMappingController.sendSchedulableData(message);
        assertEquals(1, SchedulableMessageOutput.getId());
        assertEquals(new ArrayList<>(), SchedulableMessageOutput.getSchedulableListIds());
    }

    @Test
    void whenDeadlineDoesExist_thenReturnFullResponse() {
        Deadline deadline = new Deadline("Name", "desc", DateUtils.toDate("2022-01-05"));
        deadline.setId(1);
        Project project = new Project("ProjName", "desc", DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-04-01"));
        project.setId(1);
        deadline.setParentProject(project);
        when(deadlineService.getDeadlineById(1)).thenReturn(deadline);
        when(deadlineService.getDeadlineByParentProjectId(1)).thenReturn(new ArrayList<>(List.of(deadline)));
        when(sprintService.getSprintsInProject(1)).thenReturn(new ArrayList<>());

        SchedulableMessage message = new SchedulableMessage();
        message.setId(1);
        message.setType(DEADLINE_TYPE);
        SchedulableMessageOutput SchedulableMessageOutput = messageMappingController.sendSchedulableData(message);
        assertEquals(1, SchedulableMessageOutput.getId());
        assertEquals(1, SchedulableMessageOutput.getSchedulableListIds().size());
    }

    @Test
    void whenProjectDoesNotExist_thenReturnEmptyResponse() {
        when(projectService.getProjectById(1)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
        ProjectMessage message = new ProjectMessage();
        message.setId(1);
        ProjectMessageOutput projectMessageOutput = messageMappingController.sendProjectData(message);
        assertEquals(1, projectMessageOutput.getId());
    }

    @Test
    void whenProjectDoesExist_thenReturnFullResponse() {
        Project project = new Project("ProjName", "desc", DateUtils.toDate("2022-01-01"), DateUtils.toDate("2022-04-01"));
        project.setId(1);
        when(projectService.getProjectById(1)).thenReturn(project);

        ProjectMessage message = new ProjectMessage();
        message.setId(1);
        ProjectMessageOutput projectMessageOutput = messageMappingController.sendProjectData(message);
        assertEquals(1, projectMessageOutput.getId());
        // With correct information
        assertEquals(project.getProjectName(), projectMessageOutput.getName());
        assertEquals(project.getProjectStartDate(), projectMessageOutput.getStartDate());
        assertEquals(project.getProjectEndDate(), projectMessageOutput.getEndDate());
    }
}
