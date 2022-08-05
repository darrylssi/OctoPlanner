package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Controller
public class MessageMappingController {

    EventMessageOutput eventMessageOutput;
    @Autowired
    EventService eventService;
    @Autowired
    SprintService sprintService;

    /**
     * Websocket sending example
     * @param message Data to send through the websocket
     * @return An output that gets sent to the endpoint in the @SendTo parameter
     */
    @MessageMapping("/ws")
    @SendTo("/topic/messages")
    public OutputMessage send(Message message) {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage(message.getFrom(), message.getText(), time);
    }

    /**
     * Websocket sending test data
     * @param eventMessage Data to send through the websocket
     * @return An output that gets sent to the endpoint in the @SendTo parameter
     */
    @MessageMapping("/events")
    @SendTo("/topic/events")
    public EventMessageOutput sendEventData(EventMessage eventMessage) throws Exception {
        Event updatedEvent = eventService.getEventById(eventMessage.getId());

        //Add event info to message output
        eventMessageOutput = new EventMessageOutput();
        eventMessageOutput.setParentProjectId(updatedEvent.getParentProjectId());
        eventMessageOutput.setId(eventMessage.getId());
        eventMessageOutput.setName(updatedEvent.getEventName());
        eventMessageOutput.setStartDate(DateUtils.toDisplayDateTimeString(updatedEvent.getEventStartDate()));
        eventMessageOutput.setEndDate(DateUtils.toDisplayDateTimeString(updatedEvent.getEventEndDate()));
        eventMessageOutput.setDescription(updatedEvent.getEventDescription());

        List<Sprint> sprints = sprintService.getSprintsInProject(updatedEvent.getParentProjectId());
        eventMessageOutput.setStartColour(updatedEvent.determineColour(sprints, false));
        eventMessageOutput.setEndColour(updatedEvent.determineColour(sprints, true));
        sprints.sort(Comparator.comparing(Sprint::getSprintEndDate));

//        eventMessageOutput.setSprintIds(eventMessage.getSprintIds());
        return eventMessageOutput;
    }


}
