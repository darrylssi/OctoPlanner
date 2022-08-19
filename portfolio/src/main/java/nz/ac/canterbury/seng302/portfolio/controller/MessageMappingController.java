package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.EventMessage;
import nz.ac.canterbury.seng302.portfolio.utils.EventMessageOutput;
import nz.ac.canterbury.seng302.portfolio.utils.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.ArrayList;

@Controller
public class MessageMappingController {

    private static final Logger logger = LoggerFactory.getLogger(MessageMappingController.class);

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public MessageMappingController(SimpMessagingTemplate template) {
        this.simpMessagingTemplate = template;
    }

    @EventListener
    @SendTo("/topic/editing-event")
    public void onDisconnectEvent(SessionDisconnectEvent event) {
        logger.info("A user disconnected");
        Principal user = event.getUser();
        String from = user == null ? "SERVER" : user.getName();
        simpMessagingTemplate.convertAndSend("/ws",
                new Message(from, "DISCONNECTED"));
    }

    @Autowired
    EventService eventService;
    @Autowired
    SprintService sprintService;

    /**
     * Websocket message mapping for editing events
     * @param message Data to send through the websocket
     * @return An output that gets sent to the endpoint in the @SendTo parameter
     */
    @MessageMapping("/ws")
    @SendTo("/topic/editing-event")
    public Message editingEvent(Message message) {
        return message;
    }

    /**
     * Receives a websocket message with an event id, and then replies with an empty output
     * (if the specified event does not exist) or an EventMessageOutput with the event's information
     * and location if it does exist.
     *
     * @param eventMessage Data received from the websocket containing the event id
     * @return An EventMessageOutput that gets sent to the endpoint in the @SendTo parameter
     */
    @MessageMapping("/events")
    @SendTo("/topic/events")
    public EventMessageOutput sendEventData(EventMessage eventMessage) {
        Event updatedEvent;
        EventMessageOutput eventMessageOutput;

        try {
            // Return a proper response if the event exists
            updatedEvent = eventService.getEventById(eventMessage.getId());
            eventMessageOutput = new EventMessageOutput(updatedEvent,
                    sprintService.getSprintsInProject(updatedEvent.getParentProject().getId()),
                    eventService.getEventByParentProjectId(updatedEvent.getParentProject().getId()));
        } catch (Exception e) {
            // Send back an empty response if the event doesn't exist
            logger.error(e.getMessage());
            eventMessageOutput = new EventMessageOutput();
            eventMessageOutput.setEventListIds(new ArrayList<>());
            eventMessageOutput.setId(eventMessage.getId());
        }

        return eventMessageOutput;
    }
}
