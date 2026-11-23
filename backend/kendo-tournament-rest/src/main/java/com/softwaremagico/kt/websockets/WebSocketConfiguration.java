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

import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.logger.WebsocketsLogger;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.TenantContext;
import com.softwaremagico.kt.rest.exceptions.InvalidJwtException;
import com.softwaremagico.kt.rest.security.JwtTokenUtil;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.security.AvailableRole;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + WebSocketConfiguration.ORDER)
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    static final int ORDER = 99;

    private static final String JWT_CUSTOM_HEADER = "JWT-Token";

    // Guest and participant tokens are read-only: they may follow fights and unties.
    private static final Set<String> READ_ONLY_TOPICS = Set.of(
            WebSocketController.FIGHTS_MAPPING,
            WebSocketController.UNTIES_MAPPING);
    private static final Set<String> MANAGEMENT_ROLES = Set.of(
            AvailableRole.VIEWER.name(),
            AvailableRole.EDITOR.name(),
            AvailableRole.ADMIN.name(),
            AvailableRole.SUPER_ADMIN.name());
    private static final Set<String> KNOWN_TOPICS = Set.of(
            WebSocketController.FIGHTS_MAPPING,
            WebSocketController.UNTIES_MAPPING,
            WebSocketController.GROUPS_MAPPING,
            WebSocketController.CREATING_MAPPING,
            WebSocketController.UPDATING_MAPPING,
            WebSocketController.DELETES_MAPPING,
            WebSocketController.MESSAGES_MAPPING,
            WebSocketController.ERRORS_MAPPING,
            WebSocketController.ECHO_MAPPING);

    @Value("${server.cors.domains:}")
    private List<String> serverCorsDomains;

    //Where is listening to messages
    public static final String SOCKET_RECEIVE_PREFIX = "/backend";

    //Where messages will be sent.
    public static final String SOCKET_SEND_PREFIX = "/topic";
    public static final String SOCKET_ERROR_PREFIX = "/error";

    //URL where the client must subscribe.
    public static final String SOCKETS_STOMP_URL = "/websockets";

    private final JwtTokenUtil jwtTokenUtil;
    private final TenantRepository tenantRepository;
    private AuthenticatedUserProvider authenticatedUserProvider;
    private ParticipantProvider participantProvider;


    @Autowired
    public WebSocketConfiguration(JwtTokenUtil jwtTokenUtil, TenantRepository tenantRepository,
                                   AuthenticatedUserProvider authenticatedUserProvider,
                                   ParticipantProvider participantProvider) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.tenantRepository = tenantRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.participantProvider = participantProvider;
    }

    /** Kept for unit tests that do not need database-backed revalidation. */
    public WebSocketConfiguration(JwtTokenUtil jwtTokenUtil, TenantRepository tenantRepository) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        final String[] allowedOrigins = getAllowedOrigins();
        // Same-origin only unless explicit frontend origins are configured.
        // A wildcard is never accepted for WebSocket handshakes either.
        registry.addEndpoint(SOCKETS_STOMP_URL)
                .setAllowedOrigins(allowedOrigins);
        registry.addEndpoint(SOCKETS_STOMP_URL)
                .setAllowedOrigins(allowedOrigins).withSockJS();
    }

    private String[] getAllowedOrigins() {
        if (serverCorsDomains == null) {
            return new String[0];
        }
        return serverCorsDomains.stream()
                .filter(origin -> origin != null && !origin.isBlank() && !origin.contains("*"))
                .toArray(String[]::new);
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
            if (!jwtTokenUtil.validate(token)) {
                throw new InvalidJwtException(this.getClass(), "No valid user found on JWT token");
            }
            final String username = jwtTokenUtil.getUsername(token);
            final Integer tenantId = jwtTokenUtil.getTenantId(token);
            final Set<String> roles = resolveCurrentRoles(token, username, tenantId);
            if (username != null && !username.isEmpty() && tenantId != null
                    && (tenantRepository == null || tenantRepository.existsByIdAndActiveTrue(tenantId))) {
                accessor.setUser(new UserPrincipal(username, tenantId, roles));
                WebsocketsLogger.debug(this.getClass(), "JWT token ({}) accepted for websockets.", username);
            } else {
                throw new InvalidJwtException(this.getClass(), "No valid user found on JWT token");
            }
        } catch (Exception ex) {
            WebsocketsLogger.warning(this.getClass(), "Invalid Token for websockets ({})!", ex.getMessage());
            throw new InvalidJwtException(this.getClass(), "Invalid JWT token for websockets.");
        }
    }

    /**
     * Revalidates the account on every STOMP CONNECT so tenant removal, disabling
     * and role changes take effect immediately instead of relying on stale JWT claims.
     */
    private Set<String> resolveCurrentRoles(String token, @Nullable String username, @Nullable Integer tenantId) {
        // Unit tests construct this configuration without database providers.
        if (authenticatedUserProvider == null || participantProvider == null || username == null || tenantId == null) {
            return jwtTokenUtil.getRoles(token);
        }
        TenantContext.setTenantId(tenantId);
        try {
            IAuthenticatedUser current = authenticatedUserProvider.findByUsername(username).orElse(null);
            if (current == null) {
                current = participantProvider.findByTokenUsername(username).orElse(null);
            }
            if (current == null) {
                throw new InvalidJwtException(this.getClass(), "No valid user found for JWT token");
            }
            final boolean guest = AuthenticatedUserProvider.GUEST_USER.equals(username);
            if (!guest && current instanceof AuthenticatedUser authenticatedUser
                    && !authenticatedUserProvider.belongsToTenant(authenticatedUser, tenantId)) {
                throw new InvalidJwtException(this.getClass(), "JWT token tenant does not match the user.");
            }
            final UserDetails details = (UserDetails) current;
            if (!details.isAccountNonExpired() || !details.isAccountNonLocked()
                    || !details.isCredentialsNonExpired() || !details.isEnabled()) {
                throw new InvalidJwtException(this.getClass(), "JWT token belongs to a disabled or expired account.");
            }
            return current.getRoles() == null ? Set.of() : Set.copyOf(current.getRoles());
        } finally {
            TenantContext.clear();
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
        final String topic = "/" + accessor.getDestination().substring(expectedPrefix.length());
        if (!KNOWN_TOPICS.contains(topic)) {
            throw new InvalidJwtException(this.getClass(), "Unknown websocket destination.");
        }
        // Tenant membership alone is not enough: guests and participants must not
        // receive administrative broadcasts (groups, creates/updates/deletes, messages).
        final boolean restricted = principal.getRoles().stream().anyMatch(role ->
                AvailableRole.GUEST.name().equalsIgnoreCase(role)
                        || AvailableRole.PARTICIPANT.name().equalsIgnoreCase(role));
        if (restricted) {
            if (!READ_ONLY_TOPICS.contains(topic)) {
                throw new InvalidJwtException(this.getClass(), "Websocket destination requires a viewer role.");
            }
        } else if (principal.getRoles().stream().noneMatch(MANAGEMENT_ROLES::contains)) {
            throw new InvalidJwtException(this.getClass(), "Websocket destination requires a viewer role.");
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
        private final Set<String> roles;

        UserPrincipal(String userName, Integer tenantId, @Nullable Set<String> roles) {
            this.name = userName;
            this.tenantId = tenantId;
            this.roles = roles == null ? Set.of() : roles.stream()
                    .filter(role -> role != null && !role.isBlank())
                    .map(role -> role.trim().toUpperCase(Locale.ROOT))
                    .collect(Collectors.toUnmodifiableSet());
        }

        UserPrincipal(String userName, Integer tenantId) {
            this(userName, tenantId, Set.of());
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

        public Set<String> getRoles() {
            return roles;
        }
    }
}
