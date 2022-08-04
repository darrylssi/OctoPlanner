package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.utils.EventMessage;
import nz.ac.canterbury.seng302.portfolio.utils.EventMessageOutput;
import nz.ac.canterbury.seng302.portfolio.utils.Message;
import nz.ac.canterbury.seng302.portfolio.utils.OutputMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class MessageMappingController {

    EventMessageOutput eventMessageOutput;

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
    @SendTo("/topic/messages")
    public EventMessageOutput sendEventData(EventMessage eventMessage) throws Exception {
        eventMessageOutput = new EventMessageOutput();
        eventMessageOutput.setParentProjectId(0);
        eventMessageOutput.setId(eventMessage.getId());
        eventMessageOutput.setName(eventMessage.getName());
        eventMessageOutput.setStartDate(eventMessage.getStartDate());
        eventMessageOutput.setEndDate(eventMessage.getEndDate());
        eventMessageOutput.setDescription(eventMessage.getDescription());
        eventMessageOutput.setSprintIds(eventMessage.getSprintIds());
        eventMessageOutput.setColour(eventMessage.getColour());
        return eventMessageOutput;
    }


}
