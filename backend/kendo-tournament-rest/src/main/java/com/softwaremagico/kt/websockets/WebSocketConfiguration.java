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

import com.softwaremagico.kt.logger.WebsocketsLogger;
import com.softwaremagico.kt.rest.exceptions.InvalidJwtException;
import com.softwaremagico.kt.rest.security.JwtTokenUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + WebSocketConfiguration.ORDER)
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    static final int ORDER = 99;

    private static final String JWT_CUSTOM_HEADER = "JWT-Token";

    //Where is listening to messages
    public static final String SOCKET_RECEIVE_PREFIX = "/backend";

    //Where messages will be sent.
    public static final String SOCKET_SEND_PREFIX = "/topic";
    public static final String SOCKET_ERROR_PREFIX = "/error";

    //URL where the client must subscribe.
    public static final String SOCKETS_STOMP_URL = "/websockets";

    private final JwtTokenUtil jwtTokenUtil;


    public WebSocketConfiguration(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(SOCKETS_STOMP_URL)
                .setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(SOCKET_RECEIVE_PREFIX, SOCKET_ERROR_PREFIX)
                .enableSimpleBroker(SOCKET_SEND_PREFIX);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                //message.getHeaders();
                final StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    final LinkedMultiValueMap<String, String> nativeHeaders = (LinkedMultiValueMap<String, String>) accessor.getHeader("nativeHeaders");
                    final List<String> jwtToken = nativeHeaders.get(JWT_CUSTOM_HEADER);
                    try {
                        jwtTokenUtil.getUsername(jwtToken.get(0));
                        final String username = jwtTokenUtil.getUsername(jwtToken.get(0));
                        if (username != null && !username.isEmpty()) {
                            accessor.setUser(new UserPrincipal(username));
                            WebsocketsLogger.debug(this.getClass(), "JWT token ({}) accepted for websockets.", username);
                        } else {
                            throw new InvalidJwtException(this.getClass(), "No valid user found on JWT token");
                        }
                    } catch (Exception e) {
                        //Unauthorized.
                        WebsocketsLogger.warning(this.getClass(), "Invalid Token for websockets!");
                    }
                }
                return message;
            }
        });
    }

    static class UserPrincipal implements Principal {

        private final String name;

        UserPrincipal(String userName) {
            this.name = userName;
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
