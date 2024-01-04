package com.softwaremagico.kt.websockets;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.awaitility.Awaitility.await;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Test(groups = "websockets")
public class BasicWebsocketsTests extends AbstractTestNGSpringContextTests {

    private static final String TESTING_MESSAGE = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus iaculis leo purus, vitae finibus felis fringilla eget.";

    @LocalServerPort
    private Integer port;

    private WebSocketStompClient webSocketStompClient;

    private String getWsPath() {
        return String.format("ws://localhost:%d/%s", port, WebSocketConfiguration.SOCKET_RECEIVE_PREFIX);
    }


    @BeforeClass
    public void setup() throws ExecutionException, InterruptedException, TimeoutException {
        webSocketStompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));

        StompSession session = webSocketStompClient.connectAsync(String.format("ws://localhost:%d/ws-endpoint", port, WebSocketConfiguration.SOCKETS_ROOT_URL),
                new StompSessionHandlerAdapter() {
                }).get(1, TimeUnit.SECONDS);

        webSocketStompClient.setMessageConverter(new StringMessageConverter());
    }


    @Test
    public void echoTest() throws ExecutionException, InterruptedException, TimeoutException {
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

        StompSession session = webSocketStompClient.connectAsync(String.format("ws://localhost:%d/ws-endpoint", port, WebSocketConfiguration.SOCKETS_ROOT_URL),
                new StompSessionHandlerAdapter() {
                }).get(1, TimeUnit.SECONDS);

        session.subscribe("/topic/greetings", new StompFrameHandler() {

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((String) payload);
            }
        });

        session.send("/app/welcome", TESTING_MESSAGE);

        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> Assert.assertEquals("Hello, Mike!", blockingQueue.poll()));
    }

}
