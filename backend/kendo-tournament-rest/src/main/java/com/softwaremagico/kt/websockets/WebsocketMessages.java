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

import com.softwaremagico.kt.core.controller.AchievementController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.utils.ShiaijoName;
import com.softwaremagico.kt.websockets.models.messages.AchievementAllGeneratedNumberParameters;
import com.softwaremagico.kt.websockets.models.messages.AchievementGeneratedNumberParameters;
import com.softwaremagico.kt.websockets.models.messages.ShiaijoFinishedParameters;
import org.springframework.stereotype.Component;

@Component
public class WebsocketMessages {

    public WebsocketMessages(AchievementController achievementController, DuelController duelController, WebSocketController webSocketController) {

        //Send a message when the achievements from one tournament are finished.
        achievementController.addAchievementsGeneratedListener((achievementsGenerated, tournament) ->
                webSocketController.sendMessage("achievementGenerated", "info",
                        new AchievementGeneratedNumberParameters(tournament.getName(), achievementsGenerated.size())));

        //Send a message when the achievements from all tournaments are finished.
        achievementController.addAchievementsGeneratedAllTournamentsListener((achievementsGenerated, tournaments) ->
                webSocketController.sendMessage("achievementAllGenerated", "info",
                        new AchievementAllGeneratedNumberParameters(tournaments.size(), achievementsGenerated.size())));

        //Send a message when the all fights from a shiaijo are over.
        duelController.addShiaijoFinishedListener(((tournament, shiaijo) -> {
            webSocketController.sendMessage("shiaijoFinished", "info",
                    new ShiaijoFinishedParameters(tournament.getName(), ShiaijoName.getShiaijoName(shiaijo)));
        }));
    }

}
