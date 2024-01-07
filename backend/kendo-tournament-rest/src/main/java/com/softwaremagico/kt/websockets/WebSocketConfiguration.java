package com.softwaremagico.kt.websockets;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    //Where is listening to messages
    public static final String SOCKET_RECEIVE_PREFIX = "/app";

    //Where messages will be sent.
    public static final String SOCKET_SEND_PREFIX = "/topic";

    //URL where the client must subscribe.
    public static final String SOCKETS_STOMP_URL = "/ws-endpoint";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(SOCKETS_STOMP_URL)
                .setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(SOCKET_RECEIVE_PREFIX)
                .enableSimpleBroker(SOCKET_SEND_PREFIX);
    }
}
