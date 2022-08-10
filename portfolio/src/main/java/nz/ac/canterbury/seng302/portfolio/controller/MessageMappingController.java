package nz.ac.canterbury.seng302.portfolio.controller;

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
}
