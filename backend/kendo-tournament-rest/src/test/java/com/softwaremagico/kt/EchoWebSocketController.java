package com.softwaremagico.kt;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class EchoWebSocketController {

    @MessageMapping("/welcome")
    @SendTo("/topic/echo")
    public String echo(String payload) {
        return payload;
    }
}
