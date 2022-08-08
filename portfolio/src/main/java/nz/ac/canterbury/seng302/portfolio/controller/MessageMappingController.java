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
import java.util.ArrayList;
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

    private static final String INSIDE_SPRINT_BOX_ID_FORMAT = "events-%d-inside";
    private static final String OUTSIDE_SPRINT_BOX_ID_FORMAT = "events-%d-outside";
    private static final String FIRST_OUTSIDE_BOX_ID = "events-firstOutside";

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
        eventMessageOutput.setStartColour(updatedEvent.determineColour(sprints, false) + "4c");
        eventMessageOutput.setEndColour(updatedEvent.determineColour(sprints, true) + "4c");
        sprints.sort(Comparator.comparing(Sprint::getSprintEndDate));

        List<Event> events = eventService.getAllEvents();
        events.sort(Comparator.comparing(Event::getEventStartDate));

        ArrayList<String> sprintIds = new ArrayList<>();
        ArrayList<String> eventIds = new ArrayList<>();
        if(sprints.get(0).getSprintStartDate().after(updatedEvent.getEventStartDate())) {
            sprintIds.add(FIRST_OUTSIDE_BOX_ID);
        }

        //get list of all event box ids to include the event on the project details page
        for (int i = 0; i < sprints.size(); i++) {
            if(timesOverlap(sprints.get(i).getSprintStartDate(), sprints.get(i).getSprintEndDate(),
                    updatedEvent.getEventStartDate(), updatedEvent.getEventEndDate())){
                sprintIds.add(String.format(INSIDE_SPRINT_BOX_ID_FORMAT, sprints.get(i).getId()));
                for (int j = 0; j < events.size(); j++) {
                    if(!events.get(j).getEventStartDate().after(updatedEvent.getEventStartDate())){
                        continue;
                    }
                    if (timesOverlap(sprints.get(i).getSprintStartDate(), sprints.get(i).getSprintEndDate(),
                            events.get(j).getEventStartDate(), events.get(j).getEventEndDate())){
                        eventIds.add(String.valueOf(events.get(j).getId()));
                        break;
                    }
                }
            }
            if((sprints.size() > i+1) && timesOverlap(sprints.get(i).getSprintEndDate(), sprints.get(i+1).getSprintStartDate(),
                    updatedEvent.getEventStartDate(), updatedEvent.getEventEndDate())) {
                sprintIds.add(String.format(OUTSIDE_SPRINT_BOX_ID_FORMAT, sprints.get(i).getId()));
            }
        }

        eventMessageOutput.setSprintIds(sprintIds);
        eventMessageOutput.setEventIds(eventIds);
        return eventMessageOutput;
    }

    private boolean timesOverlap(Date startA, Date endA, Date startB, Date endB){
        if (startA.after(startB)){
            return startA.before(endB);
        }
        return startB.before(endA);
    }


}
