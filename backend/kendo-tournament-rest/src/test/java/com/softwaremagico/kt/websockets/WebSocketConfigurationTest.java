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
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.rest.security.JwtTokenUtil;
import com.softwaremagico.kt.persistence.repositories.TenantRepository;
import com.softwaremagico.kt.security.AvailableRole;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;

@Test(groups = "webSocketConfiguration")
public class WebSocketConfigurationTest {

    @Mock
    private JwtTokenUtil mockJwtTokenUtil;

    @Mock
    private TenantRepository tenantRepository;

    private WebSocketConfiguration configuration;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        configuration = new WebSocketConfiguration(mockJwtTokenUtil, tenantRepository);
    }

    @Test
    public void registerStompEndpoints_expectBothEndpointsRegistered() {
        final StompEndpointRegistry registry = mock(StompEndpointRegistry.class, RETURNS_DEEP_STUBS);
        final StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class, RETURNS_DEEP_STUBS);
        when(registry.addEndpoint(WebSocketConfiguration.SOCKETS_STOMP_URL)).thenReturn(registration);
        when(registration.setAllowedOrigins("*")).thenReturn(registration);

        configuration.registerStompEndpoints(registry);

        verify(registry, times(2)).addEndpoint(WebSocketConfiguration.SOCKETS_STOMP_URL);
    }

    @Test
    public void configureMessageBroker_expectPrefixesAndBrokerConfigured() {
        final MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class, RETURNS_DEEP_STUBS);
        when(registry.setApplicationDestinationPrefixes(WebSocketConfiguration.SOCKET_RECEIVE_PREFIX, WebSocketConfiguration.SOCKET_ERROR_PREFIX))
                .thenReturn(registry);

        configuration.configureMessageBroker(registry);

        verify(registry, times(1))
                .setApplicationDestinationPrefixes(WebSocketConfiguration.SOCKET_RECEIVE_PREFIX, WebSocketConfiguration.SOCKET_ERROR_PREFIX);
    }

    private ChannelInterceptor captureInterceptor() {
        final ChannelRegistration registration = mock(ChannelRegistration.class);
        configuration.configureClientInboundChannel(registration);
        final ArgumentCaptor<ChannelInterceptor[]> captor = ArgumentCaptor.forClass(ChannelInterceptor[].class);
        verify(registration).interceptors(captor.capture());
        return captor.getValue()[0];
    }

    @Test
    public void preSend_withNonConnectCommand_expectNoAuthenticationAttempt() {
        final ChannelInterceptor interceptor = captureInterceptor();
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
        final var message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class));

        assertNull(accessor.getUser());
    }

    @Test
    public void preSend_withConnectCommandAndNoJwtHeader_expectInvalidJwt() {
        final ChannelInterceptor interceptor = captureInterceptor();
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        final var message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

		assertThrows(com.softwaremagico.kt.rest.exceptions.InvalidJwtException.class,
				() -> interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class)));
    }

    @Test
    public void preSend_withValidJwt_expectUserSet() {
        final ChannelInterceptor interceptor = captureInterceptor();
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("JWT-Token", "some.jwt.token");
        final var message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(mockJwtTokenUtil.getUsername("some.jwt.token")).thenReturn("john");
		when(mockJwtTokenUtil.getTenantId("some.jwt.token")).thenReturn(1);
		when(mockJwtTokenUtil.validate("some.jwt.token")).thenReturn(true);
		when(tenantRepository.existsByIdAndActiveTrue(1)).thenReturn(true);

        interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class));

        assertEquals(StompHeaderAccessor.wrap(message).getUser().getName(), "john");
    }

    @Test
    public void preSend_withInactiveTenant_expectInvalidJwt() {
        configuration = new WebSocketConfiguration(mockJwtTokenUtil, tenantRepository);
        final ChannelInterceptor interceptor = captureInterceptor();
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("JWT-Token", "inactive.tenant.token");
        final var message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(mockJwtTokenUtil.getUsername("inactive.tenant.token")).thenReturn("john");
        when(mockJwtTokenUtil.getTenantId("inactive.tenant.token")).thenReturn(7);
        when(mockJwtTokenUtil.validate("inactive.tenant.token")).thenReturn(true);
        when(tenantRepository.existsByIdAndActiveTrue(7)).thenReturn(false);

        assertThrows(com.softwaremagico.kt.rest.exceptions.InvalidJwtException.class,
                () -> interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class)));
    }

    @Test
    public void preSend_withJwtResolvingEmptyUsername_expectInvalidJwt() {
        final ChannelInterceptor interceptor = captureInterceptor();
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("JWT-Token", "invalid.token");
        final var message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(mockJwtTokenUtil.getUsername("invalid.token")).thenReturn("");

		assertThrows(com.softwaremagico.kt.rest.exceptions.InvalidJwtException.class,
				() -> interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class)));
    }

    @Test
    public void preSend_withJwtTokenUtilThrowing_expectInvalidJwt() {
        final ChannelInterceptor interceptor = captureInterceptor();
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("JWT-Token", "broken.token");
        final var message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(mockJwtTokenUtil.getUsername(any())).thenThrow(new RuntimeException("boom"));

		assertThrows(com.softwaremagico.kt.rest.exceptions.InvalidJwtException.class,
				() -> interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class)));
    }

    @Test
    public void userPrincipal_expectNameReturned() {
        final WebSocketConfiguration.UserPrincipal principal = new WebSocketConfiguration.UserPrincipal("someone");
        assertEquals(principal.getName(), "someone");
        assertEquals(principal.getRoles(), Set.of());
    }

    @Test
    public void userPrincipal_expectRolesNormalized() {
        final Set<String> roles = new HashSet<>(Arrays.asList(" admin ", " ", null, "viewer"));
        final WebSocketConfiguration.UserPrincipal principal =
                new WebSocketConfiguration.UserPrincipal("john", 1, roles);

        assertEquals(principal.getRoles(), Set.of("ADMIN", "VIEWER"));
        assertEquals(principal.getTenantId(), Integer.valueOf(1));
    }

    @Test
    public void userPrincipal_expectEmptyRolesForNullInput() {
        assertEquals(new WebSocketConfiguration.UserPrincipal("john", 1, null).getRoles(), Set.of());
    }

    @Test
    public void constructorWithProviders_expectConfigurationCreated() {
        final AuthenticatedUserProvider users = mock(AuthenticatedUserProvider.class);
        final ParticipantProvider participants = mock(ParticipantProvider.class);

        assertNotNull(new WebSocketConfiguration(mockJwtTokenUtil, tenantRepository, users, participants));
    }

    @Test
    public void registerStompEndpoints_expectOnlyExactConfiguredOrigins() throws Exception {
        setField(configuration, "serverCorsDomains",
                Arrays.asList("https://app.example.com", null, "  ", "*", "https://other.example.com"));
        final StompEndpointRegistry registry = mock(StompEndpointRegistry.class, RETURNS_DEEP_STUBS);
        final StompWebSocketEndpointRegistration registration = mock(StompWebSocketEndpointRegistration.class, RETURNS_DEEP_STUBS);
        when(registry.addEndpoint(WebSocketConfiguration.SOCKETS_STOMP_URL)).thenReturn(registration);

        configuration.registerStompEndpoints(registry);

        verify(registration, times(2)).setAllowedOrigins("https://app.example.com", "https://other.example.com");
    }

    @Test
    public void resolveCurrentRoles_withoutProviders_expectTokenRoles() throws Exception {
        when(mockJwtTokenUtil.getRoles("token")).thenReturn(Set.of("VIEWER"));

        assertEquals(resolveRoles(configuration, "token", "john", 1), Set.of("VIEWER"));
    }

    @Test
    public void resolveCurrentRoles_withValidUser_expectCurrentRoles() throws Exception {
        final AuthenticatedUserProvider users = mock(AuthenticatedUserProvider.class);
        final WebSocketConfiguration config = new WebSocketConfiguration(mockJwtTokenUtil, tenantRepository,
                users, mock(ParticipantProvider.class));
        final AuthenticatedUser user = new AuthenticatedUser("john", 1);
        user.setRoles(Set.of("ADMIN"));
        when(users.findByUsername("john")).thenReturn(Optional.of(user));
        when(users.belongsToTenant(user, 1)).thenReturn(true);

        assertEquals(resolveRoles(config, "token", "john", 1), Set.of("ADMIN"));
    }

    @Test
    public void resolveCurrentRoles_withParticipantFallback_expectParticipantRoles() throws Exception {
        final AuthenticatedUserProvider users = mock(AuthenticatedUserProvider.class);
        final ParticipantProvider participants = mock(ParticipantProvider.class);
        final WebSocketConfiguration config = new WebSocketConfiguration(mockJwtTokenUtil, tenantRepository,
                users, participants);
        final Participant participant = mock(Participant.class);
        when(users.findByUsername("john")).thenReturn(Optional.empty());
        when(participants.findByTokenUsername("john")).thenReturn(Optional.of(participant));
        when(participant.getRoles()).thenReturn(Set.of("PARTICIPANT"));
        when(participant.isAccountNonExpired()).thenReturn(true);
        when(participant.isAccountNonLocked()).thenReturn(true);
        when(participant.isCredentialsNonExpired()).thenReturn(true);
        when(participant.isEnabled()).thenReturn(true);

        assertEquals(resolveRoles(config, "token", "john", 1), Set.of("PARTICIPANT"));
    }

    @Test
    public void resolveCurrentRoles_withUnknownUser_expectInvalidJwt() {
        final AuthenticatedUserProvider users = mock(AuthenticatedUserProvider.class);
        final ParticipantProvider participants = mock(ParticipantProvider.class);
        final WebSocketConfiguration config = new WebSocketConfiguration(mockJwtTokenUtil, tenantRepository,
                users, participants);
        when(users.findByUsername("unknown")).thenReturn(Optional.empty());
        when(participants.findByTokenUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(com.softwaremagico.kt.rest.exceptions.InvalidJwtException.class,
                () -> resolveRoles(config, "token", "unknown", 1));
    }

    @Test
    public void resolveCurrentRoles_withTenantMismatch_expectInvalidJwt() {
        final AuthenticatedUserProvider users = mock(AuthenticatedUserProvider.class);
        final WebSocketConfiguration config = new WebSocketConfiguration(mockJwtTokenUtil, tenantRepository,
                users, mock(ParticipantProvider.class));
        final AuthenticatedUser user = new AuthenticatedUser("john", 2);
        when(users.findByUsername("john")).thenReturn(Optional.of(user));
        when(users.belongsToTenant(user, 1)).thenReturn(false);

        assertThrows(com.softwaremagico.kt.rest.exceptions.InvalidJwtException.class,
                () -> resolveRoles(config, "token", "john", 1));
    }

    @Test
    public void resolveCurrentRoles_withDisabledAccount_expectInvalidJwt() {
        final AuthenticatedUserProvider users = mock(AuthenticatedUserProvider.class);
        final WebSocketConfiguration config = new WebSocketConfiguration(mockJwtTokenUtil, tenantRepository,
                users, mock(ParticipantProvider.class));
        final AuthenticatedUser user = mock(AuthenticatedUser.class);
        when(users.findByUsername("john")).thenReturn(Optional.of(user));
        when(users.belongsToTenant(user, 1)).thenReturn(true);
        when(user.isEnabled()).thenReturn(false);

        assertThrows(com.softwaremagico.kt.rest.exceptions.InvalidJwtException.class,
                () -> resolveRoles(config, "token", "john", 1));
    }

    @Test
    public void resolveCurrentRoles_withGuest_expectRolesWithoutTenantMembership() throws Exception {
        final AuthenticatedUserProvider users = mock(AuthenticatedUserProvider.class);
        final WebSocketConfiguration config = new WebSocketConfiguration(mockJwtTokenUtil, tenantRepository,
                users, mock(ParticipantProvider.class));
        final AuthenticatedUser guest = new AuthenticatedUser(AuthenticatedUserProvider.GUEST_USER, 1);
        guest.setRoles(Set.of("GUEST"));
        when(users.findByUsername(AuthenticatedUserProvider.GUEST_USER)).thenReturn(Optional.of(guest));

        assertEquals(resolveRoles(config, "token", AuthenticatedUserProvider.GUEST_USER, 1), Set.of("GUEST"));
    }

    @Test
    public void resolveCurrentRoles_withoutRoles_expectEmptySet() throws Exception {
        final AuthenticatedUserProvider users = mock(AuthenticatedUserProvider.class);
        final WebSocketConfiguration config = new WebSocketConfiguration(mockJwtTokenUtil, tenantRepository,
                users, mock(ParticipantProvider.class));
        final AuthenticatedUser user = new AuthenticatedUser("john", 1);
        when(users.findByUsername("john")).thenReturn(Optional.of(user));
        when(users.belongsToTenant(user, 1)).thenReturn(true);

        assertEquals(resolveRoles(config, "token", "john", 1), Set.of());
    }

    @Test
    public void validateDestination_withViewerRoleAndKnownTopic_expectAccepted() {
        when(tenantRepository.existsByIdAndActiveTrue(1)).thenReturn(true);

        validateDestination(configuration,
                new WebSocketConfiguration.UserPrincipal("john", 1, Set.of("VIEWER")),
                "/topic/tenant/1/fights");
    }

    @Test
    public void validateDestination_withGuestAndReadOnlyTopic_expectAccepted() {
        when(tenantRepository.existsByIdAndActiveTrue(1)).thenReturn(true);

        validateDestination(configuration,
                new WebSocketConfiguration.UserPrincipal("guest", 1, Set.of(AvailableRole.GUEST.name())),
                "/topic/tenant/1/unties");
    }

    @Test
    public void validateDestination_withUnknownTopic_expectInvalidJwt() {
        when(tenantRepository.existsByIdAndActiveTrue(1)).thenReturn(true);

        assertThrows(com.softwaremagico.kt.rest.exceptions.InvalidJwtException.class,
                () -> validateDestination(configuration,
                        new WebSocketConfiguration.UserPrincipal("john", 1, Set.of("VIEWER")),
                        "/topic/tenant/1/unknown"));
    }

    @Test
    public void validateDestination_withGuestOnAdminTopic_expectInvalidJwt() {
        when(tenantRepository.existsByIdAndActiveTrue(1)).thenReturn(true);

        assertThrows(com.softwaremagico.kt.rest.exceptions.InvalidJwtException.class,
                () -> validateDestination(configuration,
                        new WebSocketConfiguration.UserPrincipal("guest", 1, Set.of(AvailableRole.PARTICIPANT.name())),
                        "/topic/tenant/1/groups"));
    }

    @Test
    public void validateDestination_withoutManagementRole_expectInvalidJwt() {
        when(tenantRepository.existsByIdAndActiveTrue(1)).thenReturn(true);

        assertThrows(com.softwaremagico.kt.rest.exceptions.InvalidJwtException.class,
                () -> validateDestination(configuration,
                        new WebSocketConfiguration.UserPrincipal("john", 1, Set.of("OTHER")),
                        "/topic/tenant/1/fights"));
    }

    private void setField(Object target, String name, Object value) throws Exception {
        final Field field = WebSocketConfiguration.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private Set<String> resolveRoles(WebSocketConfiguration config, String token, String username, Integer tenantId) {
        try {
            final Method method = WebSocketConfiguration.class.getDeclaredMethod(
                    "resolveCurrentRoles", String.class, String.class, Integer.class);
            method.setAccessible(true);
            return (Set<String>) method.invoke(config, token, username, tenantId);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (RuntimeException) e.getCause();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void validateDestination(WebSocketConfiguration config, WebSocketConfiguration.UserPrincipal principal,
                                     String destination) {
        try {
            final Method method = WebSocketConfiguration.class.getDeclaredMethod(
                    "validateTenantDestination", StompHeaderAccessor.class);
            method.setAccessible(true);
            final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
            accessor.setUser(principal);
            accessor.setDestination(destination);
            method.invoke(config, accessor);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (RuntimeException) e.getCause();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
