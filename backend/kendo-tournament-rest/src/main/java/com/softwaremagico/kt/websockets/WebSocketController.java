package com.softwaremagico.kt.websockets;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Fight;
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
    public static final String ERRORS_MAPPING = "/errors";

    public static final String ECHO_MAPPING = "/echo";
    public static final String ECHO_INBOUND_MAPPING = "/welcome";

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
    public MessageContent sendFight(@Payload FightDTO fight) {
        try {
            return new MessageContent(Fight.class.getSimpleName(), toJson(fight));
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
    @SendToUser(WebSocketConfiguration.SOCKET_SEND_PREFIX + ERRORS_MAPPING)
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

    @MessageMapping(ECHO_MAPPING)
    @SendTo(WebSocketConfiguration.SOCKET_SEND_PREFIX + ECHO_MAPPING)
    public String echo(String payload) {
        return payload;
    }

}
