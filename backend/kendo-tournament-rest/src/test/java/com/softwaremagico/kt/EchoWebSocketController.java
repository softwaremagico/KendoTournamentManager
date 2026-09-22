package com.softwaremagico.kt;

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

import com.softwaremagico.kt.websockets.WebSocketConfiguration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class EchoWebSocketController {

    public static final String ECHO_MAPPING = "/echo";
    public static final String ECHO_INBOUND_MAPPING = "/welcome";

    private final SimpMessagingTemplate messagingTemplate;

    public EchoWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping(ECHO_INBOUND_MAPPING)
    public void echo(String payload, Principal principal) {
        final String destination = WebSocketConfiguration.SOCKET_SEND_PREFIX + "/tenant/"
                + ((WebSocketConfiguration.UserPrincipal) principal).getTenantId() + ECHO_MAPPING;
        messagingTemplate.convertAndSend(destination, payload);
    }
}
