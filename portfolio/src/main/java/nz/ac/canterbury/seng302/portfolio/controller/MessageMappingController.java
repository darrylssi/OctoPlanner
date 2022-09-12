package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Schedulable;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.SchedulableMessage;
import nz.ac.canterbury.seng302.portfolio.utils.SchedulableMessageOutput;
import nz.ac.canterbury.seng302.portfolio.utils.Message;
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
    // TODO: uncomment once added
    // @Autowired
    // MilestoneService milestoneService;
    @Autowired
    SprintService sprintService;

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
        Schedulable updatedSchedulable = null;
        SchedulableMessageOutput schedulableMessageOutput;
        List<Schedulable> schedulableList = new ArrayList<>();

        try {
            /* Determine schedulable type and get data.
             * Return a proper response if the schedulable exists. */
            switch(schedulableMessage.getType()) {
                case EVENT_TYPE -> {
                    updatedSchedulable = eventService.getEventById(schedulableMessage.getId());
                    break;
                }
                case DEADLINE_TYPE -> {
                    updatedSchedulable = deadlineService.getDeadlineById(schedulableMessage.getId());
                    break;
                }
                case MILESTONE_TYPE -> {
                    // updatedSchedulable = milestoneService.getMilestoneById(schedulableMessage.getId());
                    // break;
                    throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Milestones do not support live editing");
                }
                default -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, schedulableMessage.getType() + " is not a type of schedulable!");
                }
            }

            schedulableList.addAll(eventService.getEventByParentProjectId(updatedSchedulable.getParentProject().getId()));
            schedulableList.addAll(deadlineService.getDeadlineByParentProjectId(updatedSchedulable.getParentProject().getId()));
            // schedulableList.addAll(milestoneService.getMilestoneByParentProjectId(updatedSchedulable.getParentProject().getId()));
            
            schedulableMessageOutput = new SchedulableMessageOutput(updatedSchedulable,
                    sprintService.getSprintsInProject(updatedSchedulable.getParentProject().getId()),
                    schedulableList);
        } catch (ResponseStatusException e) {
            // Send back an empty response if the schedulable doesn't exist
            logger.error(e.getMessage());
            schedulableMessageOutput = new SchedulableMessageOutput();
            schedulableMessageOutput.setSchedulableListIds(new ArrayList<>());
            schedulableMessageOutput.setId(schedulableMessage.getId());
        }

        return schedulableMessageOutput;
    }
}
