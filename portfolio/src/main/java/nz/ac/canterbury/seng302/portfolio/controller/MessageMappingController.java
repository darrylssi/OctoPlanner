package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Schedulable;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.*;
import nz.ac.canterbury.seng302.portfolio.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller to handle websockets.
 * Sends websocket messages to endpoints specified with @SendTo annotations.
 */
@Controller
public class MessageMappingController {

    private static final Logger logger = LoggerFactory.getLogger(MessageMappingController.class);

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public MessageMappingController(SimpMessagingTemplate template) {
        this.simpMessagingTemplate = template;
    }

    @Autowired
    EventService eventService;
    @Autowired
    DeadlineService deadlineService;
    @Autowired
    MilestoneService milestoneService;
    @Autowired
    SprintService sprintService;
    @Autowired
    ProjectService projectService;

    /**
     * Called when a user disconnects from their websocket connection.
     * Logs the disconnect and sends a DISCONNECTED message to the editing schedulable endpoint.
     * @param event the event fired when the user disconnected
     */
    @EventListener
    @SendTo("/topic/editing-schedulable")
    public void onDisconnectEvent(SessionDisconnectEvent event) {
        logger.info("A user disconnected");
        Principal user = event.getUser();
        String from = user == null ? "SERVER" : user.getName();
        simpMessagingTemplate.convertAndSend("/ws",
                new Message(from, "DISCONNECTED"));
    }

    /**
     * <p>Receives a websocket message for a sprint id, then replies with an empty output if it doesn't exist,
     * or a SprintMessageOutput with the sprint's data if it does exist.</p>
     * <p>Waits for 100ms before sending the message, so that entities in the database can be updated
     * before an update is sent out for them. This is because these updates are triggered by the user
     * updating the object in the first place, so we need to wait for the object to update before we send
     * the update out to everyone.</p>
     * @param sprintMessage data received from the websocket containing the sprint id and type
     * @return a SprintMessageOutput that gets sent to the endpoint in @Sendto
     * @throws InterruptedException if the thread is interrupted while it is waiting
     */
    @MessageMapping("/sprints")
    @SendTo("/topic/sprints")
    public synchronized SprintMessageOutput sendSprintData(SprintMessage sprintMessage) throws InterruptedException {
        // wait statements must be in a while loop, so it is in a while loop
        // the loop should end if the sprint changes, but that breaks live updates (at least on localhost)
        // so it ends after 250 ms every time instead
        int count = 0;
        while (count < 250) {
            count++;
            wait(1);
        }
        SprintMessageOutput sprintMessageOutput;
        try {
            Sprint updatedSprint = sprintService.getSprintById(sprintMessage.getId());
            sprintMessageOutput = new SprintMessageOutput(updatedSprint);
        } catch (ResponseStatusException e) {
            // Send back an empty message if the sprint is not found
            logger.error(e.getMessage());
            sprintMessageOutput = new SprintMessageOutput();
            sprintMessageOutput.setId(sprintMessage.getId());
        }

        return sprintMessageOutput;
    }

    /**
     * Receives a websocket message for a project id, then replies with an empty output if it doesn't exist,
     * or a ProjecttMessageOutput with the project's data if it does exist.
     * @param projectMessage data received from the websocket containing the project id and type
     * @return a ProjectMessageOutput that gets sent to the endpoint in @Sendto
     */
    @MessageMapping("/projects")
    @SendTo("/topic/projects")
    public ProjectMessageOutput sendProjectData(ProjectMessage projectMessage) {
        ProjectMessageOutput projectMessageOutput;
        try {
            Project updatedProject = projectService.getProjectById(projectMessage.getId());
            projectMessageOutput = new ProjectMessageOutput(updatedProject);
        } catch (ResponseStatusException e) {
            // Send back an empty message if the project is not found
            logger.error(e.getMessage());
            projectMessageOutput = new ProjectMessageOutput();
            projectMessageOutput.setId(projectMessage.getId());
        }

        return projectMessageOutput;
    }

    /**
     * Websocket message mapping for editing schedulables
     * @param message Data to send through the websocket
     * @return An output that gets sent to the endpoint in the @SendTo parameter
     */
    @MessageMapping("/ws")
    @SendTo("/topic/editing-schedulable")
    public Message editingSchedulable(Message message) {
        return message;
    }

    /**
     * Receives a websocket message with a schedulable id and type, and then replies with an empty
     * output (if the specified schedulable does not exist) or a SchedulableMessageOutput with the
     * schedulable's information and location if it does exist.
     *
     * @param schedulableMessage Data received from the websocket containing the schedulable id and type
     * @return A SchedulableMessageOutput that gets sent to the endpoint in the @SendTo parameter
     */
    @MessageMapping("/schedulables")
    @SendTo("/topic/schedulables")
    public SchedulableMessageOutput sendSchedulableData(SchedulableMessage schedulableMessage) {
        Schedulable updatedSchedulable;
        SchedulableMessageOutput schedulableMessageOutput;
        List<Schedulable> schedulableList = new ArrayList<>();

        try {
            /* Determine schedulable type and get data.
             * Return a proper response if the schedulable exists. */
            if (EVENT_TYPE.equals(schedulableMessage.getType())) {
                updatedSchedulable = eventService.getEventById(schedulableMessage.getId());
            } else if (DEADLINE_TYPE.equals(schedulableMessage.getType())) {
                updatedSchedulable = deadlineService.getDeadlineById(schedulableMessage.getId());
            } else if (MILESTONE_TYPE.equals(schedulableMessage.getType())) {
                updatedSchedulable = milestoneService.getMilestoneById(schedulableMessage.getId());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, schedulableMessage.getType() + " is not a type of schedulable!");
            }

            schedulableList.addAll(eventService.getEventByParentProjectId(updatedSchedulable.getParentProject().getId()));
            schedulableList.addAll(deadlineService.getDeadlineByParentProjectId(updatedSchedulable.getParentProject().getId()));
            schedulableList.addAll(milestoneService.getMilestoneByParentProjectId(updatedSchedulable.getParentProject().getId()));
            
            schedulableMessageOutput = new SchedulableMessageOutput(updatedSchedulable,
                    sprintService.getSprintsInProject(updatedSchedulable.getParentProject().getId()),
                    schedulableList);
        } catch (ResponseStatusException e) {
            // Send back an empty response if the schedulable doesn't exist
            logger.error(e.getMessage());
            schedulableMessageOutput = new SchedulableMessageOutput();
            schedulableMessageOutput.setSchedulableListIds(new ArrayList<>());
            schedulableMessageOutput.setId(schedulableMessage.getId());
            schedulableMessageOutput.setType(schedulableMessage.getType());
        }

        return schedulableMessageOutput;
    }
}
