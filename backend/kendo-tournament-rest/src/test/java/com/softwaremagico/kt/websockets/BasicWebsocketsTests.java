package com.softwaremagico.kt.websockets;

import com.softwaremagico.kt.EchoWebSocketController;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.rest.controllers.AuthenticatedUserController;
import com.softwaremagico.kt.rest.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Test(groups = "websockets")
@AutoConfigureMockMvc(addFilters = false)
public class BasicWebsocketsTests extends AbstractTestNGSpringContextTests {

    private final static String USER_FIRST_NAME = "Test";
    private final static String USER_LAST_NAME = "User";

    private static final String USER_NAME = USER_FIRST_NAME + "." + USER_LAST_NAME;
    private static final String USER_PASSWORD = "password";
    private static final String[] USER_ROLES = new String[]{"admin", "viewer"};

    private static final String TESTING_MESSAGE = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus iaculis leo purus, vitae finibus felis fringilla eget.";

    @LocalServerPort
    private Integer port;

    @Autowired
    private AuthenticatedUserController authenticatedUserController;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private WebSocketStompClient webSocketStompClient;

    private WebSocketHttpHeaders headers;


    private String getWsPath() {
        return String.format("ws://127.0.0.1:%d/kendo-tournament-backend/%s", port, WebSocketConfiguration.SOCKETS_STOMP_URL);
    }

    @BeforeClass
    public void authentication() {
        AuthenticatedUser authenticatedUser = authenticatedUserController.createUser(null, USER_NAME, USER_FIRST_NAME, USER_LAST_NAME, USER_PASSWORD, USER_ROLES);

        headers = new WebSocketHttpHeaders();
        headers.set("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(authenticatedUser, "127.0.0.1"));
    }


    @BeforeMethod
    public void setup() {
        WebSocketClient webSocketClient = new StandardWebSocketClient();

        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(webSocketClient));
        SockJsClient sockJsClient = new SockJsClient(transports);

        this.webSocketStompClient = new WebSocketStompClient(sockJsClient);
        this.webSocketStompClient.setMessageConverter(new StringMessageConverter());
    }


    @Test
    public void echoTest() throws ExecutionException, InterruptedException, TimeoutException {
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

        StompSession session = webSocketStompClient.connectAsync(getWsPath(), this.headers,
                new StompSessionHandlerAdapter() {
                }).get(1, TimeUnit.SECONDS);

        session.subscribe(WebSocketConfiguration.SOCKET_SEND_PREFIX + EchoWebSocketController.ECHO_MAPPING, new StompFrameHandler() {

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((String) payload);
            }
        });

        session.send(WebSocketConfiguration.SOCKET_RECEIVE_PREFIX + EchoWebSocketController.ECHO_INBOUND_MAPPING, TESTING_MESSAGE);

        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(blockingQueue.poll()).isEqualTo(TESTING_MESSAGE));
    }

}
