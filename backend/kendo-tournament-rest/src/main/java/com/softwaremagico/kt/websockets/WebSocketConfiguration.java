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
    public static final String SOCKET_PREFIX = "/websockets";

    //Where messages will be sent.
    public static final String SOCKET_TOPIC = "/frontend";

    //URL where the client must subscribe.
    public static final String SOCKETS_ROOT = "/sockets";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(SOCKETS_ROOT)
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(SOCKET_PREFIX)
                .enableSimpleBroker(SOCKET_TOPIC);
    }
}
