package com.softwaremagico.kt.websockets;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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

import com.softwaremagico.kt.logger.SuppressFBWarnings;
import com.softwaremagico.kt.logger.WebsocketsLogger;
import com.softwaremagico.kt.rest.exceptions.InvalidJwtException;
import com.softwaremagico.kt.rest.security.JwtTokenUtil;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
    private final TenantRepository tenantRepository;


    @Autowired
    public WebSocketConfiguration(JwtTokenUtil jwtTokenUtil, TenantRepository tenantRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.tenantRepository = tenantRepository;
    }

    /** Convenience constructor for isolated unit tests without persistence. */
    @SuppressFBWarnings(value = {"NP_NONNULL_PARAM_VIOLATION"},
            justification = "Test-only constructor; tenantRepository is null-checked in authenticateIfPossible and validateTenantDestination.")
    public WebSocketConfiguration(JwtTokenUtil jwtTokenUtil) {
        this(jwtTokenUtil, null);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This will allow you to use ws://localhost:8080/websocket to establish websocket connection
        registry.addEndpoint(SOCKETS_STOMP_URL)
                .setAllowedOrigins("*");
        // This will allow you to use http://localhost:8080/websocket to establish websocket connection
        registry.addEndpoint(SOCKETS_STOMP_URL)
                .setAllowedOrigins("*").withSockJS();
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
            @Nullable //NOSONAR - Matches ChannelInterceptor#preSend contract, which is also @Nullable despite its @NonNullApi package.
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                final StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && requiresAuthentication(accessor)) {
                    authenticateIfPossible(accessor);
                }
                return message;
            }
        });
    }

    private boolean requiresAuthentication(StompHeaderAccessor accessor) {
        return StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand());
    }

    private void authenticateIfPossible(StompHeaderAccessor accessor) {
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validateTenantDestination(accessor);
            return;
        }
        final List<String> jwtToken = getJwtToken(accessor);
        if (jwtToken.isEmpty()) {
            throw new InvalidJwtException(this.getClass(), "A JWT token is required for websockets.");
        }
        try {
            final String token = jwtToken.getFirst();
            final String username = jwtTokenUtil.getUsername(token);
            final Integer tenantId = jwtTokenUtil.getTenantId(token);
            if (jwtTokenUtil.validate(token) && username != null && !username.isEmpty() && tenantId != null
                    && (tenantRepository == null || tenantRepository.existsByIdAndActiveTrue(tenantId))) {
                accessor.setUser(new UserPrincipal(username, tenantId));
                WebsocketsLogger.debug(this.getClass(), "JWT token ({}) accepted for websockets.", username);
            } else {
                throw new InvalidJwtException(this.getClass(), "No valid user found on JWT token");
            }
        } catch (Exception ex) {
            WebsocketsLogger.warning(this.getClass(), "Invalid Token for websockets ({})!", ex.getMessage());
            throw new InvalidJwtException(this.getClass(), "Invalid JWT token for websockets.");
        }
    }

    private void validateTenantDestination(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof UserPrincipal principal)) {
            throw new InvalidJwtException(this.getClass(), "Websocket subscription is not authenticated.");
        }
        if (tenantRepository != null && !tenantRepository.existsByIdAndActiveTrue(principal.tenantId)) {
            throw new InvalidJwtException(this.getClass(), "Websocket tenant is inactive.");
        }
        final String expectedPrefix = SOCKET_SEND_PREFIX + "/tenant/" + principal.tenantId + "/";
        if (accessor.getDestination() == null || !accessor.getDestination().startsWith(expectedPrefix)) {
            throw new InvalidJwtException(this.getClass(), "Websocket destination belongs to another tenant.");
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getJwtToken(StompHeaderAccessor accessor) {
        final LinkedMultiValueMap<String, String> nativeHeaders =
                (LinkedMultiValueMap<String, String>) accessor.getHeader("nativeHeaders");
        if (nativeHeaders == null) {
            return List.of();
        }
        final List<String> jwtHeader = nativeHeaders.get(JWT_CUSTOM_HEADER);
        return jwtHeader == null ? List.of() : jwtHeader;
    }

    public static class UserPrincipal implements Principal {

        private final String name;
        private final Integer tenantId;

        UserPrincipal(String userName, Integer tenantId) {
            this.name = userName;
            this.tenantId = tenantId;
        }

        UserPrincipal(String userName) {
            this(userName, 1);
        }

        @Override
        public String getName() {
            return name;
        }

        public Integer getTenantId() {
            return tenantId;
        }
    }
}
