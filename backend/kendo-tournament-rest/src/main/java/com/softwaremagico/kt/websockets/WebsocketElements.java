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
import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.FightStatisticsController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.GroupLinkController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.ParticipantImageController;
import com.softwaremagico.kt.core.controller.ParticipantStatisticsController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.TournamentImageController;
import com.softwaremagico.kt.core.controller.TournamentStatisticsController;
import org.springframework.stereotype.Component;

@Component
public class WebsocketElements {

    public WebsocketElements(AchievementController achievementController,
                             ClubController clubController,
                             DuelController duelController,
                             FightController fightController,
                             FightStatisticsController fightStatisticsController,
                             GroupController groupController,
                             GroupLinkController groupLinkController,
                             ParticipantController participantController,
                             ParticipantImageController participantImageController,
                             ParticipantStatisticsController participantStatisticsController,
                             RoleController roleController,
                             TeamController teamController,
                             TournamentController tournamentController,
                             TournamentExtraPropertyController tournamentExtraPropertyController,
                             TournamentImageController tournamentImageController,
                             TournamentStatisticsController tournamentStatisticsController,
                             WebSocketController webSocketController) {

        //Refresh tables when an element is created.
        achievementController.addElementCreatedListeners((webSocketController::elementCreated));
        achievementController.addElementUpdatedListeners((webSocketController::elementUpdated));
        achievementController.addElementDeletedListeners((webSocketController::elementDeleted));

        clubController.addElementCreatedListeners((webSocketController::elementCreated));
        clubController.addElementUpdatedListeners((webSocketController::elementUpdated));
        clubController.addElementDeletedListeners((webSocketController::elementDeleted));

        duelController.addElementCreatedListeners((webSocketController::elementCreated));
        duelController.addElementUpdatedListeners((webSocketController::elementUpdated));
        duelController.addElementDeletedListeners((webSocketController::elementDeleted));

        fightController.addElementCreatedListeners((webSocketController::elementCreated));
        fightController.addElementUpdatedListeners((webSocketController::elementUpdated));
        fightController.addElementDeletedListeners((webSocketController::elementDeleted));

        fightStatisticsController.addElementCreatedListeners((webSocketController::elementCreated));
        fightStatisticsController.addElementUpdatedListeners((webSocketController::elementUpdated));
        fightStatisticsController.addElementDeletedListeners((webSocketController::elementDeleted));

        groupController.addElementCreatedListeners((webSocketController::elementCreated));
        groupController.addElementUpdatedListeners((webSocketController::elementUpdated));
        groupController.addElementDeletedListeners((webSocketController::elementDeleted));

        groupLinkController.addElementCreatedListeners((webSocketController::elementCreated));
        groupLinkController.addElementUpdatedListeners((webSocketController::elementUpdated));
        groupLinkController.addElementDeletedListeners((webSocketController::elementDeleted));

        participantController.addElementCreatedListeners((webSocketController::elementCreated));
        participantController.addElementUpdatedListeners((webSocketController::elementUpdated));
        participantController.addElementDeletedListeners((webSocketController::elementDeleted));

        participantImageController.addElementCreatedListeners((webSocketController::elementCreated));
        participantImageController.addElementUpdatedListeners((webSocketController::elementUpdated));
        participantImageController.addElementDeletedListeners((webSocketController::elementDeleted));

        participantStatisticsController.addElementCreatedListeners((webSocketController::elementCreated));
        participantStatisticsController.addElementUpdatedListeners((webSocketController::elementUpdated));
        participantStatisticsController.addElementDeletedListeners((webSocketController::elementDeleted));

        roleController.addElementCreatedListeners((webSocketController::elementCreated));
        roleController.addElementUpdatedListeners((webSocketController::elementUpdated));
        roleController.addElementDeletedListeners((webSocketController::elementDeleted));

        teamController.addElementCreatedListeners((webSocketController::elementCreated));
        teamController.addElementUpdatedListeners((webSocketController::elementUpdated));
        teamController.addElementDeletedListeners((webSocketController::elementDeleted));

        tournamentController.addElementCreatedListeners((webSocketController::elementCreated));
        tournamentController.addElementUpdatedListeners((webSocketController::elementUpdated));
        tournamentController.addElementDeletedListeners((webSocketController::elementDeleted));

        tournamentExtraPropertyController.addElementCreatedListeners((webSocketController::elementCreated));
        tournamentExtraPropertyController.addElementUpdatedListeners((webSocketController::elementUpdated));
        tournamentExtraPropertyController.addElementDeletedListeners((webSocketController::elementDeleted));

        tournamentImageController.addElementCreatedListeners((webSocketController::elementCreated));
        tournamentImageController.addElementUpdatedListeners((webSocketController::elementUpdated));
        tournamentImageController.addElementDeletedListeners((webSocketController::elementDeleted));

        tournamentStatisticsController.addElementCreatedListeners((webSocketController::elementCreated));
        tournamentStatisticsController.addElementUpdatedListeners((webSocketController::elementUpdated));
        tournamentStatisticsController.addElementDeletedListeners((webSocketController::elementDeleted));
    }

}
