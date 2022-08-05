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

        ArrayList<String> sprintIds = new ArrayList<>();
        if(sprints.get(0).getSprintStartDate().after(updatedEvent.getEventStartDate())) {
            sprintIds.add(FIRST_OUTSIDE_BOX_ID);
        }

        for (int i = 0; i < sprints.size(); i++) {
            if(sprints.get(i).getSprintStartDate().after(updatedEvent.getEventStartDate())){
                //sprint starts after event starts
                if(updatedEvent.getEventEndDate().after(sprints.get(i).getSprintStartDate())){
                    //event ends after sprint starts. include this sprint id
                    sprintIds.add(String.format(INSIDE_SPRINT_BOX_ID_FORMAT, sprints.get(i).getId()));
                }
                if(updatedEvent.getEventEndDate().after(sprints.get(i).getSprintEndDate())) {
                    //event ends after end of sprint. include the outside id for this sprint
                    sprintIds.add(String.format(OUTSIDE_SPRINT_BOX_ID_FORMAT, sprints.get(i).getId()));
                }
            } else {
                //event starts after sprint starts
                if(sprints.get(i).getSprintEndDate().after(updatedEvent.getEventStartDate())){
                    //sprint ends after event starts. include this sprint id
                    sprintIds.add(String.format(INSIDE_SPRINT_BOX_ID_FORMAT, sprints.get(i).getId()));
                }
                if(updatedEvent.getEventEndDate().after(sprints.get(i).getSprintEndDate()) &&
                        sprints.size() >= i+1 && sprints.get(i+1).getSprintEndDate().after(updatedEvent.getEventStartDate())) {
                    //event ends after sprint ends and starts before next sprint starts. include the outside id for this sprint
                    sprintIds.add(String.format(OUTSIDE_SPRINT_BOX_ID_FORMAT, sprints.get(i).getId()));
                }
            }
        }

        eventMessageOutput.setSprintIds(sprintIds);
        return eventMessageOutput;
    }


}
