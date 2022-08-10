package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.utils.Message;
import nz.ac.canterbury.seng302.portfolio.utils.OutputMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class MessageMappingController {

    private static final Logger logger = LoggerFactory.getLogger(MessageMappingController.class);

    private SimpMessagingTemplate simpMessagingTemplate;

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
        simpMessagingTemplate.convertAndSend("/ws/editing-event",
                new Message(from, "DISCONNECTED"));
    }

    /**
     * Websocket sending example
     * @param message Data to send through the websocket
     * @return An output that gets sent to the endpoint in the @SendTo parameter
     */
    @MessageMapping("/ws")
    @SendTo("/topic/messages")
    public OutputMessage send(Message message) {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage(message.getFrom(), message.getContent(), time);
    }

    /**
     * Websocket message mapping for editing events
     * @param message Data to send through the websocket
     * @return An output that gets sent to the endpoint in the @SendTo parameter
     */
    @MessageMapping("/ws/editing-event")
    @SendTo("/topic/editing-event")
    public Message editingEvent(Message message) {
        return message;
    }
}
