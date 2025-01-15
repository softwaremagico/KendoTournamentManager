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
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.security.JwtTokenUtil;
import com.softwaremagico.kt.websockets.models.MessageContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Test(groups = "fightsWebsockets")
@AutoConfigureMockMvc(addFilters = false)
public class FightsWebsocketsTests extends AbstractTestNGSpringContextTests {

    private static final String USER_FIRST_NAME = "Test";
    private static final String USER_LAST_NAME = "User";

    private static final String USER_NAME = USER_FIRST_NAME + "." + USER_LAST_NAME;
    private static final String USER_PASSWORD = "password";
    private static final String[] USER_ROLES = new String[]{"admin", "viewer"};

    @LocalServerPort
    private Integer port;

    @Autowired
    private AuthenticatedUserController authenticatedUserController;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Autowired
    private WebSocketController webSocketController;

    @Autowired
    private ObjectMapper objectMapper;

    private WebSocketStompClient webSocketStompClient;

    private WebSocketHttpHeaders headers;

    private AuthenticatedUser authenticatedUser;

    private <T> String toJson(T object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    private <T> T fromJson(String payload, Class<T> clazz) {
        try {
            return objectMapper.readValue(payload, clazz);
        } catch (JsonProcessingException e) {
            Assert.fail("Cannot convert JSON", e);
            return null;
        }
    }


    private String getWsPath() {
        return String.format("ws://127.0.0.1:%d/kendo-tournament-backend/%s", port, WebSocketConfiguration.SOCKETS_STOMP_URL);
    }

    @BeforeClass
    public void authentication() {
        authenticatedUser = authenticatedUserController.createUser(null, USER_NAME, USER_FIRST_NAME, USER_LAST_NAME, USER_PASSWORD, USER_ROLES);

        headers = new WebSocketHttpHeaders();
        headers.set("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken((IAuthenticatedUser) authenticatedUser, "127.0.0.1"));
    }


    @BeforeMethod
    public void setup() {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        this.webSocketStompClient = new WebSocketStompClient(webSocketClient);
        this.webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }


    @Test
    public void fightUpdated() throws ExecutionException, InterruptedException, TimeoutException {
        BlockingQueue<FightDTO> blockingQueue = new ArrayBlockingQueue<>(1);

        StompSession session = webSocketStompClient.connectAsync(getWsPath(), this.headers,
                new StompSessionHandlerAdapter() {
                }).get(1, TimeUnit.SECONDS);

        session.subscribe(WebSocketConfiguration.SOCKET_SEND_PREFIX + WebSocketController.FIGHTS_MAPPING, new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                System.out.println("Connected!");
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                throw new RuntimeException("Failure in WebSocket handling", exception);
            }

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MessageContent.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                final MessageContent message = (MessageContent) payload;
                blockingQueue.add(fromJson(message.getPayload(), FightDTO.class));
            }
        });

        final FightDTO fightDTO = new FightDTO();
        fightDTO.setShiaijo(3);

        webSocketController.fightUpdated(fightDTO, null);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(blockingQueue.poll()).isEqualTo(fightDTO));
    }

    @AfterClass
    public void deleteUser() {
        authenticatedUserProvider.delete(authenticatedUser);
    }

}
