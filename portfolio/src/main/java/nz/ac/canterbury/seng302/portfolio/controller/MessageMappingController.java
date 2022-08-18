package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.utils.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

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

    EventMessageOutput eventMessageOutput;
    @Autowired
    EventService eventService;
    @Autowired
    SprintService sprintService;

    private static final String INSIDE_SPRINT_BOX_ID_FORMAT = "events-%d-inside";
    private static final String OUTSIDE_SPRINT_BOX_ID_FORMAT = "events-%d-outside";
    private static final String FIRST_OUTSIDE_BOX_ID = "events-firstOutside";
    private static final String EVENT_ID_FORMAT = "event-box-%d";
    private static final String EVENT_FIRST_ID_FORMAT = "%d-before";
    private static final String EVENT_IN_ID_FORMAT = "%d-in-%d";
    private static final String EVENT_AFTER_ID_FORMAT = "%d-after-%d";


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
     * Websocket sending test data
     * @param eventMessage Data to send through the websocket
     * @return An output that gets sent to the endpoint in the @SendTo parameter
     */
    @MessageMapping("/events")
    @SendTo("/topic/events")
    public EventMessageOutput sendEventData(EventMessage eventMessage) {
        Event updatedEvent;
        try {
            updatedEvent = eventService.getEventById(eventMessage.getId());
        } catch (Exception e){
            eventMessageOutput = new EventMessageOutput();
            eventMessageOutput.setSprintIds(new ArrayList<>());
            eventMessageOutput.setId(eventMessage.getId());
            return eventMessageOutput;
        }

        //Add event info to message output
        eventMessageOutput = new EventMessageOutput();
        eventMessageOutput.setParentProjectId(updatedEvent.getParentProject().getId());
        eventMessageOutput.setId(eventMessage.getId());
        eventMessageOutput.setName(updatedEvent.getEventName());
        eventMessageOutput.setEndDate(updatedEvent.getEventEndDate());
        eventMessageOutput.setStartDate(updatedEvent.getEventStartDate());
        eventMessageOutput.setStartDateString(DateUtils.toDisplayDateTimeString(updatedEvent.getEventStartDate()));
        eventMessageOutput.setEndDateString(DateUtils.toDisplayDateTimeString(updatedEvent.getEventEndDate()));
        eventMessageOutput.setDescription(updatedEvent.getEventDescription());

        List<Sprint> sprints = sprintService.getSprintsInProject(updatedEvent.getParentProject().getId());
        eventMessageOutput.setStartColour(updatedEvent.determineColour(sprints, false) + "4c");
        eventMessageOutput.setEndColour(updatedEvent.determineColour(sprints, true) + "4c");
        sprints.sort(Comparator.comparing(Sprint::getSprintEndDate));

        List<Event> events = eventService.getAllEvents();
        events.sort(Comparator.comparing(Event::getEventStartDate));

        ArrayList<String> sprintIds = new ArrayList<>();
        ArrayList<String> eventIds = new ArrayList<>();
        ArrayList<String> eventBoxIds = new ArrayList<>();
        if(sprints.get(0).getSprintStartDate().after(updatedEvent.getEventStartDate())) {
            sprintIds.add(FIRST_OUTSIDE_BOX_ID);
            eventIds.add(getNextEvent(events, updatedEvent.getParentProject().getProjectStartDate(),
                    sprints.get(0).getSprintStartDate(), updatedEvent.getEventStartDate()));
            eventBoxIds.add(String.format(EVENT_FIRST_ID_FORMAT, updatedEvent.getId()));
        }

        //get list of all event box ids to include the event on the project details page
        for (int i = 0; i < sprints.size(); i++) {
            if(DateUtils.timesOverlap(sprints.get(i).getSprintStartDate(), sprints.get(i).getSprintEndDate(),
                    updatedEvent.getEventStartDate(), updatedEvent.getEventEndDate())){
                sprintIds.add(String.format(INSIDE_SPRINT_BOX_ID_FORMAT, sprints.get(i).getId()));
                eventIds.add(getNextEvent(events, sprints.get(i).getSprintStartDate(),
                        sprints.get(i).getSprintEndDate(), updatedEvent.getEventStartDate()));
                eventBoxIds.add(String.format(EVENT_IN_ID_FORMAT, updatedEvent.getId(), sprints.get(i).getId()));
            }
            if((sprints.size() > i+1) && DateUtils.timesOverlap(sprints.get(i).getSprintEndDate(), sprints.get(i+1).getSprintStartDate(),
                    updatedEvent.getEventStartDate(), updatedEvent.getEventEndDate())) {
                sprintIds.add(String.format(OUTSIDE_SPRINT_BOX_ID_FORMAT, sprints.get(i).getId()));
                eventIds.add(getNextEvent(events, sprints.get(i).getSprintEndDate(),
                        sprints.get(i+1).getSprintStartDate(), updatedEvent.getEventStartDate()));
                eventBoxIds.add(String.format(EVENT_AFTER_ID_FORMAT, updatedEvent.getId(), sprints.get(i).getId()));
            }
        }

        eventMessageOutput.setSprintIds(sprintIds);
        eventMessageOutput.setEventIds(eventIds);
        eventMessageOutput.setEventBoxIds(eventBoxIds);
        return eventMessageOutput;
    }

    private String getNextEvent(List<Event> events, Date periodStart, Date periodEnd, Date eventStartDate){
        for (Event event : events) {
            if (event.getEventStartDate().after(eventStartDate) &&
                    DateUtils.timesOverlap(periodStart, periodEnd,
                            event.getEventStartDate(), event.getEventEndDate())) {
                return (String.format(EVENT_ID_FORMAT, event.getId()));
            }
        }
        return "-1";
    }


}
