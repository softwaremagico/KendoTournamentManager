package com.softwaremagico.kt.rest.security;

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

import com.softwaremagico.kt.websockets.WebSocketConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

import static org.springframework.messaging.simp.SimpMessageType.MESSAGE;
import static org.springframework.messaging.simp.SimpMessageType.SUBSCRIBE;

/**
 * Not working, use configureClientInboundChannel on WebsocketConfiguration.
 */
//@Configuration
//@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    /**
     * https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html#websocket-authorization
     *
     * @param messages
     * @return
     */
    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        messages
                .nullDestMatcher().authenticated()
                //.simpSubscribeDestMatchers("/user/queue/errors").permitAll()
                .simpDestMatchers(WebSocketConfiguration.SOCKET_SEND_PREFIX + "/**").hasRole("VIEWER")
                .simpTypeMatchers(MESSAGE, SUBSCRIBE).denyAll()
                .anyMessage().denyAll();

        return messages.build();
    }
}
