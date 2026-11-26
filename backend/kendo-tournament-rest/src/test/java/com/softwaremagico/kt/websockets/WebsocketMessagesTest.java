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

import com.softwaremagico.kt.core.controller.AchievementController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.models.AchievementDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.rest.security.AuthApi;
import com.softwaremagico.kt.websockets.models.messages.MessageContentType;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Test(groups = "websocketMessages")
public class WebsocketMessagesTest {

    @Mock
    private AchievementController mockAchievementController;

    @Mock
    private DuelController mockDuelController;

    @Mock
    private WebSocketController mockWebSocketController;

    @Mock
    private AuthApi mockAuthApi;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void constructor_expectAchievementGeneratedListenerSendsMessage() {
        new WebsocketMessages(this.mockAchievementController, this.mockDuelController, this.mockWebSocketController,
                this.mockAuthApi);

        final ArgumentCaptor<AchievementController.AchievementsGeneratedListener> captor = ArgumentCaptor
                .forClass(AchievementController.AchievementsGeneratedListener.class);
        verify(this.mockAchievementController).addAchievementsGeneratedListener(captor.capture());

        final TournamentDTO tournament = new TournamentDTO();
        tournament.setName("tournament");
        captor.getValue().generated(List.of(new AchievementDTO()), tournament);

        verify(this.mockWebSocketController, times(1)).sendMessage(eq("backendMessage.achievementGenerated"),
                eq(MessageContentType.INFO), any());
    }

    @Test
    public void constructor_expectAchievementAllGeneratedListenerSendsMessage() {
        new WebsocketMessages(this.mockAchievementController, this.mockDuelController, this.mockWebSocketController,
                this.mockAuthApi);

        final ArgumentCaptor<AchievementController.AchievementsGeneratedAllTournamentsListener> captor = ArgumentCaptor
                .forClass(AchievementController.AchievementsGeneratedAllTournamentsListener.class);
        verify(this.mockAchievementController).addAchievementsGeneratedAllTournamentsListener(captor.capture());

        final TournamentDTO tournament = new TournamentDTO();
        tournament.setName("tournament");
        captor.getValue().generated(List.of(new AchievementDTO()), List.of(tournament));

        verify(this.mockWebSocketController, times(1)).sendMessage(eq("backendMessage.achievementAllGenerated"),
                eq(MessageContentType.INFO), any());
    }

    @Test
    public void constructor_expectShiaijoFinishedListenerSendsMessage() {
        new WebsocketMessages(this.mockAchievementController, this.mockDuelController, this.mockWebSocketController,
                this.mockAuthApi);

        final ArgumentCaptor<DuelController.ShiaijoFinishedListener> captor = ArgumentCaptor
                .forClass(DuelController.ShiaijoFinishedListener.class);
        verify(this.mockDuelController).addShiaijoFinishedListener(captor.capture());

        final TournamentDTO tournament = new TournamentDTO();
        tournament.setName("tournament");
        captor.getValue().finished(tournament, 1);

        verify(this.mockWebSocketController, times(1)).sendMessage(eq("backendMessage.shiaijoFinished"),
                eq(MessageContentType.INFO), any());
    }

    @Test
    public void constructor_expectUserAdminGeneratedListenerSendsMessage() {
        new WebsocketMessages(this.mockAchievementController, this.mockDuelController, this.mockWebSocketController,
                this.mockAuthApi);

        final ArgumentCaptor<AuthApi.UserAdminGeneratedListener> captor = ArgumentCaptor
                .forClass(AuthApi.UserAdminGeneratedListener.class);
        verify(this.mockAuthApi).addUserAdminGeneratedListeners(captor.capture());

        captor.getValue().generated("admin");

        verify(this.mockWebSocketController, times(1)).sendMessage(eq("backendMessage.userAdminGenerated"),
                eq(MessageContentType.INFO), any());
    }
}
