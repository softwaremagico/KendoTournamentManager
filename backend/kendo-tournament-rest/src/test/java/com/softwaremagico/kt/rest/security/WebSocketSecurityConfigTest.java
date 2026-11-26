package com.softwaremagico.kt.rest.security;

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

import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

@Test(groups = "webSocketSecurityConfigTests")
public class WebSocketSecurityConfigTest {

    @Test
    public void shouldBuildMessageAuthorizationManager() {
        final WebSocketSecurityConfig config = new WebSocketSecurityConfig();
        final MessageMatcherDelegatingAuthorizationManager.Builder builder = MessageMatcherDelegatingAuthorizationManager.builder();

        final AuthorizationManager<Message<?>> authorizationManager = config.messageAuthorizationManager(builder);

        assertNotNull(authorizationManager);
    }
}

