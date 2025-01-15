package com.softwaremagico.kt.websockets;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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
import com.softwaremagico.kt.core.controller.models.ElementDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.logger.WebsocketsLogger;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.websockets.models.MessageContent;
import com.softwaremagico.kt.websockets.models.messages.MessageContentType;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WebSocketController {

    public static final String FIGHTS_MAPPING = "/fights";
    public static final String GROUPS_MAPPING = "/groups";
    public static final String CREATING_MAPPING = "/creates";
    public static final String UPDATING_MAPPING = "/updates";
    public static final String DELETES_MAPPING = "/deletes";
    public static final String MESSAGES_MAPPING = "/messages";
    public static final String ERRORS_MAPPING = "/errors";

    public static final String ECHO_MAPPING = "/echo";

    private final ObjectMapper objectMapper;

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    private <T> String toJson(T object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Sends an Element to {@value com.softwaremagico.kt.websockets.WebSocketConfiguration#SOCKET_SEND_PREFIX} + {@value #CREATING_MAPPING}.
     *
     * @param element the element created.
     * @return
     */
    public void elementCreated(@Payload ElementDTO element, String actor) {
        try {
            WebsocketsLogger.debug(this.getClass(), "Creating element '{}'.", element);
            this.messagingTemplate.convertAndSend(WebSocketConfiguration.SOCKET_SEND_PREFIX + CREATING_MAPPING,
                    new MessageContent(element.getClass().getSimpleName(), toJson(element), MessageContentType.CREATED, actor));
        } catch (Exception e) {
            WebsocketsLogger.errorMessage(this.getClass(), e);
        }
    }

    /**
     * Sends an Element to {@value com.softwaremagico.kt.websockets.WebSocketConfiguration#SOCKET_SEND_PREFIX} + {@value #CREATING_MAPPING}.
     *
     * @param element the element created.
     * @return
     */
    public void elementUpdated(@Payload ElementDTO element, String actor) {
        try {
            WebsocketsLogger.debug(this.getClass(), "Updating element '{}'.", element);
            this.messagingTemplate.convertAndSend(WebSocketConfiguration.SOCKET_SEND_PREFIX + UPDATING_MAPPING,
                    new MessageContent(element.getClass().getSimpleName(), toJson(element), MessageContentType.UPDATED, actor));
        } catch (Exception e) {
            WebsocketsLogger.errorMessage(this.getClass(), e);
        }
    }

    /**
     * Sends an Element to {@value com.softwaremagico.kt.websockets.WebSocketConfiguration#SOCKET_SEND_PREFIX} + {@value #CREATING_MAPPING}.
     *
     * @param element the element created.
     * @return
     */
    public void elementDeleted(@Payload ElementDTO element, String actor) {
        try {
            WebsocketsLogger.debug(this.getClass(), "Deleting element '{}'.", element);
            this.messagingTemplate.convertAndSend(WebSocketConfiguration.SOCKET_SEND_PREFIX + DELETES_MAPPING,
                    new MessageContent(element.getClass().getSimpleName(), toJson(element), MessageContentType.DELETED, actor));
        } catch (Exception e) {
            WebsocketsLogger.errorMessage(this.getClass(), e);
        }
    }

    /**
     * Sends a fightDTO to {@value com.softwaremagico.kt.websockets.WebSocketConfiguration#SOCKET_SEND_PREFIX} + {@value #FIGHTS_MAPPING}.
     *
     * @param fight the fight to send.
     * @return
     */
    public void fightUpdated(@Payload FightDTO fight, String actor) {
        try {
            WebsocketsLogger.debug(this.getClass(), "Sending fight '{}'.", fight);
            this.messagingTemplate.convertAndSend(WebSocketConfiguration.SOCKET_SEND_PREFIX + FIGHTS_MAPPING,
                    new MessageContent(Fight.class.getSimpleName(), toJson(fight), MessageContentType.UPDATED, actor));
        } catch (Exception e) {
            WebsocketsLogger.errorMessage(this.getClass(), e);
        }
    }

    /**
     * Sends a fightDTO to {@value com.softwaremagico.kt.websockets.WebSocketConfiguration#SOCKET_SEND_PREFIX} + {@value #FIGHTS_MAPPING}.
     *
     * @param fights the fight to send.
     * @return
     */
    public void fightsCreated(@Payload List<FightDTO> fights, String actor) {
        try {
            WebsocketsLogger.debug(this.getClass(), "Sending fights '{}'.", fights);
            this.messagingTemplate.convertAndSend(WebSocketConfiguration.SOCKET_SEND_PREFIX + FIGHTS_MAPPING,
                    new MessageContent(Fight.class.getSimpleName(), toJson(fights), MessageContentType.CREATED, actor));
        } catch (Exception e) {
            WebsocketsLogger.errorMessage(this.getClass(), e);
        }
    }

    /**
     * Advise that the groups of a tournament has been updated {@value com.softwaremagico.kt.websockets.WebSocketConfiguration#SOCKET_SEND_PREFIX}
     * + {@value #GROUPS_MAPPING}.
     *
     * @param tournamentDTO the tournament that is modified.
     * @return
     */
    public void groupsUpdated(@Payload TournamentDTO tournamentDTO, String actor) {
        try {
            WebsocketsLogger.debug(this.getClass(), "Groups from  '{}' has been updated.", tournamentDTO);
            this.messagingTemplate.convertAndSend(WebSocketConfiguration.SOCKET_SEND_PREFIX + GROUPS_MAPPING,
                    new MessageContent(Group.class.getSimpleName(), toJson(tournamentDTO), MessageContentType.UPDATED, actor));
        } catch (Exception e) {
            WebsocketsLogger.errorMessage(this.getClass(), e);
        }
    }


    public void sendMessage(String message, MessageContentType type) {
        try {
            WebsocketsLogger.debug(this.getClass(), "Sending message '{}' of type '{}'.", message, type);
            this.messagingTemplate.convertAndSend(WebSocketConfiguration.SOCKET_SEND_PREFIX + MESSAGES_MAPPING,
                    new MessageContent(String.class.getSimpleName(), message, type));
        } catch (Exception e) {
            WebsocketsLogger.errorMessage(this.getClass(), e);
        }
    }


    public void sendMessage(String message, MessageContentType type, Object parameters) {
        try {
            WebsocketsLogger.debug(this.getClass(), "Sending message '{}' of type '{}' with parameters '{}'.", message, type, parameters);
            this.messagingTemplate.convertAndSend(WebSocketConfiguration.SOCKET_SEND_PREFIX + MESSAGES_MAPPING,
                    new MessageContent(String.class.getSimpleName(), message, type, parameters));
        } catch (Exception e) {
            WebsocketsLogger.errorMessage(this.getClass(), e);
        }
    }

    @MessageExceptionHandler
    @SendToUser(WebSocketConfiguration.SOCKET_SEND_PREFIX + ERRORS_MAPPING)
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

    /**
     * Only for testing.
     *
     * @param payload
     * @return
     */
    @MessageMapping(ECHO_MAPPING)
    @SendTo(WebSocketConfiguration.SOCKET_SEND_PREFIX + ECHO_MAPPING)
    public String echo(String payload) {
        return "Echoing... " + payload;
    }

}
