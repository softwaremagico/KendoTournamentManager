package com.softwaremagico.kt.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.websockets.models.MessageContent;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    public static final String FIGHTS_MAPPING = "/fights";
    public static final String MESSAGES_MAPPING = "/messages";

    private final ObjectMapper objectMapper;

    public WebSocketController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private <T> String toJson(T object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Sends a fightDTO to {@value com.softwaremagico.kt.websockets.WebSocketConfiguration#SOCKET_SEND_PREFIX} + {@value #FIGHTS_MAPPING}.
     *
     * @param fight the fight to send.
     * @return
     */
    @MessageMapping(FIGHTS_MAPPING)
    @SendTo(WebSocketConfiguration.SOCKET_SEND_PREFIX + FIGHTS_MAPPING)
    @SubscribeMapping(FIGHTS_MAPPING)
    public FightDTO sendFight(@Payload FightDTO fight) {
        try {
            return fight;
        } catch (Exception e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
        }
        return null;
    }

    /**
     * Sends a fightDTO to {@value com.softwaremagico.kt.websockets.WebSocketConfiguration#SOCKET_SEND_PREFIX} + {@value #MESSAGES_MAPPING}.
     *
     * @param message the message to send.
     * @return
     */
    @SendTo(WebSocketConfiguration.SOCKET_SEND_PREFIX + MESSAGES_MAPPING)
    public MessageContent sendMessage(String message) {
        try {
            return new MessageContent(String.class.getSimpleName(), message);
        } catch (Exception e) {
            KendoTournamentLogger.errorMessage(this.getClass(), e);
        }
        return null;
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

}
