package com.softwaremagico.kt.websockets;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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
import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.ElementDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.websockets.models.MessageContent;
import com.softwaremagico.kt.websockets.models.messages.MessageContentType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Test(groups = "restServicesUnit")
public class WebSocketControllerUnitTests {

    private ObjectMapper objectMapper;
    private SimpMessagingTemplate messagingTemplate;
    private WebSocketController webSocketController;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        objectMapper = mock(ObjectMapper.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        webSocketController = new WebSocketController(objectMapper, messagingTemplate);
    }

    @Test
    public void shouldSendCreatedUpdatedDeletedMessages() throws Exception {
        final ElementDTO elementDTO = new ElementDTO();
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"ok\":true}");

        webSocketController.elementCreated(elementDTO, "actor", "session");
        webSocketController.elementUpdated(elementDTO, "actor", "session");
        webSocketController.elementDeleted(elementDTO, "actor", "session");

        verify(messagingTemplate, times(1)).convertAndSend(eq(WebSocketConfiguration.SOCKET_SEND_PREFIX + WebSocketController.CREATING_MAPPING), isA(MessageContent.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq(WebSocketConfiguration.SOCKET_SEND_PREFIX + WebSocketController.UPDATING_MAPPING), isA(MessageContent.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq(WebSocketConfiguration.SOCKET_SEND_PREFIX + WebSocketController.DELETES_MAPPING), isA(MessageContent.class));
    }

    @Test
    public void shouldSendFightAndGroupUpdates() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        webSocketController.fightUpdated(new FightDTO(), "actor", "session");
        webSocketController.fightsCreated(Collections.singletonList(new FightDTO()), "actor", "session");
        webSocketController.untieUpdated(new DuelDTO(), "actor", "session");
        webSocketController.groupsUpdated(new TournamentDTO(), "actor", "session");

        verify(messagingTemplate, times(2)).convertAndSend(eq(WebSocketConfiguration.SOCKET_SEND_PREFIX + WebSocketController.FIGHTS_MAPPING), isA(MessageContent.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq(WebSocketConfiguration.SOCKET_SEND_PREFIX + WebSocketController.UNTIES_MAPPING), isA(MessageContent.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq(WebSocketConfiguration.SOCKET_SEND_PREFIX + WebSocketController.GROUPS_MAPPING), isA(MessageContent.class));
    }

    @Test
    public void shouldSendSystemMessages() {
        webSocketController.sendMessage("hello", MessageContentType.INFO);
        webSocketController.sendMessage("hello", MessageContentType.WARNING, "params");

        verify(messagingTemplate, times(2)).convertAndSend(eq(WebSocketConfiguration.SOCKET_SEND_PREFIX + WebSocketController.MESSAGES_MAPPING), isA(MessageContent.class));
    }

    @Test
    public void shouldHandleSerializationErrorsWithoutThrowing() throws Exception {
        doThrow(new JsonProcessingException("boom") {
        }).when(objectMapper).writeValueAsString(any());

        webSocketController.elementCreated(new ElementDTO(), "actor", "session");
        webSocketController.fightUpdated(new FightDTO(), "actor", "session");

        verify(messagingTemplate, times(0)).convertAndSend(eq(WebSocketConfiguration.SOCKET_SEND_PREFIX + WebSocketController.CREATING_MAPPING), isA(MessageContent.class));
        verify(messagingTemplate, times(0)).convertAndSend(eq(WebSocketConfiguration.SOCKET_SEND_PREFIX + WebSocketController.FIGHTS_MAPPING), isA(MessageContent.class));
    }

    @Test
    public void shouldReturnExceptionMessageAndEchoPayload() {
        assertEquals(webSocketController.handleException(new IllegalStateException("err-msg")), "err-msg");
        assertEquals(webSocketController.echo("payload"), "Echoing... payload");
    }
}



