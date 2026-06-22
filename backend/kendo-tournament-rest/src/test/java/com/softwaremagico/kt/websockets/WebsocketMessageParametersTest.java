package com.softwaremagico.kt.websockets;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import com.softwaremagico.kt.websockets.models.messages.AchievementAllGeneratedNumberParameters;
import com.softwaremagico.kt.websockets.models.messages.AchievementGeneratedNumberParameters;
import com.softwaremagico.kt.websockets.models.messages.MessageContentType;
import com.softwaremagico.kt.websockets.models.messages.ShiaijoFinishedParameters;
import com.softwaremagico.kt.websockets.models.messages.UserAdminCreatedParameters;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = "websocketMessages")
public class WebsocketMessageParametersTest {

    @Test
    public void shouldHandleAchievementGeneratedParameters() {
        final AchievementGeneratedNumberParameters parameters =
                new AchievementGeneratedNumberParameters("Autumn Cup", 5L);

        assertEquals(parameters.getTournamentName(), "Autumn Cup");
        assertEquals(parameters.getAchievementsNumber(), 5L);

        parameters.setTournamentName("Winter Cup");
        parameters.setAchievementsNumber(8L);

        assertEquals(parameters.getTournamentName(), "Winter Cup");
        assertEquals(parameters.getAchievementsNumber(), 8L);
    }

    @Test
    public void shouldHandleAchievementAllGeneratedParameters() {
        final AchievementAllGeneratedNumberParameters parameters =
                new AchievementAllGeneratedNumberParameters(3, 12L);

        assertEquals(parameters.getTournamentNumber(), 3);
        assertEquals(parameters.getAchievementsNumber(), 12L);

        parameters.setTournamentNumber(4);
        parameters.setAchievementsNumber(20L);

        assertEquals(parameters.getTournamentNumber(), 4);
        assertEquals(parameters.getAchievementsNumber(), 20L);
    }

    @Test
    public void shouldHandleShiaijoAndUserAdminParameters() {
        final ShiaijoFinishedParameters shiaijoFinishedParameters =
                new ShiaijoFinishedParameters("National", "Court A");
        final UserAdminCreatedParameters userAdminCreatedParameters =
                new UserAdminCreatedParameters("admin-user");

        assertEquals(shiaijoFinishedParameters.getTournamentName(), "National");
        assertEquals(shiaijoFinishedParameters.getShiaijoName(), "Court A");
        assertEquals(userAdminCreatedParameters.getUserName(), "admin-user");

        shiaijoFinishedParameters.setTournamentName("Regional");
        shiaijoFinishedParameters.setShiaijoName("Court B");
        userAdminCreatedParameters.setUserName("super-admin");

        assertEquals(shiaijoFinishedParameters.getTournamentName(), "Regional");
        assertEquals(shiaijoFinishedParameters.getShiaijoName(), "Court B");
        assertEquals(userAdminCreatedParameters.getUserName(), "super-admin");
    }

    @Test
    public void shouldExposeAllMessageContentTypes() {
        final MessageContentType[] values = MessageContentType.values();

        assertEquals(values.length, 6);
        assertEquals(MessageContentType.valueOf("INFO"), MessageContentType.INFO);
        assertEquals(MessageContentType.valueOf("WARNING"), MessageContentType.WARNING);
        assertEquals(MessageContentType.valueOf("ERROR"), MessageContentType.ERROR);
        assertEquals(MessageContentType.valueOf("CREATED"), MessageContentType.CREATED);
        assertEquals(MessageContentType.valueOf("DELETED"), MessageContentType.DELETED);
        assertEquals(MessageContentType.valueOf("UPDATED"), MessageContentType.UPDATED);
        assertTrue(values[0].ordinal() <= values[values.length - 1].ordinal());
    }
}

