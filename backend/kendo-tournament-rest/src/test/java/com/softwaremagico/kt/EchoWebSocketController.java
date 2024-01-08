package com.softwaremagico.kt;

import com.softwaremagico.kt.websockets.WebSocketConfiguration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class EchoWebSocketController {

    public static final String ECHO_MAPPING = "/echo";
    public static final String ECHO_INBOUND_MAPPING = "/welcome";

    @MessageMapping(ECHO_INBOUND_MAPPING)
    @SendTo(WebSocketConfiguration.SOCKET_SEND_PREFIX + ECHO_MAPPING)
    public String echo(String payload) {
        return payload;
    }
}
