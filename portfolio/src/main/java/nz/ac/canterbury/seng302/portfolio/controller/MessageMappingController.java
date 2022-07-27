package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class MessageMappingController {

    @MessageMapping("/sendTest")
    @SendTo("/project/test")
    public String handleMessage(String message) {
        return ("Message received: " + HtmlUtils.htmlEscape(message) + "!");
    }
}
