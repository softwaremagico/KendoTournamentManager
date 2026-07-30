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

import com.softwaremagico.kt.rest.security.JwtTokenUtil;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Test(groups = "webSocketConfiguration")
public class WebSocketConfigurationTest {

    @Mock
    private JwtTokenUtil mockJwtTokenUtil;

    private WebSocketConfiguration configuration;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        configuration = new WebSocketConfiguration(mockJwtTokenUtil);
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
    public void preSend_withConnectCommandAndNoJwtHeader_expectNoUserSet() {
        final ChannelInterceptor interceptor = captureInterceptor();
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        final var message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class));

        assertNull(accessor.getUser());
    }

    @Test
    public void preSend_withValidJwt_expectUserSet() {
        final ChannelInterceptor interceptor = captureInterceptor();
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("JWT-Token", "some.jwt.token");
        final var message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(mockJwtTokenUtil.getUsername("some.jwt.token")).thenReturn("john");

        interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class));

        assertEquals(StompHeaderAccessor.wrap(message).getUser().getName(), "john");
    }

    @Test
    public void preSend_withJwtResolvingEmptyUsername_expectNoUserSet() {
        final ChannelInterceptor interceptor = captureInterceptor();
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("JWT-Token", "invalid.token");
        final var message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(mockJwtTokenUtil.getUsername("invalid.token")).thenReturn("");

        interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class));

        assertNull(StompHeaderAccessor.wrap(message).getUser());
    }

    @Test
    public void preSend_withJwtTokenUtilThrowing_expectExceptionSwallowed() {
        final ChannelInterceptor interceptor = captureInterceptor();
        final StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        accessor.addNativeHeader("JWT-Token", "broken.token");
        final var message = org.springframework.messaging.support.MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(mockJwtTokenUtil.getUsername(any())).thenThrow(new RuntimeException("boom"));

        interceptor.preSend(message, mock(org.springframework.messaging.MessageChannel.class));

        assertNull(StompHeaderAccessor.wrap(message).getUser());
    }

    @Test
    public void userPrincipal_expectNameReturned() {
        final WebSocketConfiguration.UserPrincipal principal = new WebSocketConfiguration.UserPrincipal("someone");
        assertEquals(principal.getName(), "someone");
    }
}



